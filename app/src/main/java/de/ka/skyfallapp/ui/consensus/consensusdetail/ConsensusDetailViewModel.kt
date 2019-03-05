package de.ka.skyfallapp.ui.consensus.consensusdetail

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.base.events.BACK
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.ConsensusResponse
import de.ka.skyfallapp.repo.api.RequestAccessBody
import de.ka.skyfallapp.repo.api.SuggestionResponse
import de.ka.skyfallapp.repo.subscribeRepoCompletion

import de.ka.skyfallapp.ui.consensus.consensusdetail.suggestionlist.SuggestionsAdapter
import de.ka.skyfallapp.ui.consensus.consensusdetail.newsuggestion.NewSuggestionFragment
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.LockView
import de.ka.skyfallapp.utils.start


import de.ka.skyfallapp.utils.with
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator
import okhttp3.ResponseBody
import timber.log.Timber
import java.text.SimpleDateFormat

class ConsensusDetailViewModel(app: Application) : BaseViewModel(app), LockView.UnlockListener {

    val swipeToRefreshListener = SwipeRefreshLayout.OnRefreshListener { refreshDetails() }
    val refresh = MutableLiveData<Boolean>().apply { postValue(false) }
    val title = MutableLiveData<String>().apply { postValue("") }
    val creator = MutableLiveData<String>().apply { postValue("") }
    val creationDate = MutableLiveData<String>().apply { postValue("") }
    val endDate = MutableLiveData<String>().apply { postValue("") }
    val adminVisibility = MutableLiveData<Int>().apply { postValue(View.GONE) }
    val publicVisibility = MutableLiveData<Int>().apply { postValue(View.GONE) }
    val notFinishedVisibility = MutableLiveData<Int>().apply { postValue(View.GONE) }
    val unlockedVisibility = MutableLiveData<Int>().apply { postValue(View.GONE) }
    val finishedVisibility = MutableLiveData<Int>().apply { postValue(View.GONE) }
    val statusColor = MutableLiveData<Int>().apply {
        value = ContextCompat.getColor(app.applicationContext, R.color.colorStatusLocked)
    }
    val unlockState = MutableLiveData<LockView.LockedViewState>().apply { value = LockView.LockedViewState.HIDDEN }
    val adapter = MutableLiveData<SuggestionsAdapter>()
    val unlockListener: LockView.UnlockListener = this
    val description = MutableLiveData<String>().apply { postValue("") }
    val blankVisibility = MutableLiveData<Int>().apply { postValue(View.GONE) }

    var consensusId: Int = -1
    var isFinished = false

    init {
        repository.consensusManager.observableSuggestions
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onError = { error -> error.printStackTrace() },
                onNext = {
                    if (it.invalidate) {
                        refreshDetails()
                    } else {
                        adapter.value?.insert(it.list, isFinished)

                        if (it.list.isEmpty()) {
                            blankVisibility.postValue(View.VISIBLE)
                        } else {
                            blankVisibility.postValue(View.GONE)
                        }

                    }
                }
            )
            .addTo(compositeDisposable)
    }

    private val addMoreClickListener = {
        navigateTo(R.id.action_consensusDetailFragment_to_newSuggestionFragment,
            false,
            Bundle().apply { putInt(NewSuggestionFragment.CONS_ID_KEY, consensusId) })
    }

    override fun onUnlockRequested(password: String) {
        repository.consensusManager.sendConsensusAccessRequest(consensusId, RequestAccessBody(password))
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion { showDetails(it, fromLock = true) }
            .start(compositeDisposable, ::showLockLoading)
    }

    fun layoutManager() = LinearLayoutManager(app.applicationContext)

    fun setupAdapterAndLoad(owner: LifecycleOwner, id: Int) {
        adapter.value = (SuggestionsAdapter(
            owner = owner,
            addMoreClickListener = addMoreClickListener,
            toolsClickListener = ::askForSuggestionTools
        ))
        //TODO reset details...
        isFinished = false
        title.postValue("")
        description.postValue("")
        creator.postValue("")
        creationDate.postValue("")
        endDate.postValue("")
        blankVisibility.postValue(View.GONE)
        adminVisibility.postValue(View.GONE)
        publicVisibility.postValue(View.GONE)
        notFinishedVisibility.postValue(View.GONE)
        finishedVisibility.postValue(View.GONE)
        unlockedVisibility.postValue(View.GONE)
        statusColor.postValue(ContextCompat.getColor(app.applicationContext, R.color.colorStatusLocked))
        unlockState.value = LockView.LockedViewState.HIDDEN

        consensusId = id

        refreshDetails()
    }

    private fun refreshDetails() {
        repository.consensusManager.getConsensusDetail(consensusId)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion { showDetails(it, fromLock = false) }
            .start(compositeDisposable, ::showLoading)
    }

    fun itemAnimator() = SlideInDownAnimator()

    fun updateConsensus() {
        // val consensusDetail = currentConsensusDetail ?: return

        /*  repository.sendConsensus(consensusDetail)
              .with(AndroidSchedulerProvider())
              .subscribeRepoCompletion { showDetails(it, isRefresh = false) }
              .start(compositeDisposable, ::showLoading)*/

    }

    fun askForConsensusDeletion() {
        handle(ConsensusDeletionAsk())
    }

    fun askForSuggestionTools(view: View, id: Int) {
        handle(SuggestionToolsAsk(view, id))
    }

    fun deleteConsensus() {
        repository.consensusManager.deleteConsensus(consensusId)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion(::showDeletion)
            .start(compositeDisposable, ::showLoading)
    }

    fun deleteSuggestion(suggestionId: Int) {
        repository.consensusManager.deleteSuggestion(consensusId, suggestionId)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion { refresh.postValue(false) }
            .start(compositeDisposable, ::showLoading)
    }

    fun onBack() {
        navigateTo(BACK)
    }

    private fun showDeletion(result: RepoData<ResponseBody?>) {
        refresh.postValue(false)

        if (result.info.code == 200) {
            navigateTo(BACK)
        }

        Timber.e("woha $result")
    }

    private fun loadSuggestions() {
        repository.consensusManager.getConsensusSuggestions(consensusId)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion(::showSuggestions)
            .start(compositeDisposable, ::showLoading)

    }

    private fun showDetails(result: RepoData<ConsensusResponse?>, fromLock: Boolean) {
        refresh.postValue(false)

        result.data?.let {
            title.postValue(it.title)
            description.postValue(if (it.description.isNullOrBlank()) app.applicationContext.getString(R.string.consensus_fallback_description) else it.description)
            creator.postValue(it.creator)
            creationDate.postValue(SimpleDateFormat().format(it.creationDate))
            endDate.postValue(SimpleDateFormat().format(it.endDate))

            if (it.admin) {
                adminVisibility.postValue(View.VISIBLE)
            }

            if (it.public) {
                publicVisibility.postValue(View.VISIBLE)
            }

            if (it.hasAccess) { // has to be before finished, to show the right color
                statusColor.postValue(ContextCompat.getColor(app.applicationContext, R.color.colorStatusUnlocked))
            }

            if (it.finished) {
                statusColor.postValue(ContextCompat.getColor(app.applicationContext, R.color.colorStatusFinished))
                finishedVisibility.postValue(View.VISIBLE)
            } else {
                notFinishedVisibility.postValue(View.VISIBLE)
            }

            isFinished = it.finished

            if (!it.finished && !it.hasAccess) {
                if (fromLock) {
                    unlockState.postValue(LockView.LockedViewState.ERROR) // must be wrong password.. TODO animate on error?
                } else {
                    unlockState.postValue(LockView.LockedViewState.SHOW)
                }
            } else {
                unlockedVisibility.postValue(View.VISIBLE)
                unlockState.postValue(LockView.LockedViewState.HIDE)
                loadSuggestions()
            }
        }

        if (result.data == null) {
            unlockState.postValue(LockView.LockedViewState.ERROR)

            apiErrorHandler.handle(result) {
                showSnack(it.toString())
            }
        }
    }


    private fun showLockLoading() {
        unlockState.postValue(LockView.LockedViewState.LOAD)
    }

    private fun showSuggestions(result: RepoData<List<SuggestionResponse>?>) {
        refresh.postValue(false)


        // handle errors
    }

    private fun showLoading() {
        refresh.postValue(true)
    }

    class ConsensusDeletionAsk

    class SuggestionToolsAsk(val view: View, val id: Int)
}

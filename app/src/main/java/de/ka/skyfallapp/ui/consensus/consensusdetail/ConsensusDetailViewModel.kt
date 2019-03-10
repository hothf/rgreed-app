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
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.ConsensusResponse
import de.ka.skyfallapp.repo.api.RequestAccessBody
import de.ka.skyfallapp.repo.api.SuggestionResponse
import de.ka.skyfallapp.repo.subscribeRepoCompletion

import de.ka.skyfallapp.ui.consensus.consensusdetail.suggestionlist.SuggestionsAdapter
import de.ka.skyfallapp.ui.consensus.consensusdetail.neweditsuggestion.NewEditSuggestionFragment
import de.ka.skyfallapp.utils.*
import de.ka.skyfallapp.utils.NavigationUtils.BACK


import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator
import okhttp3.ResponseBody
import timber.log.Timber

class ConsensusDetailViewModel(app: Application) : BaseViewModel(app), LockView.UnlockListener {

    private var currentConsensus: ConsensusResponse? = null
    private var currentId = -1

    val unlockListener: LockView.UnlockListener = this
    val adapter = MutableLiveData<SuggestionsAdapter>()
    val title = MutableLiveData<String>().apply { value = "" }
    val creator = MutableLiveData<String>().apply { value = "" }
    val endDate = MutableLiveData<String>().apply { value = "" }
    val description = MutableLiveData<String>().apply { value = "" }
    val refresh = MutableLiveData<Boolean>().apply { value = false }
    val creationDate = MutableLiveData<String>().apply { value = "" }
    val blankVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val adminVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val publicVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val unlockedVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val finishedVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val descriptionVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val swipeToRefreshListener = SwipeRefreshLayout.OnRefreshListener { refreshDetails() }
    val adminAndNotFinishedVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val unlockState = MutableLiveData<LockView.LockedViewState>().apply { value = LockView.LockedViewState.HIDDEN }
    val statusColor = MutableLiveData<Int>().apply {
        value = ContextCompat.getColor(app.applicationContext, R.color.colorStatusLocked)
    }

    private val addMoreClickListener = {
        navigateTo(R.id.action_consensusDetailFragment_to_newSuggestionFragment,
            false,
            Bundle().apply { putInt(NewEditSuggestionFragment.CONS_ID_KEY, currentId) })
    }

    init {
        repository.consensusManager.observableSuggestions
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onError = { error -> error.printStackTrace() },
                onNext = {
                    if (it.invalidate) {
                        refreshDetails()
                    } else {
                        adapter.value?.insert(it.list, currentConsensus?.finished ?: false)

                        if (it.list.isEmpty()) {
                            blankVisibility.postValue(View.VISIBLE)
                        } else {
                            blankVisibility.postValue(View.GONE)
                        }
                    }
                }
            )
            .addTo(compositeDisposable)

        repository.consensusManager.observableConsensuses
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onError = { error -> error.printStackTrace() },
                onNext = {
                    // either the whole list of items has been changed, or a single item. This is a special case,
                    // for example, when coming from a deeplink, we do no want to add this to the whole list, but still
                    // be able to show the details
                    if (it.item != null) {
                        updateDetails(it.item)
                    } else {
                        it.list.find { consensus -> consensus.id == currentId }?.let(::updateDetails)
                    }
                }
            )
            .addTo(compositeDisposable)

        repository.profileManager.observableProfile
            .with(AndroidSchedulerProvider())
            .subscribeBy(onNext = { refreshDetails() })
            .addTo(compositeDisposable)
    }

    fun setupAdapterAndLoad(owner: LifecycleOwner, id: Int) {
        if (currentId == id) {
            currentConsensus?.let { updateDetails(it) }
            return
        }

        currentId = id

        adapter.value = (SuggestionsAdapter(
            owner = owner,
            addMoreClickListener = addMoreClickListener,
            toolsClickListener = ::askForSuggestionTools
        ))

        // TODO add a nicer empty state .. because we could come to this screen with no internet connection and it will look a bit ugly.
        // resets all current saved details
        currentConsensus = null
        title.postValue("")
        description.postValue("")
        creator.postValue("")
        creationDate.postValue("")
        endDate.postValue("")
        blankVisibility.postValue(View.GONE)
        adminVisibility.postValue(View.GONE)
        publicVisibility.postValue(View.GONE)
        finishedVisibility.postValue(View.GONE)
        unlockedVisibility.postValue(View.GONE)
        adminAndNotFinishedVisibility.postValue(View.GONE)
        unlockState.value = LockView.LockedViewState.HIDDEN
        statusColor.postValue(ContextCompat.getColor(app.applicationContext, R.color.colorStatusLocked))

        refreshDetails()
    }

    private fun refreshDetails() {
        repository.consensusManager.getConsensusDetail(currentId)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion { onDetailsLoaded(it, fromLock = false) }
            .start(compositeDisposable) {
                showLoading()
                showLockLoading()
            }
    }

    override fun onUnlockRequested(password: String) {
        currentConsensus?.let {
            repository.consensusManager.sendConsensusAccessRequest(it.id, RequestAccessBody(password))
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion { result -> onDetailsLoaded(result, fromLock = true) }
                .start(compositeDisposable, ::showLockLoading)
        }
    }

    fun deleteConsensus() {
        currentConsensus?.let {
            repository.consensusManager.deleteConsensus(it.id)
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion(::showDeletion)
                .start(compositeDisposable, ::showLoading)
        }
    }

    fun deleteSuggestion(suggestionId: Int) {
        currentConsensus?.let {
            repository.consensusManager.deleteSuggestion(it.id, suggestionId)
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion { refresh.postValue(false) }
                .start(compositeDisposable, ::showLoading)
        }
    }

    private fun loadSuggestions() {
        currentConsensus?.let {
            repository.consensusManager.getConsensusSuggestions(it.id)
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion(::onSuggestionsLoaded)
                .start(compositeDisposable, ::showLoading)
        }
    }

    fun askForSuggestionTools(view: View, suggestionResponse: SuggestionResponse) {
        handle(SuggestionToolsAsk(view, suggestionResponse))
    }

    fun onToolsClick(view: View) {
        handle(ConsensusToolsAsk(view, currentConsensus))
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

    private fun updateDetails(it: ConsensusResponse) {
        currentConsensus = it

        title.postValue(it.title)
        description.postValue(it.description)
        creator.postValue(it.creator)
        creationDate.postValue(it.creationDate.toDateTime())
        endDate.postValue(it.endDate.toDateTime())

        if (it.description.isNullOrBlank()) {
            descriptionVisibility.postValue(View.GONE)
        } else {
            descriptionVisibility.postValue(View.VISIBLE)
        }

        if (it.admin) {
            adminVisibility.postValue(View.VISIBLE)

            if (!it.finished) {
                adminAndNotFinishedVisibility.postValue(View.VISIBLE)
            } else {
                adminAndNotFinishedVisibility.postValue(View.GONE)
            }
        } else {
            adminVisibility.postValue(View.GONE)
        }

        if (it.public) {
            publicVisibility.postValue(View.VISIBLE)
        } else {
            publicVisibility.postValue(View.GONE)
        }

        var statColor = ContextCompat.getColor(app.applicationContext, R.color.colorStatusLocked)
        var lockState = LockView.LockedViewState.HIDE

        if (it.hasAccess) { // has to be before finished, to show the right color
            statColor = ContextCompat.getColor(app.applicationContext, R.color.colorStatusUnlocked)
            unlockedVisibility.postValue(View.VISIBLE)
        } else {
            lockState = LockView.LockedViewState.SHOW
            unlockedVisibility.postValue(View.GONE)
        }

        if (it.finished) {
            statColor = ContextCompat.getColor(app.applicationContext, R.color.colorStatusFinished)
            finishedVisibility.postValue(View.VISIBLE)
            lockState = LockView.LockedViewState.HIDE
        } else {
            finishedVisibility.postValue(View.GONE)
        }

        unlockState.postValue(lockState)
        statusColor.postValue(statColor)
    }

    private fun onDetailsLoaded(result: RepoData<ConsensusResponse?>, fromLock: Boolean) {
        refresh.postValue(false)

        result.data?.let {
            loadSuggestions()

            if (fromLock && !it.hasAccess) {
                unlockState.postValue(LockView.LockedViewState.ERROR) // must be wrong password.. TODO animate on error?
            }
        }

        if (result.data == null) {
            if (fromLock) {
                unlockState.postValue(LockView.LockedViewState.ERROR)
            }
            apiErrorHandler.handle(result) {
                showSnack(it.toString())
            }
        }
    }

    private fun onSuggestionsLoaded(result: RepoData<List<SuggestionResponse>?>) {
        refresh.postValue(false)


        // handle errors
    }

    private fun showLockLoading() {
        unlockState.postValue(LockView.LockedViewState.LOAD)
    }

    private fun showLoading() {
        refresh.postValue(true)
    }

    fun itemAnimator() = SlideInDownAnimator()

    fun layoutManager() = LinearLayoutManager(app.applicationContext)

    class SuggestionToolsAsk(val view: View, val data: SuggestionResponse)

    class ConsensusToolsAsk(val view: View, val data: ConsensusResponse?)
}

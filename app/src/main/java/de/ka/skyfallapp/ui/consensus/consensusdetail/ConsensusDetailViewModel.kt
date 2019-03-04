package de.ka.skyfallapp.ui.consensus.consensusdetail

import android.app.Application
import android.os.Bundle

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

class ConsensusDetailViewModel(app: Application) : BaseViewModel(app), LockView.UnlockListener {

    val swipeToRefreshListener = SwipeRefreshLayout.OnRefreshListener { refreshDetails() }
    val refresh = MutableLiveData<Boolean>().apply { postValue(false) }
    val title = MutableLiveData<String>().apply { postValue("") }
    val unlockState = MutableLiveData<LockView.LockedViewState>().apply { value = LockView.LockedViewState.HIDDEN }
    val adapter = MutableLiveData<SuggestionsAdapter>()
    val unlockListener: LockView.UnlockListener = this

    var consensusId: Int = -1

    init {
        repository.consensusManager.observableSuggestions
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onNext = {
                    if (it.invalidate) {
                        refreshDetails()
                    } else {
                        adapter.value?.insert(it.list)
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
        adapter.value = (SuggestionsAdapter(owner = owner, addMoreClickListener = addMoreClickListener))
        //TODO reset details...
        title.postValue("")
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

    fun deleteConsensus() {
        repository.consensusManager.deleteConsensus(consensusId)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion(::showDeletion)
            .start(compositeDisposable, ::showLoading)
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

            //TODO show lock or dismiss lock: provide a click listener for the lockView throug databinding
            // when the click is registered, start the loading process
            // update the lockview with its updateState method on loading start / error. Finish is a special case:
            // this also updates the curren details! be aware and also call error or hide there  ...

            if (!it.finished && !it.hasAccess) {
                if (fromLock) {
                    unlockState.postValue(LockView.LockedViewState.ERROR) // must be wrong password..
                } else {
                    unlockState.postValue(LockView.LockedViewState.SHOW)
                }
            } else {
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
}

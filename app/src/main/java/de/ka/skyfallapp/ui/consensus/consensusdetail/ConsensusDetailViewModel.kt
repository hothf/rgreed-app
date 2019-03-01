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
import de.ka.skyfallapp.repo.api.SuggestionResponse
import de.ka.skyfallapp.repo.subscribeRepoCompletion

import de.ka.skyfallapp.ui.consensus.consensusdetail.suggestionlist.SuggestionsAdapter
import de.ka.skyfallapp.ui.consensus.consensusdetail.newsuggestion.NewSuggestionFragment
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.start


import de.ka.skyfallapp.utils.with
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator
import okhttp3.ResponseBody
import timber.log.Timber

class ConsensusDetailViewModel(app: Application) : BaseViewModel(app) {

    val swipeToRefreshListener = SwipeRefreshLayout.OnRefreshListener { loadSuggestions() }
    val adapter = MutableLiveData<SuggestionsAdapter>()
    val refresh = MutableLiveData<Boolean>().apply { postValue(false) }
    val title = MutableLiveData<String>().apply { postValue("") }

    init {
        repository.consensusManager.observableSuggestions
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onNext = {
                    if (it.invalidate) {
                        loadSuggestions()
                    } else {
                        adapter.value?.insert(it.list)
                        refreshDetails()
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

    var consensusId: Int = -1

    fun layoutManager() = LinearLayoutManager(app.applicationContext)

    fun setupAdapterAndLoad(owner: LifecycleOwner, id: Int) {
        adapter.value = (SuggestionsAdapter(owner = owner, addMoreClickListener = addMoreClickListener))

        //TODO reset details...
        title.postValue("")

        consensusId = id

        loadSuggestions()
    }

    fun refreshDetails() {
        repository.consensusManager.getConsensusDetail(consensusId)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion { showDetails(it) }
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

    private fun showDetails(result: RepoData<ConsensusResponse?>) {
        refresh.postValue(false)

        result.data?.let {

            title.postValue(it.title)


        }

        result.info.throwable?.let { showSnack(it.toString()) }
    }

    private fun showSuggestions(result: RepoData<List<SuggestionResponse>?>) {
        refresh.postValue(false)

        // handle errors
    }

    private fun showLoading() {
        refresh.postValue(true)
    }

    class ConsensusDeletionAsk


    companion object {
        const val CONSENSUS_DETAIL_DATA = "ConsensusDetailData"
    }
}

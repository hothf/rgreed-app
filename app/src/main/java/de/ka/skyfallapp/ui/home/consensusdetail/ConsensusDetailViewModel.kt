package de.ka.skyfallapp.ui.home.consensusdetail

import android.app.Application
import android.os.Bundle
import android.view.View
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
import de.ka.skyfallapp.ui.home.HomeFragment
import de.ka.skyfallapp.ui.home.HomeViewModel

import de.ka.skyfallapp.ui.home.consensusdetail.suggestionlist.SuggestionsAdapter
import de.ka.skyfallapp.ui.home.consensusdetail.newsuggestion.NewSuggestionFragment
import de.ka.skyfallapp.ui.personal.PersonalFragment
import de.ka.skyfallapp.ui.personal.PersonalViewModel
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.start


import de.ka.skyfallapp.utils.with
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import okhttp3.ResponseBody
import timber.log.Timber

class ConsensusDetailViewModel(app: Application) : BaseViewModel(app) {

    val swipeToRefreshListener = SwipeRefreshLayout.OnRefreshListener { refreshDetails() }
    var blankVisibility = MutableLiveData<Int>().apply { postValue(View.GONE) }
    val adapter = MutableLiveData<SuggestionsAdapter>()
    val refresh = MutableLiveData<Boolean>().apply { postValue(false) }
    val title = MutableLiveData<String>().apply { postValue("") }

    init {
        dirtyDataWatcher.subject
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onNext = {
                    if (it.key == CONSENSUS_DETAIL_DATA) {
                        Timber.e("Dirty: ${it.key}")
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

        adapter.postValue(SuggestionsAdapter(owner = owner, addMoreClickListener = addMoreClickListener))

        title.postValue("")

        consensusId = id

        refreshDetails()
    }

    fun refreshDetails() {
        repository.consensusManager.getConsensusDetail(consensusId)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion { showDetails(it, markDirty = false) }
            .start(compositeDisposable, ::showLoading)
    }

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
            markDirty()
            navigateTo(BACK)
        }

        Timber.e("woha $result")
    }

    private fun showDetails(result: RepoData<ConsensusResponse?>, markDirty: Boolean) {
        refresh.postValue(false)

        result.data?.let {

            if (markDirty) {
                markDirty()
            }

            title.postValue(it.title)

            repository.consensusManager.getConsensusSuggestions(it.id)
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion(::showSuggestions)
                .start(compositeDisposable, ::showLoading)
        }

        result.info.throwable?.let { showSnack(it.toString()) }
    }

    private fun showSuggestions(result: RepoData<List<SuggestionResponse>?>) {
        refresh.postValue(false)

        result.data?.let { adapter.value?.insert(it) }
    }

    private fun showLoading() {
        refresh.postValue(true)
    }

    class ConsensusDeletionAsk

    private fun markDirty() {
        dirtyDataWatcher.markDirty(HomeViewModel.HOME_DATA, consensusId)
        dirtyDataWatcher.markDirty(PersonalViewModel.PERSONAL_DATA)
    }


    companion object {
        const val CONSENSUS_DETAIL_DATA = "ConsensusDetailData"
    }
}

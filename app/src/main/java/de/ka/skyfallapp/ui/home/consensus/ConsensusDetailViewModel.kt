package de.ka.skyfallapp.ui.home.consensus

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

import de.ka.skyfallapp.ui.home.consensus.list.SuggestionsAdapter
import de.ka.skyfallapp.ui.home.consensus.newsuggestion.NewSuggestionFragment
import de.ka.skyfallapp.ui.personal.PersonalFragment
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.start


import de.ka.skyfallapp.utils.with
import io.reactivex.Completable
import okhttp3.ResponseBody
import timber.log.Timber

class ConsensusDetailViewModel(app: Application) : BaseViewModel(app) {

    val swipeToRefreshListener = SwipeRefreshLayout.OnRefreshListener { refreshDetails() }
    var blankVisibility = MutableLiveData<Int>().apply { postValue(View.GONE) }
    val adapter = MutableLiveData<SuggestionsAdapter>()
    val refresh = MutableLiveData<Boolean>().apply { postValue(false) }

    private val addMoreClickListener = {
        navigateTo(R.id.action_consensusDetailFragment_to_newSuggestionFragment,
            false,
            Bundle().apply { putInt(NewSuggestionFragment.CONS_ID_KEY, id) })
    }

    var id: Int = 0
    var currentConsensusDetail: ConsensusResponse? = null

    fun layoutManager() = LinearLayoutManager(app.applicationContext)

    fun setupAdapterAndLoad(owner: LifecycleOwner, consensusId: Int) {

        //if (adapter.value == null) {
        adapter.postValue(SuggestionsAdapter(owner = owner, addMoreClickListener = addMoreClickListener))
        //}

        id = consensusId
        //adapter.value?.clear()
        refreshDetails()
    }

    fun refreshDetails() {
        val consensusId = id ?: return

        repository.getConsensusDetail(consensusId)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion { showDetails(it, markDirty = false) }
            .start(compositeDisposable, ::showLoading)
    }

    fun updateConsensus() {
        val consensusDetail = currentConsensusDetail ?: return

        /*  repository.sendConsensus(consensusDetail)
              .with(AndroidSchedulerProvider())
              .subscribeRepoCompletion { showDetails(it, isRefresh = false) }
              .start(compositeDisposable, ::showLoading)*/

    }

    fun askForConsensusDeletion() {
        handle(ConsensusDeletionAsk())
    }

    fun deleteConsensus() {
        val consensusId = id ?: return

        repository.deleteConsensus(consensusId)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion(::showDeletion)
            .start(compositeDisposable, ::showLoading)
    }

    private fun showDeletion(result: RepoData<ResponseBody?>) {
        refresh.postValue(false)

        if (result.info.code == 200) {
            dirtyDataWatcher.markDirty(PersonalFragment.PERSONAL_DIRTY)
            dirtyDataWatcher.markDirty(HomeFragment.HOME_DIRTY)
            navigateTo(BACK)
        }

        Timber.e("woha $result")
    }

    private fun showDetails(result: RepoData<ConsensusResponse?>, markDirty: Boolean) {
        refresh.postValue(false)

        result.data?.let {

            currentConsensusDetail = it

            if (markDirty) {
                dirtyDataWatcher.markDirty(HomeFragment.HOME_DIRTY)
                dirtyDataWatcher.markDirty(PersonalFragment.PERSONAL_DIRTY)
            }

            repository.getConsensusSuggestions(it.id)
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
}

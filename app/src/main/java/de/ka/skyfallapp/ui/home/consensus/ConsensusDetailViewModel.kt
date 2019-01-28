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
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.ConsensusDetail
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.ui.home.HomeFragment

import de.ka.skyfallapp.ui.home.consensus.list.SuggestionsAdapter
import de.ka.skyfallapp.ui.home.consensus.newsuggestion.NewSuggestionFragment
import de.ka.skyfallapp.ui.personal.PersonalFragment
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.start


import de.ka.skyfallapp.utils.with
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
            Bundle().apply { putString(NewSuggestionFragment.CONS_ID_KEY, id) })
    }

    var id: String? = null

    fun layoutManager() = LinearLayoutManager(app.applicationContext)

    fun setupAdapterAndLoad(owner: LifecycleOwner, consensusId: String) {

        if (adapter.value == null) {
            adapter.postValue(SuggestionsAdapter(owner = owner, addMoreClickListener = addMoreClickListener))
        }

        if (consensusId != id) {
            id = consensusId
            refreshDetails()
        }
    }

    fun refreshDetails() {
        if (id == null) {
            return
        }

        repository.getConsensusDetail(id!!)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion { showDetails(it, isRefresh = true) }
            .start(compositeDisposable, ::showLoading)
    }

    fun updateConsensus() {

        val consensusDetail = ConsensusDetailManager.getDetail(id) ?: return

        repository.sendConsensus(consensusDetail)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion { showDetails(it, isRefresh = false) }
            .start(compositeDisposable, ::showLoading)

    }

    fun askForConsensusDeletion() {
        handle(ConsensusDeletionAsk())
    }

    fun deleteConsensus() {
        if (id == null) {
            return
        }

        repository.deleteConsensus(id!!)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion(::showDeletion)
            .start(compositeDisposable, ::showLoading)
    }

    private fun showDeletion(result: RepoData<ResponseBody?>) {
        refresh.postValue(false)

        if (result.info.code == 200) { // TODO handle better...
            dirtyDataWatcher.markDirty(PersonalFragment.PERSONAL_DIRTY)
            dirtyDataWatcher.markDirty(HomeFragment.HOME_DIRTY)
            navigateTo(-1)
        }

        Timber.e("woha $result")
    }

    private fun showDetails(result: RepoData<ConsensusDetail?>, isRefresh: Boolean) {
        refresh.postValue(false)

        result.data?.let {

            if (it.suggestions.isEmpty()) {
                blankVisibility.postValue(View.VISIBLE)
            } else {
                blankVisibility.postValue(View.GONE)

                if (isRefresh) {
                    dirtyDataWatcher.markDirty(HomeFragment.HOME_DIRTY)
                    dirtyDataWatcher.markDirty(PersonalFragment.PERSONAL_DIRTY)
                }
            }

            adapter.value?.insert(it.suggestions)
        }

        result.info.throwable?.let { showSnack(it.toString()) }
    }

    private fun showLoading() {
        refresh.postValue(true)
    }


    class ConsensusDeletionAsk


}

package de.ka.skyfallapp.ui.home.consensus

import android.app.Application
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.Consensus
import de.ka.skyfallapp.repo.api.ConsensusDetail
import de.ka.skyfallapp.repo.subscribeRepoCompletion

import de.ka.skyfallapp.ui.home.consensus.list.SuggestionsAdapter
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.start


import de.ka.skyfallapp.utils.with


class ConsensusDetailViewModel(app: Application) : BaseViewModel(app) {

    val swipeToRefreshListener = SwipeRefreshLayout.OnRefreshListener { refreshDetails() }
    var blankVisibility = MutableLiveData<Int>().apply { postValue(View.GONE) }
    val adapter = MutableLiveData<SuggestionsAdapter>()
    val refresh = MutableLiveData<Boolean>().apply { postValue(false) }

    private val addMoreClickListener = {
        navigateTo(R.id.action_consensusDetailFragment_to_newSuggestionFragment, false)
    }

    var id: String? = null

    fun layoutManager() = LinearLayoutManager(app.applicationContext)

    fun populateConsensusDetails(owner: LifecycleOwner, consensus: Consensus) {

        id = consensus.id

        adapter.postValue(SuggestionsAdapter(owner = owner, addMoreClickListener = addMoreClickListener)
            .apply { insert(consensus.suggestions) })
    }

    fun refreshDetails() {

        if (id == null) {
            return
        }

        repository.getConsensusDetail(id!!)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion(::showResult)
            .start(compositeDisposable, ::showLoading)
    }

    private fun showResult(result: RepoData<ConsensusDetail?>) {
        refresh.postValue(false)

        result.data?.let {

            if (it.suggestions.isEmpty()) {
                blankVisibility.postValue(View.VISIBLE)
            } else {
                blankVisibility.postValue(View.GONE)
            }

            adapter.value?.insert(it.suggestions)
        }

        result.info.throwable?.let { showSnack(it.toString()) }
    }

    private fun showLoading() {
        refresh.postValue(true)
    }


}

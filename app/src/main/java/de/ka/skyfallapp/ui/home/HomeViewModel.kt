package de.ka.skyfallapp.ui.home

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
import de.ka.skyfallapp.repo.api.ConsensusResponse
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.ui.home.consensusdetail.ConsensusDetailFragment
import de.ka.skyfallapp.ui.home.consensuslist.HomeAdapter
import de.ka.skyfallapp.ui.home.consensuslist.HomeItemViewModel

import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.start
import de.ka.skyfallapp.utils.with


class HomeViewModel(app: Application) : BaseViewModel(app) {

    val swipeToRefreshListener = SwipeRefreshLayout.OnRefreshListener { loadConsensus() }
    val blankVisibility = MutableLiveData<Int>().apply { postValue(View.GONE) }
    val refresh = MutableLiveData<Boolean>().apply { postValue(false) }
    val scrollTo = MutableLiveData<Int>().apply { postValue(0) }
    val adapter = MutableLiveData<HomeAdapter>()

    private val itemClickListener = { vm: HomeItemViewModel ->
        navigateTo(
            R.id.action_homeFragment_to_consensusDetailFragment,
            false,
            Bundle().apply { putString(ConsensusDetailFragment.CONS_ID_KEY, vm.item.id.toString()) }
        )
    }

    fun layoutManager() = LinearLayoutManager(app.applicationContext)

    fun setupAdapterAndLoad(owner: LifecycleOwner) {
        if (adapter.value == null) {
            adapter.postValue(HomeAdapter(owner))
            loadConsensus()
        }
    }

    fun loadConsensus() {
        repository.getConsensus()
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion(::showResult)
            .start(compositeDisposable, ::showLoading)
    }

    private fun showResult(result: RepoData<List<ConsensusResponse>?>) {
        refresh.postValue(false)

        result.data?.let {

            if (it.isEmpty()) {
                blankVisibility.postValue(View.VISIBLE)
            } else {
                blankVisibility.postValue(View.GONE)
            }

            adapter.value?.insert(it, itemClickListener)

            scrollTo.postValue(0)
        }

        result.info.throwable?.let { showSnack(it.message.toString()) }
    }

    private fun showLoading() {
        refresh.postValue(true)
    }
}

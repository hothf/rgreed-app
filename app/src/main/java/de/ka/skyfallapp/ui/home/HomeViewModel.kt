package de.ka.skyfallapp.ui.home

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner


import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.ConsensusResponse
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.ui.consensus.consensusdetail.ConsensusDetailFragment
import de.ka.skyfallapp.ui.consensus.consensuslist.HomeAdapter
import de.ka.skyfallapp.ui.consensus.consensuslist.ConsensusItemViewModel

import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.start
import de.ka.skyfallapp.utils.with
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator

class HomeViewModel(app: Application) : BaseViewModel(app) {

    val adapter = MutableLiveData<HomeAdapter>()
    val refresh = MutableLiveData<Boolean>().apply { postValue(false) }
    val blankVisibility = MutableLiveData<Int>().apply { postValue(View.GONE) }
    val swipeToRefreshListener = SwipeRefreshLayout.OnRefreshListener { loadConsensuses(true) }

    private var currentlyShown = 0
    private var lastReceivedCount = 0
    private var isLoading: Boolean = false

    init {
        startObserving()
    }

    private fun startObserving() {
        repository.profileManager.observableProfile
            .with(AndroidSchedulerProvider())
            .subscribeBy(onNext = { loadConsensuses(true) })
            .addTo(compositeDisposable)

        repository.consensusManager.observableConsensuses
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onNext = {

                    if (it.invalidate) {
                        loadConsensuses(true)
                        return@subscribeBy
                    }

                    if (it.list.isEmpty()) {
                        blankVisibility.postValue(View.VISIBLE)
                    } else {
                        blankVisibility.postValue(View.GONE)
                    }
                    adapter.value?.insert(it.list, itemClickListener)
                }
            )
            .addTo(compositeDisposable)
    }

    private val itemClickListener = { vm: ConsensusItemViewModel, view: View ->
        navigateTo(
            R.id.action_homeFragment_to_consensusDetailFragment,
            false,
            Bundle().apply { putString(ConsensusDetailFragment.CONS_ID_KEY, vm.item.id.toString()) },
            null,
            FragmentNavigatorExtras(view to view.transitionName)
        )
    }

    fun layoutManager() = LinearLayoutManager(app.applicationContext)

    fun setupAdapterAndLoad(owner: LifecycleOwner) {
        if (adapter.value == null) {
            adapter.postValue(HomeAdapter(owner))
            loadConsensuses(true)
        }
    }

    /**
     * Retrieves an on scroll listener for charging history loading.
     *
     * @return the scroll listener
     */
    fun getOnScrollListener(): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollVertically(1) && lastReceivedCount >= ITEMS_PER_LOAD) {
                    loadConsensuses(false)
                }
            }
        }
    }

    fun itemAnimator() = SlideInDownAnimator()

    /**
     * Loads all consensuses. This will be a paginated process, as long as [reset] is set to false.
     * Calling this with [reset] set to true will immediately cancel all requests and try to fetch from start.
     *
     * @param reset set to true to reset the current state of consensus pagination loading and force a fresh reload
     */
    private fun loadConsensuses(reset: Boolean) {
        if (reset) {
            currentlyShown = 0
            isLoading = false
            compositeDisposable.clear()
            startObserving()
        }

        if (isLoading) {
            return
        }

        repository.consensusManager.getConsensuses(reset, ITEMS_PER_LOAD, currentlyShown)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion(::handleListResult)
            .start(compositeDisposable, ::showLoading)
    }

    private fun handleListResult(result: RepoData<List<ConsensusResponse>?>) {
        refresh.postValue(false)
        isLoading = false

        result.data?.let {
            currentlyShown += it.size
            lastReceivedCount = it.size
        }

        result.info.throwable?.let { showSnack(it.message.toString()) }
    }

    private fun showLoading() {
        isLoading = true
        refresh.postValue(true)
    }

    companion object {
        const val ITEMS_PER_LOAD = 10
    }
}

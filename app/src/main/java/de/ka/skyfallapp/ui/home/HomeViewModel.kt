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
import de.ka.skyfallapp.ui.home.consensusdetail.ConsensusDetailFragment
import de.ka.skyfallapp.ui.home.consensuslist.HomeAdapter
import de.ka.skyfallapp.ui.home.consensuslist.HomeItemViewModel

import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.start
import de.ka.skyfallapp.utils.with
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber


class HomeViewModel(app: Application) : BaseViewModel(app) {

    val adapter = MutableLiveData<HomeAdapter>()
    val scrollTo = MutableLiveData<Int>().apply { postValue(0) }
    val refresh = MutableLiveData<Boolean>().apply { postValue(false) }
    val blankVisibility = MutableLiveData<Int>().apply { postValue(View.GONE) }
    val swipeToRefreshListener = SwipeRefreshLayout.OnRefreshListener { loadConsensus(true) }

    private var currentlyShown = 0
    private var lastReceivedCount = 0
    private var isLoading: Boolean = false

    init {
        watchForDirtyData()
    }

    private fun watchForDirtyData() {
        dirtyDataWatcher.subject
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onNext = {
                    if (it.key == HOME_DATA) {
                        Timber.e("Dirty: ${it.key}")
                        loadConsensus(it.id == null, it.id)
                    }
                }
            )
            .addTo(compositeDisposable)
    }

    private val itemClickListener = { vm: HomeItemViewModel, view: View ->
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
            loadConsensus(true)
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
                    loadConsensus(false)
                }
            }
        }
    }

    /**
     * Loads consensuses. This will be a paginated process, as long as [reset] is set to false.
     * Calling this with [reset] set to true will immediately cancel all requests and try to fetch from start.
     *
     *
     * @param reset set to true to reset the current state of consensus pagination loading and force a fresh reload
     */
    fun loadConsensus(reset: Boolean, id: Int? = null) {

        if (reset) {
            currentlyShown = 0
            isLoading = false
            compositeDisposable.clear()
            watchForDirtyData()
        }

        if (isLoading) {
            return
        }

        if (id != null) {
            repository.getConsensusDetail(id)
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion { showSingleResult(it, id) }
                .start(compositeDisposable, ::showLoading)
        } else {
            repository.getConsensus(ITEMS_PER_LOAD, currentlyShown)
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion { showResult(reset, it) }
                .start(compositeDisposable, ::showLoading)
        }
    }

    private fun showSingleResult(result: RepoData<ConsensusResponse?>, id: Int) {
        refresh.postValue(false)

        result.data?.let {
            adapter.value?.update(it, itemClickListener)
            //TODO Consider  add a auto scroll to the position of the updated item if reset?
        }

        if (result.info.code == 404) {
            adapter.value?.remove(id)
        }

        isLoading = false
    }

    private fun showResult(reset: Boolean, result: RepoData<List<ConsensusResponse>?>) {
        refresh.postValue(false)

        result.data?.let {

            if (it.isEmpty()) {
                blankVisibility.postValue(View.VISIBLE)
            } else {
                blankVisibility.postValue(View.GONE)
            }

            currentlyShown += it.size
            lastReceivedCount = it.size

            adapter.value?.insert(!reset, it, itemClickListener)

        }

        isLoading = false

        result.info.throwable?.let { showSnack(it.message.toString()) }
    }

    private fun showLoading() {
        isLoading = true
        refresh.postValue(true)
    }

    companion object {
        const val ITEMS_PER_LOAD = 10
        const val HOME_DATA = "HomeViewModelData"
    }
}

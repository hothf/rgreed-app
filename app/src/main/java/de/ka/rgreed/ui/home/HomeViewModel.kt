package de.ka.rgreed.ui.home

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner


import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.ka.rgreed.R
import de.ka.rgreed.base.BaseViewModel
import de.ka.rgreed.base.events.AnimType
import de.ka.rgreed.repo.api.models.ConsensusResponse
import de.ka.rgreed.repo.subscribeRepoCompletion
import de.ka.rgreed.ui.consensus.consensusdetail.ConsensusDetailFragment
import de.ka.rgreed.ui.consensus.consensuslist.ConsensusAdapter
import de.ka.rgreed.ui.consensus.consensuslist.ConsensusItemViewModel

import de.ka.rgreed.utils.AndroidSchedulerProvider
import de.ka.rgreed.ui.consensus.consensuslist.ConsensusItemDecoration
import de.ka.rgreed.utils.start
import de.ka.rgreed.utils.with
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator

/**
 * Responsible for displaying the list of all [ConsensusResponse]s this app has to offer.
 */
class HomeViewModel(app: Application) : BaseViewModel(app) {

    private var currentlyShown = 0
    private var isLoading: Boolean = false

    val adapter = MutableLiveData<ConsensusAdapter>()
    val refresh = MutableLiveData<Boolean>().apply { value = false }
    val blankVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val swipeToRefreshListener = SwipeRefreshLayout.OnRefreshListener { loadConsensuses(true) }
    val itemDecoration = ConsensusItemDecoration(app.resources.getDimensionPixelSize(R.dimen.default_16))
    private val itemClickListener = { vm: ConsensusItemViewModel, view: View ->
        navigateTo(
            R.id.action_homeFragment_to_consensusDetailFragment,
            false,
            Bundle().apply { putString(ConsensusDetailFragment.CONS_ID_KEY, vm.item.id.toString()) },
            null,
            FragmentNavigatorExtras(view to view.transitionName)
        )
    }

    init {
        startObserving()
    }

    fun onSettingsClick() {
        navigateTo(R.id.settingsFragment, animType = AnimType.MODAL)
    }

    private fun startObserving() {
        repository.profileManager.observableLoginLogoutProfile
            .with(AndroidSchedulerProvider())
            .subscribeBy(onNext = { loadConsensuses(true) }, onError = {})
            .addTo(compositeDisposable)

        repository.consensusManager.observableConsensuses
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onNext = { result ->
                    adapter.value?.let {
                        val updateOnly = if (result.isFiltered) true else result.update

                        val removedOrAddedCount = it.removeAddOrUpdate(
                            result.list,
                            itemClickListener,
                            result.remove,
                            updateOnly,
                            result.addToTop
                        )
                        currentlyShown += removedOrAddedCount

                        if (it.isEmpty) {
                            blankVisibility.postValue(View.VISIBLE)
                        } else {
                            blankVisibility.postValue(View.GONE)
                        }
                    }
                }, onError = ::handleGeneralError
            )
            .addTo(compositeDisposable)
    }

    fun layoutManager() = LinearLayoutManager(app.applicationContext)

    /**
     * Sets up the view, if not already done.
     *
     * @param owner the lifecycle owner to keep the data in sync with the lifecycle
     */
    fun setupAdapterAndLoad(owner: LifecycleOwner) {
        if (adapter.value == null) {
            adapter.postValue(ConsensusAdapter(owner))
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

                if (!recyclerView.canScrollVertically(1)) {
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
            adapter.value?.markForDisposition()
            startObserving()
        }

        if (isLoading) {
            return
        }

        repository.consensusManager.getConsensuses(ITEMS_PER_LOAD, currentlyShown)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion { hideLoading() }
            .start(compositeDisposable, ::showLoading)
    }

    private fun hideLoading() {
        refresh.postValue(false)
        isLoading = false
    }

    private fun showLoading() {
        isLoading = true
        refresh.postValue(true)
    }

    companion object {
        const val ITEMS_PER_LOAD = 10
    }
}

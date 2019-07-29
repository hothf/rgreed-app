package de.ka.rgreed.ui.search

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.LinearLayoutManager
import de.ka.rgreed.R
import de.ka.rgreed.base.BaseViewModel
import de.ka.rgreed.repo.RepoData
import de.ka.rgreed.repo.api.models.ConsensusResponse
import de.ka.rgreed.repo.subscribeRepoCompletion
import de.ka.rgreed.ui.consensus.consensusdetail.ConsensusDetailFragment
import de.ka.rgreed.ui.consensus.consensuslist.ConsensusItemDecoration
import de.ka.rgreed.ui.consensus.consensuslist.ConsensusItemViewModel
import de.ka.rgreed.ui.consensus.consensuslist.ConsensusAdapter
import de.ka.rgreed.utils.*
import de.ka.rgreed.utils.NavigationUtils.BACK
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

/**
 * Allows for detail searches.
 */
class SearchDetailViewModel(app: Application) : BaseViewModel(app) {

    var currentSearch = ""

    val adapter = MutableLiveData<ConsensusAdapter>()
    val getDoneListener = ViewUtils.TextDoneListener { search() }
    val searchText = MutableLiveData<String>().apply { value = "" }
    val searchTextSelection = MutableLiveData<Int>().apply { value = 0 }
    val buttonEnabled = MutableLiveData<Boolean>().apply { value = false }
    val header = app.applicationContext.getString(R.string.search_detail_head)
    val loadingVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val blankVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val contentVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val buttonVisibility = MutableLiveData<Int>().apply { value = View.VISIBLE }
    val getSearchChangeListener = ViewUtils.TextChangeListener { updateSearchWith(it) }
    val itemDecoration = ConsensusItemDecoration(
        app.resources.getDimensionPixelSize(R.dimen.default_16),
        app.resources.getDimensionPixelSize(R.dimen.default_8)
    )

    private val itemClickListener = { vm: ConsensusItemViewModel, view: View ->
        view.closeAttachedKeyboard()
        navigateTo(
            R.id.action_searchDetailFragment_to_consensusDetailFragment,
            false,
            Bundle().apply { putString(ConsensusDetailFragment.CONS_ID_KEY, vm.item.id.toString()) },
            null,
            FragmentNavigatorExtras(view to view.transitionName)
        )
    }

    init {
        repository.consensusManager.searchManager.observableSearchResults
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onError = ::handleGeneralError,
                onNext = {
                    adapter.value?.overwriteList(it, itemClickListener)
                    if (it.isEmpty()) {
                        blankVisibility.postValue(View.VISIBLE)
                        contentVisibility.postValue(View.GONE)
                    } else {
                        blankVisibility.postValue(View.GONE)
                        contentVisibility.postValue(View.VISIBLE)
                    }
                }
            )
            .addTo(compositeDisposable)

        repository.consensusManager.observableConsensuses
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onNext = { result ->
                    adapter.value?.let {
                        if (result.update) {
                            it.removeAddOrUpdate(
                                result.list,
                                itemClickListener,
                                remove = false,
                                onlyUpdate = true,
                                addToTop = false
                            )
                        }
                    }
                }, onError = ::handleGeneralError
            )
            .addTo(compositeDisposable)
    }

    /**
     * Sets up the viewmodel data, if not already done.
     *
     * @param owner the lifecycle owner to keep the data in sync with the lifecycle

     */
    fun setupAdapter(owner: LifecycleOwner) {
        if (adapter.value == null) {
            adapter.postValue(ConsensusAdapter(owner))
        } else {
            adapter.value?.owner = owner
        }
    }

    /**
     * Sets up the arguments for first use of the viewmodel.
     *  @param query a optional query
     * @param new a flag indicating, that this should be a newly initialized search result display
     */
    fun setupArguments(query: String?, new: Boolean?) {
        if (!query.isNullOrBlank()) {
            updateSearchWith(query)
            search()
        } else if (new != null && new == true) {
            updateSearchWith("")
            resetSearch()
        }
    }

    /**
     * Resets the currently displayed search.
     */
    private fun resetSearch() {
        repository.consensusManager.searchManager.clearSearchResults()
    }

    /**
     * Updates the search with the given string query.
     *
     * @param it the string search query
     * @param skipSelection set to true to skip selection settings
     */
    private fun updateSearchWith(it: String) {
        currentSearch = it
        searchText.value = it
        searchTextSelection.value = it.length
        buttonEnabled.postValue(it.isNotBlank())
    }

    fun layoutManager() = LinearLayoutManager(app.applicationContext)

    fun itemAnimator() = SlideInUpAnimator()

    fun search() {
        if (currentSearch.isBlank()) {
            return
        }
        repository.consensusManager.searchManager.search(currentSearch)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion(::handleSearchResult)
            .start(compositeDisposable, ::showLoading)
    }

    fun onBack() {
        navigateTo(BACK)
    }

    private fun handleSearchResult(result: RepoData<List<ConsensusResponse>?>) {
        hideLoading()
    }

    private fun showLoading() {
        loadingVisibility.postValue(View.VISIBLE)
        buttonVisibility.postValue(View.GONE)
    }

    private fun hideLoading() {
        loadingVisibility.postValue(View.GONE)
        buttonVisibility.postValue(View.VISIBLE)
    }

}

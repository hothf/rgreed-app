package de.ka.skyfallapp.ui.search

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.LinearLayoutManager
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.base.events.SnackType
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.models.ConsensusResponse
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.ui.consensus.consensusdetail.ConsensusDetailFragment
import de.ka.skyfallapp.ui.consensus.consensuslist.ConsensusItemViewModel
import de.ka.skyfallapp.ui.consensus.consensuslist.HomeAdapter
import de.ka.skyfallapp.ui.personal.PersonalViewModel
import de.ka.skyfallapp.utils.*
import de.ka.skyfallapp.utils.NavigationUtils.BACK
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

/**
 * Allows for detail searches.
 */
class SearchDetailViewModel(app: Application) : BaseViewModel(app) {

    var currentSearch = ""

    val adapter = MutableLiveData<HomeAdapter>()
    val getDoneListener = ViewUtils.TextDoneListener { search() }
    val searchText = MutableLiveData<String>().apply { value = "" }
    val searchTextSelection = MutableLiveData<Int>().apply { value = 0 }
    val buttonEnabled = MutableLiveData<Boolean>().apply { value = false }
    val header = app.applicationContext.getString(R.string.search_detail_head)
    val loadingVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val blankVisibility = MutableLiveData<Int>().apply { postValue(View.GONE) }
    val buttonVisibility = MutableLiveData<Int>().apply { value = View.VISIBLE }
    val getSearchChangeListener = ViewUtils.TextChangeListener(::updateSearchWith)

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
    private val shareListener = { id: String -> handle(PersonalViewModel.SharePersonalConsensus(id)) }

    init {
        repository.consensusManager.searchManager.observableSearchResults
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onNext = {
                    adapter.value?.insert(it, itemClickListener, shareListener)
                    if (it.isEmpty()) {
                        blankVisibility.postValue(View.VISIBLE)
                    } else {
                        blankVisibility.postValue(View.GONE)
                    }
                }
            )
            .addTo(compositeDisposable)
    }

    /**
     * Sets up the view, if not already done.
     *
     * @param owner the lifecycle owner to keep the data in sync with the lifecycle
     * @param query a optional query
     */
    fun setup(owner: LifecycleOwner, query: String?) {
        if (adapter.value == null) {
            adapter.postValue(HomeAdapter(owner))
        }
        if (query != null) {
            updateSearchWith(query)
            search()
        }
    }

    /**
     * Updates the search with the given string query.
     *
     * @param it the string search query
     */
    private fun updateSearchWith(it: String) {
        repository.consensusManager.searchManager.notifySearchQueryChanged(it)
        currentSearch = it
        searchText.postValue(it)
        searchTextSelection.postValue(it.length)
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

        if (result.data == null) {
            showSnack(result.info.throwable.toString(), SnackType.WARNING)
        }
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
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
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.NavigationUtils.BACK
import de.ka.skyfallapp.utils.ViewUtils
import de.ka.skyfallapp.utils.start
import de.ka.skyfallapp.utils.with
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
    val loadingVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val blankVisibility = MutableLiveData<Int>().apply { postValue(View.GONE) }
    val buttonVisibility = MutableLiveData<Int>().apply { value = View.VISIBLE }
    val header = app.applicationContext.getString(R.string.search_detail_head)
    val buttonEnabled = MutableLiveData<Boolean>().apply { value = false }
    val getSearchChangeListener = ViewUtils.TextChangeListener {
        repository.consensusManager.searchManager.updateSearchQuery(it)
        currentSearch = it
        searchText.postValue(it)
        searchTextSelection.postValue(it.length)
        buttonEnabled.postValue(it.isNotBlank())
    }
    private val itemClickListener = { vm: ConsensusItemViewModel, view: View ->
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
     */
    fun setup(owner: LifecycleOwner) {
        if (adapter.value == null) {
            adapter.postValue(HomeAdapter(owner))
        }
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

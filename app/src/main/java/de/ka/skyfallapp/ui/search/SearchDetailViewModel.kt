package de.ka.skyfallapp.ui.search

import android.app.Application
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.base.events.SnackType
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.models.ConsensusResponse
import de.ka.skyfallapp.ui.consensus.consensuslist.HomeAdapter
import de.ka.skyfallapp.utils.NavigationUtils.BACK
import de.ka.skyfallapp.utils.ViewUtils
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
    val buttonVisibility = MutableLiveData<Int>().apply { value = View.VISIBLE }
    val header = app.applicationContext.getString(R.string.search_detail_head)
    val getSearchChangeListener = ViewUtils.TextChangeListener {
        currentSearch = it
        searchText.postValue(it)
        searchTextSelection.postValue(it.length)
    }

    init {


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

        loadingVisibility.postValue(View.VISIBLE)
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

    private fun hideLoading() {
        loadingVisibility.postValue(View.GONE)

        loadingVisibility.postValue(View.GONE)
        buttonVisibility.postValue(View.VISIBLE)
    }

}

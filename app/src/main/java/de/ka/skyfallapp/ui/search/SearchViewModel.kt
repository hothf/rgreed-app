package de.ka.skyfallapp.ui.search

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.ui.search.history.SearchHistoryAdapter
import de.ka.skyfallapp.ui.search.history.SearchHistoryItemViewModel
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.with
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

/**
 * Allows for searches.
 */
class SearchViewModel(app: Application) : BaseViewModel(app) {

    val adapter = MutableLiveData<SearchHistoryAdapter>()
    val searchText = MutableLiveData<String>().apply { value = "" }
    val blankVisibility = MutableLiveData<Int>().apply { View.GONE }

    private val historyClickListener = { item: SearchHistoryItemViewModel ->
        navigateTo(
            R.id.searchDetailFragment,
            args = Bundle().apply { putString(SearchDetailFragment.KEY_SEARCH, item.text) })
    }

    init {
        repository.consensusManager.searchManager.observableLastSearchQuery
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onNext = {
                    searchText.postValue(it)
                }
            )
            .addTo(compositeDisposable)

        repository.consensusManager.searchManager.observableSearchHistory
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onNext = { history ->
                    adapter.value?.insert(history, historyClickListener)

                    if (history.isEmpty()) {
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
            adapter.postValue(SearchHistoryAdapter(owner))
            repository.consensusManager.searchManager.loadSearchHistory()
        }
    }

    fun layoutManager() = LinearLayoutManager(app.applicationContext)

    fun itemAnimator() = SlideInUpAnimator()

    fun onSettingsClick() {
        navigateTo(R.id.settingsFragment)
    }

    fun onSearchClick() {
        navigateTo(R.id.searchDetailFragment)
    }
}

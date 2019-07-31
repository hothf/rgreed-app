package de.ka.rgreed.ui.search

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import de.ka.rgreed.R
import de.ka.rgreed.base.BaseViewModel
import de.ka.rgreed.base.events.AnimType
import de.ka.rgreed.ui.search.history.SearchHistoryAdapter
import de.ka.rgreed.ui.search.history.SearchHistoryItemViewModel
import de.ka.rgreed.utils.AndroidSchedulerProvider
import de.ka.rgreed.utils.with
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator

/**
 * Allows for searches.
 */
class SearchViewModel(app: Application) : BaseViewModel(app) {

    val adapter = SearchHistoryAdapter()
    val blankVisibility = MutableLiveData<Int>().apply { View.GONE }

    private val historyClickListener = { item: SearchHistoryItemViewModel ->
        navigateTo(
            R.id.searchDetailFragment,
            args = Bundle().apply { putString(SearchDetailFragment.KEY_SEARCH, item.text) },
            animType = AnimType.NONE
        )
    }
    private val deleteClickListener = { item: SearchHistoryItemViewModel ->
        repository.consensusManager.searchManager.deleteSearchHistory(item.text)
    }

    init {
        repository.consensusManager.searchManager.observableSearchHistory
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onError = ::handleGeneralError,
                onNext = { history ->
                    adapter.overwriteList(history, historyClickListener, deleteClickListener)

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
     */
    fun setup() {
        repository.consensusManager.searchManager.loadSearchHistory()
    }

    fun layoutManager() = LinearLayoutManager(app.applicationContext)

    fun itemAnimator() = SlideInLeftAnimator()

    fun onSettingsClick() {
        navigateTo(R.id.settingsFragment, animType = AnimType.MODAL)
    }

    fun onSearchClick() {
        navigateTo(
            R.id.searchDetailFragment,
            args = Bundle().apply { putBoolean(SearchDetailFragment.KEY_NEW, true) },
            animType = AnimType.NONE
        )
    }
}

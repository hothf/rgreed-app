package de.ka.skyfallapp.ui.search

import android.app.Application
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.with
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy

/**
 * Allows for searches.
 */
class SearchViewModel(app: Application) : BaseViewModel(app) {

    val searchText = MutableLiveData<String>().apply { value = "" }

    init {
        repository.consensusManager.searchManager.observableLastSearchQuery
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onNext = {
                    searchText.postValue(it)
                }
            )
            .addTo(compositeDisposable)
    }

    fun onSettingsClick() {
        navigateTo(R.id.settingsFragment)
    }

    fun onSearchClick() {
        navigateTo(R.id.searchDetailFragment)
    }
}

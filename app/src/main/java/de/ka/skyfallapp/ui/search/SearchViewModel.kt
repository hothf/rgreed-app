package de.ka.skyfallapp.ui.search

import android.app.Application
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel

/**
 * Allows for searches.
 */
class SearchViewModel(app: Application) : BaseViewModel(app) {

    fun onSettingsClick() {
        navigateTo(R.id.settingsFragment)
    }

    fun onSearchClick() {
        navigateTo(R.id.searchDetailFragment)
    }
}

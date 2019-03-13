package de.ka.skyfallapp.ui.search

import android.app.Application
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.utils.NavigationUtils.BACK

/**
 * Allows for detail searches.
 */
class SearchDetailViewModel(app: Application) : BaseViewModel(app) {

    val header = app.applicationContext.getString(R.string.search_detail_head)

    fun onBack() {
        navigateTo(BACK)
    }

}

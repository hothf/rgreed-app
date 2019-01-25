package de.ka.skyfallapp.ui.settings

import android.app.Application
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel

class SettingsViewModel(app: Application) : BaseViewModel(app) {


    fun onProfileClicked() {
        navigateTo(R.id.profileFragment)
    }
}

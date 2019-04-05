package de.ka.rgreed.ui.settings

import android.app.Application
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.lifecycle.MutableLiveData
import de.ka.rgreed.BuildConfig
import de.ka.rgreed.R
import de.ka.rgreed.base.BaseViewModel
import de.ka.rgreed.base.events.AnimType
import de.ka.rgreed.repo.Profile
import de.ka.rgreed.ui.profile.ProfileFragment
import de.ka.rgreed.utils.AndroidSchedulerProvider
import de.ka.rgreed.utils.NavigationUtils.BACK
import de.ka.rgreed.utils.with
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy

/**
 * Allows for app specific setting adjustments. Listens for profile changes to display the current profile.
 */
class SettingsViewModel(app: Application) : BaseViewModel(app) {

    val loginText = app.getString(R.string.settings_login)
    val profileText =
        MutableLiveData<String>().apply { value = repository.profileManager.currentProfile.username ?: loginText }
    val versionText = "${BuildConfig.VERSION_NAME}.${BuildConfig.BUILD_TYPE}"
    val isPushEnabled =
        MutableLiveData<Boolean>().apply { value = repository.profileManager.currentProfile.isPushEnabled }
    val isPushEnabledVisibility =
        MutableLiveData<Int>().apply {
            value = if (repository.profileManager.currentProfile.username != null) View.VISIBLE else View.GONE
        }
    val header = MutableLiveData<String>().apply { value = app.getString(R.string.settings_head) }
    val pushCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, checked ->
        repository.profileManager.updateProfile { this.isPushEnabled = checked }
    }

    init {
        repository.profileManager.observableProfile
            .with(AndroidSchedulerProvider())
            .subscribeBy(onNext = ::handleProfileChange, onError = {})
            .addTo(compositeDisposable)
    }

    fun onBack() {
        navigateTo(BACK)
    }

    private fun handleProfileChange(profile: Profile) {
        profileText.postValue(profile.username ?: loginText)
        isPushEnabledVisibility.postValue(if (profile.username != null) View.VISIBLE else View.GONE)
        isPushEnabled.postValue(profile.isPushEnabled)
    }

    /**
     * Called on a click of the profile and goes to the [ProfileFragment].
     */
    fun onProfileClicked() {
        navigateTo(
            R.id.profileFragment,
            args = Bundle().apply { putBoolean(ProfileFragment.NEW_KEY, true) },
            animType = AnimType.MODAL
        )
    }
}

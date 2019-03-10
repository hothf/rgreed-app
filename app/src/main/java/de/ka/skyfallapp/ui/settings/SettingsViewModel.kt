package de.ka.skyfallapp.ui.settings

import android.app.Application
import android.os.Bundle
import android.widget.CompoundButton
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.BuildConfig
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.repo.Profile
import de.ka.skyfallapp.ui.profile.ProfileFragment
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.with
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy

class SettingsViewModel(app: Application) : BaseViewModel(app) {

    var pushEnabled = true

    val loginText = app.getString(R.string.settings_login)
    val isPushEnabled = MutableLiveData<Boolean>().apply { value = pushEnabled }
    val profileText = MutableLiveData<String>().apply { value = loginText }
    val versionText = "${BuildConfig.VERSION_NAME}.${BuildConfig.BUILD_TYPE}"
    val pushCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, checked ->
        pushEnabled = checked
        isPushEnabled.postValue(checked)
    }

    init {
        changeToProfile(repository.profileManager.currentProfile)

        repository.profileManager.observableProfile
            .with(AndroidSchedulerProvider())
            .subscribeBy(onNext = { changeToProfile(it) })
            .addTo(compositeDisposable)
    }

    private fun changeToProfile(profile: Profile?) {
        profileText.postValue(profile?.username ?: loginText)
    }

    fun onProfileClicked() {
        navigateTo(R.id.profileFragment, args = Bundle().apply { putBoolean(ProfileFragment.NEW_KEY, true) })
    }
}

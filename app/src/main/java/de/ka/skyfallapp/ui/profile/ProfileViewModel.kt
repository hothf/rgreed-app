package de.ka.skyfallapp.ui.profile

import android.app.Application
import android.view.View
import androidx.databinding.adapters.TextViewBindingAdapter
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.base.events.BACK
import de.ka.skyfallapp.repo.Profile
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.*
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.ViewUtils
import de.ka.skyfallapp.utils.start
import de.ka.skyfallapp.utils.with
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import org.koin.android.ext.android.get
import timber.log.Timber

class ProfileViewModel(app: Application) : BaseViewModel(app) {

    var usernameText = ""
    var passwordText = ""

    val profileText = MutableLiveData<String>().apply { value = "" }
    val controlsEnabled = MutableLiveData<Boolean>().apply { value = true }
    val loginVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val logoutVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val loadingVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val header = MutableLiveData<String>().apply { value = app.getString(R.string.profile_head) }
    val getLoginUserNameChangedListener = ViewUtils.TextChangeListener { usernameText = it }
    val getLoginPasswordChangedListener = ViewUtils.TextChangeListener { passwordText = it }

    init {
        handleProfileChange(repository.profileManager.currentProfile)

        repository.profileManager.observableProfile
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onComplete = {
                    Timber.e("Profile subscription complete")
                },
                onError = {
                    Timber.e("Profile subscription error")
                },
                onNext = { profile: Profile ->
                    Timber.e("Profile subscription onNext $profile")
                    handleProfileChange(profile)
                }
            )
            .addTo(compositeDisposable)
    }

    /**
     * Handles a profile change.
     */
    private fun handleProfileChange(profile: Profile?) {
        if (profile?.username != null) {
            loginVisibility.postValue(View.GONE)
            logoutVisibility.postValue(View.VISIBLE)
            profileText.postValue(profile.username)
        } else {
            loginVisibility.postValue(View.VISIBLE)
            logoutVisibility.postValue(View.GONE)
            profileText.postValue("")
        }
    }

    fun onBack() {
        navigateTo(BACK)
    }

    fun logout() {
        repository.logout()
    }

    fun login() {
        repository.login(LoginBody(usernameText, passwordText))
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion(::handleLogin)
            .start(compositeDisposable, ::showLoading)

    }

    private fun handleLogin(result: RepoData<LoginResponse?>) {
        controlsEnabled.postValue(true)
        loadingVisibility.postValue(View.GONE)

        if (result.data != null) {
            navigateTo(BACK)
            return
        }

        result.info.throwable?.let {
            showSnack(it.message.toString())
        }

    }

    private fun showLoading() {
        controlsEnabled.postValue(false)
        loadingVisibility.postValue(View.VISIBLE)
    }
}

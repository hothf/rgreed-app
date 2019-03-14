package de.ka.skyfallapp.ui.profile

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.base.events.AnimType
import de.ka.skyfallapp.repo.Profile
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.models.LoginBody
import de.ka.skyfallapp.repo.api.models.LoginResponse
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.ui.profile.register.RegisterFragment
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.NavigationUtils.BACK
import de.ka.skyfallapp.utils.ViewUtils
import de.ka.skyfallapp.utils.start
import de.ka.skyfallapp.utils.with
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy

/**
 * The profile view model allows for logging out, logging in and going to the registration.
 */
class ProfileViewModel(app: Application) : BaseViewModel(app) {

    /**
     * The current state of the profile view model.
     */
    enum class State {
        LOGIN,
        LOGOUT
    }

    private var loginUserName = ""
    private var loginPassword = ""
    private var currentState = State.LOGIN

    val getDoneListener = ViewUtils.TextDoneListener()
    val headerText = app.getString(R.string.profile_head)
    val profileText = MutableLiveData<String>().apply { value = "" }
    val usernameText = MutableLiveData<String>().apply { value = "" }
    val passwordText = MutableLiveData<String>().apply { value = "" }
    val usernameSelection = MutableLiveData<Int>().apply { value = 0 }
    val passwordSelection = MutableLiveData<Int>().apply { value = 0 }
    val controlsEnabled = MutableLiveData<Boolean>().apply { value = true }
    val loginVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val logoutVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val loadingVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val buttonVisibility = MutableLiveData<Int>().apply { value = View.VISIBLE }
    val toRegisterVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val getLoginUserNameChangedListener = ViewUtils.TextChangeListener {
        loginUserName = it
        usernameText.postValue(it)
        usernameSelection.postValue(it.length)
    }
    val getLoginPasswordChangedListener = ViewUtils.TextChangeListener {
        loginPassword = it
        passwordText.postValue(it)
        passwordSelection.postValue(it.length)
    }

    init {
        handleProfileChange(repository.profileManager.currentProfile)

        repository.profileManager.observableProfile
            .with(AndroidSchedulerProvider())
            .subscribeBy(onNext = ::handleProfileChange)
            .addTo(compositeDisposable)
    }

    /**
     * Sets up the view for a new login/logout process, clearing all data.
     */
    fun setupNew() {
        loginUserName = ""
        loginPassword = ""

        updateTextViews()
    }

    private fun updateTextViews() {
        usernameText.postValue(loginUserName)
        usernameSelection.postValue(loginUserName.length)
        passwordText.postValue(loginPassword)
        passwordSelection.postValue(loginPassword.length)
    }

    /**
     * Goes back.
     */
    fun onBack() {
        navigateTo(BACK)
    }

    /**
     * Handles a profile change.
     */
    private fun handleProfileChange(profile: Profile?) {
        if (profile?.username != null) {
            profileText.postValue(profile.username)

            currentState = State.LOGOUT
            updateToState()
        } else {
            currentState = State.LOGIN
            updateToState()
        }
    }

    /**
     * Goes to the registration.
     */
    fun onToRegisterClick() {
        navigateTo(
            R.id.registerFragment,
            args = Bundle().apply { putBoolean(RegisterFragment.NEW_KEY, true) },
            animType = AnimType.MODAL
        )
    }

    private fun updateToState() {
        loginUserName = ""
        loginPassword = ""

        when (currentState) {
            State.LOGIN -> {
                loginVisibility.postValue(View.VISIBLE)
                logoutVisibility.postValue(View.GONE)

                toRegisterVisibility.postValue(View.VISIBLE)
            }
            State.LOGOUT -> {
                loginVisibility.postValue(View.GONE)
                logoutVisibility.postValue(View.VISIBLE)

                toRegisterVisibility.postValue(View.GONE)
            }
        }
    }

    /**
     * Immediately logs out the user.
     */
    fun logout() {
        repository.logout()
    }

    /**
     * Requests to log in the user.
     */
    fun login() {
        repository.login(LoginBody(loginUserName, loginPassword))
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion(::handleLogin)
            .start(compositeDisposable, ::showLoading)
    }

    private fun handleLogin(result: RepoData<LoginResponse?>) {
        hideLoading()

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
        buttonVisibility.postValue(View.GONE)
        loadingVisibility.postValue(View.VISIBLE)
    }

    private fun hideLoading() {
        controlsEnabled.postValue(true)
        buttonVisibility.postValue(View.VISIBLE)
        loadingVisibility.postValue(View.GONE)
    }
}

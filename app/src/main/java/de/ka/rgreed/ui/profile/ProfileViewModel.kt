package de.ka.rgreed.ui.profile

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import de.ka.rgreed.R
import de.ka.rgreed.base.BaseViewModel
import de.ka.rgreed.base.events.AnimType
import de.ka.rgreed.repo.Profile
import de.ka.rgreed.repo.RepoData
import de.ka.rgreed.repo.api.models.LoginBody
import de.ka.rgreed.repo.api.models.LoginResponse
import de.ka.rgreed.repo.subscribeRepoCompletion
import de.ka.rgreed.ui.profile.register.RegisterFragment
import de.ka.rgreed.utils.*
import de.ka.rgreed.utils.NavigationUtils.BACK
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import de.ka.rgreed.utils.ValidationRules.*

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

    val headerText = app.getString(R.string.profile_head)
    val getDoneListener = ViewUtils.TextDoneListener { login() }
    val profileText = MutableLiveData<String>().apply { value = "" }
    val usernameText = MutableLiveData<String>().apply { value = "" }
    val passwordText = MutableLiveData<String>().apply { value = "" }
    val usernameSelection = MutableLiveData<Int>().apply { value = 0 }
    val passwordSelection = MutableLiveData<Int>().apply { value = 0 }
    val passwordError = MutableLiveData<String>().apply { value = "" }
    val usernameError = MutableLiveData<String>().apply { value = "" }
    val controlsEnabled = MutableLiveData<Boolean>().apply { value = true }
    val loginVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val logoutVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val loadingVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val buttonVisibility = MutableLiveData<Int>().apply { value = View.VISIBLE }
    val toRegisterVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val getLoginUserNameChangedListener = ViewUtils.TextChangeListener {
        loginUserName = it
        usernameText.postValue(it)
        usernameError.postValue("")
    }
    val getLoginPasswordChangedListener = ViewUtils.TextChangeListener {
        loginPassword = it
        passwordText.postValue(it)
        passwordError.postValue("")
    }

    init {
        handleProfileChange(repository.profileManager.currentProfile)

        repository.profileManager.observableLoginLogoutProfile
            .with(AndroidSchedulerProvider())
            .subscribeBy(onNext = ::handleProfileChange, onError = {})
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
        passwordError.postValue("")
        usernameError.postValue("")
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
        when (currentState) {
            State.LOGIN -> {
                setupNew()

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
        // perform a quick low level validation
        InputValidator(
            listOf(
                ValidatorInput(loginUserName, usernameError, listOf(NOT_EMPTY)),
                ValidatorInput(loginPassword, passwordError, listOf(NOT_EMPTY))
            )
        ).apply {
            if (!validateAll(app)) {
                return
            }
        }

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

        result.repoError?.errors?.forEach {
            when (it.parameter) {
                "username" -> usernameError.postValue(it.localizedMessage(app))
                "password" -> passwordError.postValue(it.localizedMessage(app))
            }
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

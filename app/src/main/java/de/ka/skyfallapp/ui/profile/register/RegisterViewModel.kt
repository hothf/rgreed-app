package de.ka.skyfallapp.ui.profile.register

import android.app.Application
import android.view.View
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.base.events.SnackType
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.models.LoginResponse
import de.ka.skyfallapp.repo.api.models.RegisterBody
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.utils.*
import de.ka.skyfallapp.utils.NavigationUtils.BACK

/**
 * The view model for registering a user.
 */
class RegisterViewModel(app: Application) : BaseViewModel(app) {

    private var registerUserName = ""
    private var registerEmail = ""
    private var registerPassword = ""
    private var registerRepeatPassword = ""

    val getDoneListener = ViewUtils.TextDoneListener { register() }
    val headerText = app.getString(R.string.register_head)
    val emailText = MutableLiveData<String>().apply { value = "" }
    val emailSelection = MutableLiveData<Int>().apply { value = 0 }
    val usernameText = MutableLiveData<String>().apply { value = "" }
    val passwordText = MutableLiveData<String>().apply { value = "" }
    val passwordRepeatText = MutableLiveData<String>().apply { value = "" }
    val controlsEnabled = MutableLiveData<Boolean>().apply { value = true }
    val usernameSelection = MutableLiveData<Int>().apply { value = 0 }
    val passwordSelection = MutableLiveData<Int>().apply { value = 0 }
    val passwordRepeatSelection = MutableLiveData<Int>().apply { value = 0 }
    val loadingVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val buttonVisibility = MutableLiveData<Int>().apply { value = View.VISIBLE }
    val getRegisterEmailChangedListener = ViewUtils.TextChangeListener {
        registerEmail = it
        emailText.postValue(it)
    }
    val getRegisterUserNameChangedListener = ViewUtils.TextChangeListener {
        registerUserName = it
        usernameText.postValue(it)
    }
    val getRegisterPasswordChangedListener = ViewUtils.TextChangeListener {
        registerPassword = it
        passwordText.postValue(it)
    }
    val getRegisterRepeatPasswordChangedListener = ViewUtils.TextChangeListener {
        registerRepeatPassword = it
        passwordRepeatText.postValue(it)
    }

    // TODO ADD evaluation of repeat password ... (validation)

    /**
     * Sets up the view for a new registration process, clearing all data.
     */
    fun setupNew() {
        registerUserName = ""
        registerEmail = ""
        registerPassword = ""
        registerRepeatPassword = ""

        updateTextViews()
    }

    private fun updateTextViews() {
        usernameText.postValue(registerUserName)
        usernameSelection.postValue(registerUserName.length)
        emailText.postValue(registerEmail)
        emailSelection.postValue(registerEmail.length)
        passwordText.postValue(registerPassword)
        passwordSelection.postValue(registerPassword.length)
        passwordRepeatText.postValue(registerRepeatPassword)
        passwordSelection.postValue(registerRepeatPassword.length)
    }

    /**
     * Goes back.
     */
    fun onBack() {
        navigateTo(BACK)
    }

    /**
     * Opens up the login. Because this will always be the pre-screen of the registration, simply goes back.
     */
    fun onToLoginClick() {
        navigateTo(BACK)
    }

    /**
     * Requests to register the user.
     */
    fun register() {
        repository.register(
            RegisterBody(
                registerUserName,
                registerEmail,
                registerPassword
            )
        )
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion(::handleRegister)
            .start(compositeDisposable, ::showLoading)
    }

    private fun handleRegister(result: RepoData<LoginResponse?>) {
        hideLoading()

        result.data?.let {
            navigateTo(navigationTargetId = NavigationUtils.POPUPTO, popupToId = R.id.profileFragment)
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

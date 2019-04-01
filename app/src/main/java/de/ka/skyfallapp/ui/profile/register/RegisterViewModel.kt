package de.ka.skyfallapp.ui.profile.register

import android.app.Application
import android.view.View
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.models.LoginResponse
import de.ka.skyfallapp.repo.api.models.RegisterBody
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.utils.*
import de.ka.skyfallapp.utils.NavigationUtils.BACK
import de.ka.skyfallapp.utils.ValidationRules.*
import org.koin.core.parameter.parametersOf

/**
 * The view model for registering a user.
 */
class RegisterViewModel(app: Application) : BaseViewModel(app) {

    private var registerUserName = ""
    private var registerEmail = ""
    private var registerPassword = ""
    private var registerRepeatPassword = ""

    val headerText = app.getString(R.string.register_head)
    val emailText = MutableLiveData<String>().apply { value = "" }
    val getDoneListener = ViewUtils.TextDoneListener { register() }
    val emailSelection = MutableLiveData<Int>().apply { value = 0 }
    val emailError = MutableLiveData<String>().apply { value = "" }
    val usernameText = MutableLiveData<String>().apply { value = "" }
    val passwordText = MutableLiveData<String>().apply { value = "" }
    val passwordError = MutableLiveData<String>().apply { value = "" }
    val usernameError = MutableLiveData<String>().apply { value = "" }
    val usernameSelection = MutableLiveData<Int>().apply { value = 0 }
    val passwordSelection = MutableLiveData<Int>().apply { value = 0 }
    val passwordRepeatText = MutableLiveData<String>().apply { value = "" }
    val controlsEnabled = MutableLiveData<Boolean>().apply { value = true }
    val repeatPasswordError = MutableLiveData<String>().apply { value = "" }
    val passwordRepeatSelection = MutableLiveData<Int>().apply { value = 0 }
    val loadingVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val buttonVisibility = MutableLiveData<Int>().apply { value = View.VISIBLE }
    val getRegisterEmailChangedListener = ViewUtils.TextChangeListener {
        registerEmail = it
        emailText.postValue(it)
        emailError.postValue("")
    }
    val getRegisterUserNameChangedListener = ViewUtils.TextChangeListener {
        registerUserName = it
        usernameText.postValue(it)
        usernameError.postValue("")
    }
    val getRegisterPasswordChangedListener = ViewUtils.TextChangeListener {
        registerPassword = it
        passwordText.postValue(it)
        passwordError.postValue("")
        repeatPasswordError.postValue("") // not to forget, because repeat password is dependant on the password
    }
    val getRegisterRepeatPasswordChangedListener = ViewUtils.TextChangeListener {
        registerRepeatPassword = it
        passwordRepeatText.postValue(it)
        repeatPasswordError.postValue("")
    }

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
        passwordError.postValue("")
        repeatPasswordError.postValue("")
        emailError.postValue("")
        usernameError.postValue("")
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
        // perform a quick low level validation
        InputValidator(
            listOf(
                ValidatorInput(registerUserName, usernameError, listOf(NOT_EMPTY)),
                ValidatorInput(registerEmail, emailError, listOf(NOT_EMPTY)),
                ValidatorInput(registerPassword, passwordError, listOf(NOT_EMPTY))
            )
        ).apply {
            // special validation for repeat password:
            var isRepeatValid = true
            val areAllOthersValid = validateAll(app)
            if (registerRepeatPassword != registerPassword) {
                repeatPasswordError.postValue(app.getString(R.string.error_input_no_match))
                isRepeatValid = false
            }

            if (!isRepeatValid || !areAllOthersValid) {
                return
            }
        }

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

        result.repoError?.errors?.forEach {
            when (it.parameter) {
                "email" -> emailError.postValue(it.localizedMessage(app))
                "password" -> passwordError.postValue(it.localizedMessage(app))
                "username" -> usernameError.postValue(it.localizedMessage(app))
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

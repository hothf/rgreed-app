package de.ka.skyfallapp.ui.profile.register

import android.app.Application
import android.view.View
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.*
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.utils.*
import de.ka.skyfallapp.utils.NavigationUtils.BACK

class RegisterViewModel(app: Application) : BaseViewModel(app) {

    private var registerUserName = ""
    private var registerEmail = ""
    private var registerPassword = ""
    private var registerRepeatPassword = ""

    val getDoneListener = ViewUtils.TextDoneListener()
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
        emailSelection.postValue(it.length)
    }
    val getRegisterUserNameChangedListener = ViewUtils.TextChangeListener {
        registerUserName = it
        usernameText.postValue(it)
        usernameSelection.postValue(it.length)
    }
    val getRegisterPasswordChangedListener = ViewUtils.TextChangeListener {
        registerPassword = it
        passwordText.postValue(it)
        passwordSelection.postValue(it.length)
    }
    val getRegisterRepeatPasswordChangedListener = ViewUtils.TextChangeListener {
        registerRepeatPassword = it
        passwordRepeatText.postValue(it)
        passwordSelection.postValue(it.length)
    }

    // TODO ADD evaluation of repeat password ... (validation)

    // TODO ADD error cases

    // TODO ADD we already go back on success but it should really log in too, this is a backend task but will make
    // registerResponse obsolete (should give a loginResponse instead!)

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

    fun onBack() {
        navigateTo(BACK)
    }

    fun onToLoginClick() {
        navigateTo(BACK)
    }

    fun register() {
        repository.register(RegisterBody(registerUserName, registerEmail, registerPassword))
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion(::handleRegister)
            .start(compositeDisposable, ::showLoading)
    }

    private fun handleRegister(result: RepoData<RegisterResponse?>) {
        hideLoading()

        navigateTo(navigationTargetId = NavigationUtils.POPUPTO, popupToId = R.id.profileFragment)
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

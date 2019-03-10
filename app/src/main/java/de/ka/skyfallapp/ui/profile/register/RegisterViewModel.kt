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

    var registerUserName = ""
    var registerEmail = ""
    var registerPassword = ""

    val getDoneListener = ViewUtils.TextDoneListener()
    val controlsEnabled = MutableLiveData<Boolean>().apply { value = true }
    val loadingVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val buttonVisibility = MutableLiveData<Int>().apply { value = View.VISIBLE }
    val getRegisterEmailChangedListener = ViewUtils.TextChangeListener { registerEmail = it }
    val getRegisterUserNameChangedListener = ViewUtils.TextChangeListener { registerUserName = it }
    val getRegisterPasswordChangedListener = ViewUtils.TextChangeListener { registerPassword = it }

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

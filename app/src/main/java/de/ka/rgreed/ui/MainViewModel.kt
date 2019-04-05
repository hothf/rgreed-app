package de.ka.rgreed.ui

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.navigation.navOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import de.ka.rgreed.R
import de.ka.rgreed.base.BaseViewModel
import de.ka.rgreed.base.events.AnimType
import de.ka.rgreed.repo.Profile
import de.ka.rgreed.repo.api.models.PushTokenBody
import de.ka.rgreed.repo.subscribeRepoCompletion
import de.ka.rgreed.ui.neweditconsensus.NewEditConsensusFragment
import de.ka.rgreed.ui.profile.ProfileFragment
import de.ka.rgreed.utils.*
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy

/**
 * The main view model for the single activity. Observes data which should be available in all screens.
 */
class MainViewModel(app: Application) : BaseViewModel(app) {

    init {
        repository.profileManager.observableLoginLogoutProfile
            .with(AndroidSchedulerProvider())
            .subscribeBy(onNext = { profile: Profile -> handleProfileLoginLogout(profile) }, onError = {})
            .addTo(compositeDisposable)

        apiErrorHandler.observableGlobalError
            .with(AndroidSchedulerProvider())
            .subscribeBy(onNext = ::handleErrors, onError = {})
            .addTo(compositeDisposable)

        backPressListener.observableBackpress
            .with(AndroidSchedulerProvider())
            .subscribeBy(onNext = { handleBack() }, onError = {})
            .addTo(compositeDisposable)
    }

    val barVisibility = MutableLiveData<Int>().apply { View.VISIBLE }

    /**
     * Called on a add click for new suggestions. Should lead to the creation of a new consensus.
     */
    fun onAddClick() {
        navigateTo(
            R.id.newConsensusFragment,
            args = Bundle().apply { putBoolean(NewEditConsensusFragment.NEW_KEY, true) },
            animType = AnimType.MODAL
        )
    }

    /**
     * Tries to register for push, if Firebase is ready and has a token. This is a very silent process.
     */
    fun registerForPush() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }

                val token = task.result?.token
                repository.profileManager.updateProfile { pushToken = token }

                if (repository.profileManager.currentProfile.username == null) {
                    return@OnCompleteListener // only try push register, when the user is logged in.
                }

                if (!token.isNullOrEmpty() && !repository.profileManager.isPushTokenConfirmed(token)) {
                    repository.registerPushToken(PushTokenBody(token))
                        .with(AndroidSchedulerProvider())
                        .subscribeRepoCompletion { }
                        .start(compositeDisposable)
                }
            })
    }

    private fun handleProfileLoginLogout(profile: Profile) {
        if (profile.username == null) {
            showSnack(app.getString(R.string.main_logged_out))
        } else {
            showSnack(String.format(app.getString(R.string.main_logged_in), profile.username))
        }
    }

    private fun handleErrors(error: ApiErrorManager.GlobalApiError) {
        when (error.status) {
            401 -> navigateTo(
                R.id.profileFragment,
                navOptions = navOptions { launchSingleTop = true },
                args = Bundle().apply { putBoolean(ProfileFragment.NEW_KEY, true) },
                animType = AnimType.MODAL
            )
            0 -> showSnack(app.getString(R.string.error_network), Snacker.SnackType.ERROR)
            in 400..499 -> showSnack(app.getString(R.string.error_client), Snacker.SnackType.ERROR)
            else -> showSnack(app.getString(R.string.error_unknown), Snacker.SnackType.ERROR)
        }
    }
}

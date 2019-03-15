package de.ka.skyfallapp.ui

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.base.events.AnimType
import de.ka.skyfallapp.base.events.SnackType
import de.ka.skyfallapp.repo.Profile
import de.ka.skyfallapp.ui.neweditconsensus.NewEditConsensusFragment
import de.ka.skyfallapp.ui.profile.ProfileFragment
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.ApiErrorManager
import de.ka.skyfallapp.utils.with
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber

/**
 * The main view model for the single activity. Observes data which should be available in all screens.
 */
class MainViewModel(app: Application) : BaseViewModel(app) {

    init {
        repository.profileManager.observableProfile
            .with(AndroidSchedulerProvider())
            .subscribeBy(onNext = { profile: Profile ->
                Timber.e("Profile subscription onNext $profile")
                handleProfileChange(profile)
            }
            )
            .addTo(compositeDisposable)

        apiErrorHandler.observableError
            .with(AndroidSchedulerProvider())
            .subscribeBy(onNext = ::handleErrors)
            .addTo(compositeDisposable)

        backPressListener.observableBackpress
            .with(AndroidSchedulerProvider())
            .subscribeBy(onNext = { handleBack() })
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

    private fun handleProfileChange(profile: Profile) {
        if (profile.username == null) {
            showSnack("Logged out")
        } else {
            showSnack("Logged in: ${profile.username}")
        }
    }

    private fun handleErrors(error: ApiErrorManager.ApiError) {
        when (error.status) {
            401 -> navigateTo(
                R.id.profileFragment,
                args = Bundle().apply { putBoolean(ProfileFragment.NEW_KEY, true) },
                animType = AnimType.MODAL
            )
            0 -> showSnack(app.getString(R.string.errors_network), SnackType.WARNING)
            409 -> showSnack(app.getString(R.string.errors_conflict), SnackType.ERROR)
            in 400..499 -> showSnack(app.getString(R.string.error_client), SnackType.ERROR)
            else -> showSnack(app.getString(R.string.error_unknown), SnackType.ERROR)
        }
    }
}

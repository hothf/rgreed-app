package de.ka.skyfallapp.ui

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.repo.Profile
import de.ka.skyfallapp.ui.neweditconsensus.NewEditConsensusFragment
import de.ka.skyfallapp.ui.profile.ProfileFragment
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
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
            .subscribeBy(
                onNext = { apiError ->
                    if (apiError.status == 401) {
                        navigateTo(
                            R.id.profileFragment,
                            args = Bundle().apply { putBoolean(ProfileFragment.NEW_KEY, true) })
                    }
                }
            )
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
            args = Bundle().apply { putBoolean(NewEditConsensusFragment.NEW_KEY, true) })
    }

    private fun handleProfileChange(profile: Profile) {
        if (profile.username == null) {
            showSnack("Logged out")
        } else {
            showSnack("Logged in: ${profile.username}")
        }
    }

}

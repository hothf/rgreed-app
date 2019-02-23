package de.ka.skyfallapp.ui

import android.app.Application
import android.view.View
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.repo.Profile
import de.ka.skyfallapp.ui.home.HomeFragment
import de.ka.skyfallapp.ui.personal.PersonalFragment
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.with
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber

class MainViewModel(app: Application) : BaseViewModel(app) {

    init {
        repository.profileManager.subject
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

        apiErrorHandler.subject
            .with(AndroidSchedulerProvider())
            .subscribeBy (
                onNext = { apiError ->
                    if (apiError.status == 401){
                        navigateTo(R.id.profileFragment)
                    }
                }
            )
            .addTo(compositeDisposable)
    }


    val barVisibility = MutableLiveData<Int>().apply { View.VISIBLE }

    fun onAddClick() {
        navigateTo(R.id.newConsensusFragment)
    }

    private fun handleProfileChange(profile: Profile) {

        dirtyDataWatcher.markDirty(HomeFragment.HOME_DIRTY)
        dirtyDataWatcher.markDirty(PersonalFragment.PERSONAL_DIRTY)

        if (profile.username == null) {
            showSnack("Logged out")
        } else {
            showSnack("${profile.username} loggedIn")
        }
    }

}

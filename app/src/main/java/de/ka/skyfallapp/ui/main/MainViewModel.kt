package de.ka.skyfallapp.ui.main

import android.app.Application
import android.view.View
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.repo.api.Profile
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
    }


    val barVisibility = MutableLiveData<Int>().apply { View.VISIBLE }

    fun onAddClick() {
        navigateTo(R.id.newConsensusFragment)
    }

    private fun handleProfileChange(profile: Profile) {

        if (profile.username == null) {
            showSnack("Logged out")
        } else {
            showSnack("${profile.username} loggedIn")
        }
    }

}

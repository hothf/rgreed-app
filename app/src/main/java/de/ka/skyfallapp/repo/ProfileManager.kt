package de.ka.skyfallapp.repo

import androidx.annotation.Keep
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

@Keep
data class Profile(val username: String? = null, val token: String? = null)

interface ProfileManager {

    var currentProfile: Profile?

    val observableProfile: Observable<Profile>
}
package de.ka.skyfallapp.repo

import androidx.annotation.Keep
import io.reactivex.subjects.PublishSubject

@Keep
data class Profile(val username: String? = null, val token: String? = null)

interface ProfileManager {

    var currentProfile: Profile?

    val subject: PublishSubject<Profile>
}
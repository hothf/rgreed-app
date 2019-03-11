package de.ka.skyfallapp.repo

import io.reactivex.Observable

data class Profile(val username: String? = null, val token: String? = null)

interface ProfileManager {

    var currentProfile: Profile?

    val observableProfile: Observable<Profile>
}
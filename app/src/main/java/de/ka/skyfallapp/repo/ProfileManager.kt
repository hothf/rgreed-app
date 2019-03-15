package de.ka.skyfallapp.repo

import io.reactivex.Observable

data class Profile(var username: String? = null, var token: String? = null, var pushToken: String? = null)

interface ProfileManager {

    var currentProfile: Profile

    val observableProfile: Observable<Profile>

    /**
     * Updates the current profile with the values provided in the given apply block.
     */
    fun updateProfile(block: Profile.() -> Unit): Profile
}


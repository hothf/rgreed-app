package de.ka.skyfallapp.repo

import io.reactivex.Observable

data class Profile(
    var username: String? = null,
    var token: String? = null,
    var pushToken: String? = null,
    var confirmedPushToken: String? = null
)

interface ProfileManager {

    var currentProfile: Profile

    val observableProfile: Observable<Profile>

    /**
     * Updates the current profile with the values provided in the given apply block.
     */
    fun updateProfile(block: Profile.() -> Unit): Profile

    /**
     * Checks whether the given token is confirmed by the server or not. This indicator can be used
     * to reduce push token register requests for tokens that are already registered.
     */
    fun isPushTokenConfirmed(token: String): Boolean
}


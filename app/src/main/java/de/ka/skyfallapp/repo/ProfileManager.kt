package de.ka.skyfallapp.repo

import io.reactivex.Observable

/**
 * A profile containing user data, jwt and push tokens plus user specific settings.
 */
data class Profile(
    var username: String? = null,
    var token: String? = null,
    var pushToken: String? = null,
    var confirmedPushToken: String? = null,
    var isPushEnabled: Boolean = true
)

interface ProfileManager {

    /**
     * The currently loaded user profile.
     */
    var currentProfile: Profile

    /**
     * Should only emit a profile, if a logout/login has occurred. For accessing the  profile, use [currentProfile].
     */
    val observableLoginLogoutProfile: Observable<Profile>

    /**
     * Should  emit a profile, if something of the [currentProfile] has changed.
     */
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


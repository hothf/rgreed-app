package de.ka.rgreed.repo

import de.ka.rgreed.repo.db.*
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import io.reactivex.subjects.PublishSubject


class ProfileManagerImpl(val db: AppDatabase) : ProfileManager {

    override var currentProfile = Profile()
    override val observableProfile: PublishSubject<Profile> = PublishSubject.create()
    override val observableLoginLogoutProfile: PublishSubject<Profile> = PublishSubject.create()

    // load profile
    init {
        val profileBox: Box<ProfileDao> = db.get().boxFor()

        if (!profileBox.isEmpty) {

            val profileDao = profileBox.all.firstOrNull()

            // The profile class abstracts from the DAO. Currently we could simply use the DAO, but it might be likely
            // that the profile will have some fields, the DAO should not have.
            if (profileDao != null) {
                currentProfile =
                    Profile(
                        profileDao.username,
                        profileDao.token,
                        profileDao.pushToken,
                        profileDao.confirmedPushToken,
                        profileDao.pushEnabled
                    )
            }
        }
    }

    // update profile
    override fun updateProfile(block: Profile.() -> Unit): Profile {
        val profile = currentProfile.apply(block)

        val profileBox: Box<ProfileDao> = db.get().boxFor()
        val id = profileBox.all.firstOrNull()?.id ?: 0

        val profileDao = ProfileDao(
            id,
            profile.username,
            profile.token,
            profile.pushToken,
            profile.confirmedPushToken,
            profile.isPushEnabled
        )
        profileBox.put(profileDao)

        //currentProfile = profile
        observableProfile.onNext(currentProfile)

        return profile
    }

    fun removeProfile() {
        val profileBox: Box<ProfileDao> = db.get().boxFor()
        profileBox.removeAll()

        currentProfile = Profile().apply {
            // remember to keep these settings, as they are bound to the device, not to the account
            this.pushToken = currentProfile.pushToken
            this.isPushEnabled = currentProfile.isPushEnabled
        }
        observableProfile.onNext(currentProfile)

        observableLoginLogoutProfile.onNext(currentProfile)
    }

    fun loginProfile(profile: Profile) {
        observableLoginLogoutProfile.onNext(updateProfile {
            username = profile.username
            token = profile.token
            confirmedPushToken = currentProfile.pushToken
        })
    }

    override fun isPushTokenConfirmed(token: String): Boolean {
        if (currentProfile.confirmedPushToken == null) {
            return false
        }
        return currentProfile.confirmedPushToken == token
    }
}
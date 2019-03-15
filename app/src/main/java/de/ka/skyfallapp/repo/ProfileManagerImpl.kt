package de.ka.skyfallapp.repo

import de.ka.skyfallapp.repo.db.AppDatabase
import de.ka.skyfallapp.repo.db.ProfileDao
import de.ka.skyfallapp.repo.db.singleUpdateId
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import io.reactivex.subjects.PublishSubject


class ProfileManagerImpl(val db: AppDatabase) : ProfileManager {

    override var currentProfile = Profile()
    override val observableProfile: PublishSubject<Profile> = PublishSubject.create()

    init {
        val profileBox: Box<ProfileDao> = db.get().boxFor()

        if (!profileBox.isEmpty) {

            val profileDao = profileBox.all.first()

            if (profileDao != null) {
                currentProfile = Profile(profileDao.username, profileDao.token, profileDao.pushToken)
            }
        }
    }

    fun removeProfile() {
        val profileBox: Box<ProfileDao> = db.get().boxFor()
        profileBox.removeAll()

        currentProfile = Profile()

        observableProfile.onNext(Profile())
    }

    fun loginProfile(profile: Profile) {
        observableProfile.onNext(updateProfile {
            username = profile.username
            token = profile.token
        })
    }

    override fun updateProfile(block: Profile.() -> Unit): Profile {
        val profile = currentProfile.apply(block)

        val profileBox: Box<ProfileDao> = db.get().boxFor()
        val profileDao = ProfileDao(
            profileBox.singleUpdateId(),
            profile.username,
            profile.token,
            profile.pushToken
        )
        profileBox.put(profileDao)

        currentProfile = profile

        return profile
    }
}
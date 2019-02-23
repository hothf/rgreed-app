package de.ka.skyfallapp.repo

import de.ka.skyfallapp.repo.db.AppDatabase
import de.ka.skyfallapp.repo.db.ProfileDao
import de.ka.skyfallapp.repo.db.singleUpdateId
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import io.reactivex.subjects.PublishSubject


class ProfileManagerImpl(val db: AppDatabase) : ProfileManager {

    override var currentProfile: Profile? = null
    override val subject: PublishSubject<Profile> = PublishSubject.create()

    init {
        val profileBox: Box<ProfileDao> = db.get().boxFor()

        if (!profileBox.isEmpty) {

            val profileDao = profileBox.all.first()

            if (profileDao != null) {
                currentProfile = Profile(profileDao.username, profileDao.token)
            }
        }
    }

    fun removeProfile() {
        val profileBox: Box<ProfileDao> = db.get().boxFor()
        profileBox.removeAll()

        currentProfile = null

        subject.onNext(Profile(null))
    }

    fun updateProfile(profile: Profile) {
        val profileBox: Box<ProfileDao> = db.get().boxFor()
        val profileDao = ProfileDao(profileBox.singleUpdateId(), profile.username, profile.token)
        profileBox.put(profileDao)

        currentProfile = profile

        subject.onNext(profile)
    }
}
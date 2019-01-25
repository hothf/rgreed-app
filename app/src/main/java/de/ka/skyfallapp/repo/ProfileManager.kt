package de.ka.skyfallapp.repo

import de.ka.skyfallapp.repo.api.Profile
import io.reactivex.subjects.PublishSubject


interface ProfileManager {

    var currentProfile: Profile?

    val subject: PublishSubject<Profile>
}
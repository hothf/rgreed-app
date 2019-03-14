package de.ka.skyfallapp.utils

import de.ka.skyfallapp.repo.RepoData
import io.reactivex.subjects.PublishSubject

/**
 * Utility class for checking if any api error has to be handled.
 */
class ApiErrorHandler {

    data class ApiError(val status: Int)

    val observableError: PublishSubject<ApiError> = PublishSubject.create()

    /**
     * Offers fast access to a default error handling of repo data.
     * For example, the profile fragment is immediately called if the given repo data say that the user is unauthorized.
     *
     * Will execute any given code that is not handled in the unhandled program block.
     */
    fun handle(repoData: RepoData<*>, unhandled: (RepoData<*>) -> Unit) {
        if (repoData.info.code == 401) {
            observableError.onNext(ApiError(401))
        } else {
            unhandled(repoData)
        }
    }
}



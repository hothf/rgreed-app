package de.ka.skyfallapp.utils

import de.ka.skyfallapp.repo.RepoData
import io.reactivex.subjects.PublishSubject

/**
 * Utility class for checking if any api error has to be handled.
 */
class ApiErrorManager {

    data class ApiError(val status: Int)

    val observableError: PublishSubject<ApiError> = PublishSubject.create()

    /**
     * Signals errors received via [observableError].
     *
     * Will execute any given code that is not handled in the unhandled program block.
     *
     * @param repoData optional repo data. Will be there if everything runs fine (even if a  a server error occurred)
     * @param throwable optional error. This kicks in if something really bad happens, usually only on the device itself
     * @param unhandled optional method. Run if nothing could be handled
     */
    fun handle(
        repoData: RepoData<*>?,
        throwable: Throwable?,
        unhandled: () -> Unit = {}
    ) {
        if (repoData != null && (repoData.info.code == 0 || repoData.info.code > 299)) {
            observableError.onNext(ApiError(repoData.info.code))
        } else if (throwable != null) {
            observableError.onNext(ApiError(0))
        } else {
            unhandled()
        }
    }
}



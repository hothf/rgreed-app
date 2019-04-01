package de.ka.rgreed.utils

import de.ka.rgreed.repo.RepoData
import io.reactivex.subjects.PublishSubject

/**
 * Utility class for checking if any api error has to be handled.
 */
class ApiErrorManager {

    data class GlobalApiError(val status: Int)

    val observableGlobalError: PublishSubject<GlobalApiError> = PublishSubject.create()

    /**
     * Signals errors received via [observableGlobalError].
     *
     * Will execute any given code that is not handled in the unhandled program block.
     *
     * @param repoData optional repo data. Will be there if everything runs fine (even if a  a server error occurred)
     * @param throwable optional error. This kicks in if something really bad happens, usually only on the device itself
     * @param silenceUnAuthorized set to true if 401 (unauthorized) errors should simply be ignored
     * @param unhandled optional method. Run if nothing could be handled
     */
    fun handle(
        repoData: RepoData<*>?,
        throwable: Throwable?,
        silenceUnAuthorized: Boolean = false,
        unhandled: () -> Unit = {}
    ) {
        if (repoData != null && (repoData.info.code == 0 || repoData.info.code > 299)) {
            if (silenceUnAuthorized && repoData.info.code == 401) {
                return
            }
            if (repoData.repoError == null || repoData.repoError.errors.none { it.parameter != null }) {
                observableGlobalError.onNext(GlobalApiError(repoData.info.code))
            }
        } else if (throwable != null) {
            observableGlobalError.onNext(GlobalApiError(0))
        } else {
            unhandled()
        }
    }
}



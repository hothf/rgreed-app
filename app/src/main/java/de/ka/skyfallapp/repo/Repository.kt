package de.ka.skyfallapp.repo

import androidx.annotation.Keep
import de.ka.skyfallapp.repo.api.*
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import okhttp3.Headers
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

/**
 * The interface for the abstraction of the data sources of the app.
 */
interface Repository {

    val profileManager: ProfileManager

    /**
     * Retrieves a list of all consensus.
     */
    fun getConsensus(): Single<RepoData<List<Consensus>?>>

    /**
     * Retrieves the details of a consensus.
     */
    fun getConsensusDetail(id: String): Single<RepoData<ConsensusDetail?>>

    /**
     * Deletes the given consensus.
     */
    fun deleteConsensus(id: String): Single<RepoData<ResponseBody?>>

    /**
     * Sends a consensus.
     */
    fun sendConsensus(consensus: ConsensusDetail): Single<RepoData<ConsensusDetail?>>

    /**
     * Retrieves the personal created consensus.
     */
    fun getCreatedConsensus(): Single<RepoData<List<Consensus>?>>

    /**
     * Retrieves the personal participating  consensus minus created ones.
     */
    fun getParticipatingConsensus(): Single<RepoData<List<Consensus>?>>

    /**
     * Sends a login-register request.
     */
    fun loginRegister(loginRegister: LoginRegister): Single<RepoData<Token?>>

    /**
     * Logs the user out.
     */
    fun logout()

}

/**
 * A wrapper for repository data, bundled with possible data info with errors.
 */
@Keep
data class RepoData<T>(val data: T, val info: Info)

/**
 * A info wrapper for additional api info or/and errors
 */
@Keep
data class Info(val code: Int, val headers: Headers? = null, val throwable: Throwable? = null)

/**
 * A conversion to a repository single item stream with optional data with errors.
 *
 * This will always emit success, as it tries to convert the result of an error to a data object. This is intended
 * to be used with databases which should always deliver cached objects, when possible.
 *
 * A success block can be used to work with the received data.
 * The error response item  can be used to convert the error back to a (successful) data object.
 */
fun <T, E : Response<T>> Single<E>.mapToRepoData(
    success: ((T?) -> Unit)? = null,
    errorResponseItem: E? = null
): Single<RepoData<T?>> {

    var code = -1
    var data: T? = null
    var headers: Headers? = null
    var error: Throwable? = null

    return this.doOnSuccess {
        headers = it.headers()
        code = it.code()
        data = it.body()

        success?.invoke(data)
    }
        .onErrorReturn { throwable ->

            if (throwable is HttpException) {
                code = throwable.code()
            }

            error = throwable
            data = errorResponseItem?.body()
            errorResponseItem
        }
        .map { RepoData(data, Info(code, headers, error)) }
}

/**
 * When extending a single stream of repo data, this can be used to get the result of the stream.
 * This is convenient, if no additional error mapping is wanted.
 *
 * This will wrap all errors in the success block and link the exception into it.
 */
fun <T> Single<RepoData<T?>>.subscribeRepoCompletion(onComplete: (RepoData<T?>) -> Unit): Disposable {
    return subscribe(onComplete, { throwable ->
        onComplete(RepoData(null, Info(0, null, throwable)))
    })
}
package de.ka.skyfallapp.repo

import androidx.annotation.Keep
import de.ka.skyfallapp.repo.api.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import okhttp3.Headers
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

/**
 * The interface for the abstraction of the data sources of the app.
 */
interface Repository {

    val profileManager: ProfileManager

    val observableConsensuses: BehaviorSubject<MutableList<ConsensusResponse>>

    /**
     * Retrieves a list of all personal consensus where the user is an admin, has created a suggestion or voted on one.
     */
    fun getPersonalConsensus(): Single<RepoData<List<ConsensusResponse>?>>

    /**
     * Retrieves a list of all consensus.
     */
    fun getConsensus(resetCurrent: Boolean, limit: Int, offset: Int): Single<RepoData<List<ConsensusResponse>?>>

    /**
     * Retrieves the details of a consensus.
     */
    fun getConsensusDetail(consensusId: Int): Single<RepoData<ConsensusResponse?>>

    /**
     * Deletes the given consensus.
     */
    fun deleteConsensus(consensusId: Int): Single<RepoData<ResponseBody?>>

    /**
     * Updates a consensus
     */
    fun updateConsensus(consensusId: Int, consensusBody: ConsensusBody): Single<RepoData<ConsensusResponse?>>

    /**
     * Sends a consensus.
     */
    fun sendConsensus(consensus: ConsensusBody): Single<RepoData<ConsensusResponse?>>

    /**
     * Get suggestion details.
     */
    fun getSuggestionDetail(consensusId: Int, suggestionId: Int): Single<RepoData<SuggestionResponse?>>

    /**
     * Sends a suggestion.
     */
    fun sendSuggestion(consensusId: Int, suggestionBody: SuggestionBody): Single<RepoData<SuggestionResponse?>>

    /**
     * Updates a suggestion
     */
    fun updateSuggestion(
        consensusId: Int,
        suggestionId: Int,
        suggestionBody: SuggestionBody
    ): Single<RepoData<SuggestionResponse?>>

    /**
     * Deletes a suggestion
     */
    fun deleteSuggestion(consensusId: Int, suggestionId: Int): Single<RepoData<ResponseBody?>>

    /**
     * Votes for a suggestion.
     */
    fun voteForSuggestion(
        consensusId: Int,
        suggestionId: Int,
        voteBody: VoteBody
    ): Single<RepoData<SuggestionResponse?>>

    /**
     * Gets all consensus suggestions.
     */
    fun getConsensusSuggestions(consensusId: Int): Single<RepoData<List<SuggestionResponse>?>>

    /**
     * Sends a login request.
     */
    fun login(loginBody: LoginBody): Single<RepoData<LoginResponse?>>

    /**
     * Sends a register request.
     */
    fun register(registerBody: RegisterBody): Single<RepoData<RegisterResponse?>>

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
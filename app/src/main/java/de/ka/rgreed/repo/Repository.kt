package de.ka.rgreed.repo

import android.content.Context
import com.google.gson.Gson
import de.ka.rgreed.R
import de.ka.rgreed.repo.api.models.LoginBody
import de.ka.rgreed.repo.api.models.LoginResponse
import de.ka.rgreed.repo.api.models.PushTokenBody
import de.ka.rgreed.repo.api.models.RegisterBody
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import okhttp3.Headers
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber

/**
 * The interface for the abstraction of the data sources of the app.
 */
interface Repository {

    val profileManager: ProfileManager
    val consensusManager: ConsensusManager

    /**
     * Registers a push token.
     */
    fun registerPushToken(pushTokenBody: PushTokenBody): Single<RepoData<ResponseBody?>>

    /**
     * Sends a login request.
     */
    fun login(loginBody: LoginBody): Single<RepoData<LoginResponse?>>

    /**
     * Sends a register request.
     */
    fun register(registerBody: RegisterBody): Single<RepoData<LoginResponse?>>

    /**
     * Logs the user out.
     */
    fun logout()
}

/**
 * A wrapper for repository data, bundled with possible data info with errors.
 */
data class RepoData<T>(val data: T, val info: Info, val repoError: RepoError? = null)

/**
 * A info wrapper for additional api info or/and errors
 */
data class Info(val code: Int, val headers: Headers? = null, val throwable: Throwable? = null)

/**
 * A repo error.
 */
data class RepoError(val errors: List<RepoErrorResponse>)

/**
 * A mapping of api errors to strings
 */
val errorMap = mapOf(
    404 to R.string.api_error_notfound,
    403 to R.string.api_error_forbidden,
    400 to R.string.api_error_general_bad_request,
    401 to R.string.api_error_bad_credentials,
    406 to R.string.api_error_username_taken,
    409 to R.string.api_error_suggestion_title_taken,
    444 to R.string.api_error_voting_after_consensus_end,
    445 to R.string.api_error_title_min,
    446 to R.string.api_error_consensus_end_too_early,
    450 to R.string.api_error_username_min,
    451 to R.string.api_error_email_min,
    452 to R.string.api_error_password_min,
    477 to R.string.api_error_voting_range
)

/**
 * A repo error response.
 */
data class RepoErrorResponse(val code: Int, val description: String, val parameter: String? = null) {

    /**
     * Retrieves a localized message to the corresponding [code], even if a suitable one could not be found there is
     * a very generic error message.
     */
    fun localizedMessage(context: Context): String {
        return errorMap[code]?.let { context.getString(it) } ?: context.getString(R.string.api_error_unknown)
    }

}

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
    var repoError: RepoError? = null

    return this.doOnSuccess {
        headers = it.headers()
        code = it.code()
        data = it.body()

        it.errorBody()?.let { apiError ->
            repoError = try {
                Gson().fromJson(apiError.string(), RepoError::class.java)
            } catch (e: Exception) {
                Timber.e(e, "Could not parse error body.")
                null
            }
        }
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
        .map { RepoData(data, Info(code, headers, error), repoError) }
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
package de.ka.rgreed.repo

import android.app.Application
import de.ka.rgreed.repo.api.*
import de.ka.rgreed.repo.api.models.LoginBody
import de.ka.rgreed.repo.api.models.LoginResponse
import de.ka.rgreed.repo.api.models.PushTokenBody
import de.ka.rgreed.repo.api.models.RegisterBody

import de.ka.rgreed.repo.db.AppDatabase
import de.ka.rgreed.utils.ApiErrorManager

import io.reactivex.Single
import okhttp3.ResponseBody


/**
 * The implementation for the abstraction of data sources.
 */
class RepositoryImpl(
    val app: Application,
    val api: ApiService,
    val db: AppDatabase,
    private val apiErrorHandler: ApiErrorManager,
    override val profileManager: ProfileManagerImpl,
    override val consensusManager: ConsensusManagerImpl
) : Repository {

    override fun login(loginBody: LoginBody): Single<RepoData<LoginResponse?>> {
        loginBody.pushToken = profileManager.currentProfile.pushToken

        return api.postLogin(loginBody).mapToRepoData(success = { result ->
            result?.let(::updateLogin)
        }).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun register(registerBody: RegisterBody): Single<RepoData<LoginResponse?>> {
        registerBody.pushToken = profileManager.currentProfile.pushToken

        return api.postRegistration(registerBody).mapToRepoData(
            success = { result -> result?.let(::updateLogin) })
            .doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    // We do some fancy stuff here:
    // -  This method should only be called if the pushTokenBody contains a push token which has not
    //    been confirmed by the server yet.
    //    We update beforehand the profile with the push token. If anything fails, we can repeat this process
    //    and it works, because it is still not confirmed.
    // -  We fire a call to register the push token. This will also be done on registering and logging in.
    // -  When registering / login or doing this call we update the profile confirmed token on success
    // -> This should lead to only register push tokens not confirmed or logging in or registering a new user.
    override fun registerPushToken(pushTokenBody: PushTokenBody): Single<RepoData<ResponseBody?>> {
        return api.postPushTokenRegistration(pushTokenBody).mapToRepoData().doOnEvent { result, _ ->
            if (result != null && result.info.code in 200..299) {
                profileManager.updateProfile { confirmedPushToken = pushToken }
            }
        }
    }

    override fun logout() {
        profileManager.removeProfile()
    }

    private fun updateLogin(loginResponse: LoginResponse) {
        profileManager.loginProfile(Profile(loginResponse.userName, loginResponse.token))
    }
}
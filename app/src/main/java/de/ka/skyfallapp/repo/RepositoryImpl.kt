package de.ka.skyfallapp.repo

import android.app.Application
import de.ka.skyfallapp.repo.api.*
import de.ka.skyfallapp.repo.api.models.LoginBody
import de.ka.skyfallapp.repo.api.models.LoginResponse
import de.ka.skyfallapp.repo.api.models.RegisterBody

import de.ka.skyfallapp.repo.db.AppDatabase
import de.ka.skyfallapp.utils.ApiErrorManager

import io.reactivex.Single


/**
 * The implementation for the abstraction of data sources.
 */
class RepositoryImpl(
    val app: Application,
    val api: ApiService,
    val db: AppDatabase,
    val apiErrorHandler: ApiErrorManager,
    override val profileManager: ProfileManagerImpl,
    override val consensusManager: ConsensusManagerImpl
) : Repository {

    override fun login(loginBody: LoginBody): Single<RepoData<LoginResponse?>> {
        return api.postLogin(loginBody).mapToRepoData(success = { result ->
            result?.let(::updateLogin)
        }).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun register(registerBody: RegisterBody): Single<RepoData<LoginResponse?>> {
        return api.postRegistration(registerBody).mapToRepoData(success = { result -> result?.let(::updateLogin) })
            .doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun logout() {
        profileManager.removeProfile()
    }

    private fun updateLogin(loginResponse: LoginResponse) {
        profileManager.updateProfile(Profile(loginResponse.userName, loginResponse.token))
    }
}
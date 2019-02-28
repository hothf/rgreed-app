package de.ka.skyfallapp.repo

import android.app.Application
import de.ka.skyfallapp.repo.api.*

import de.ka.skyfallapp.repo.db.AppDatabase
import de.ka.skyfallapp.repo.db.ConsensusManagerImpl

import io.reactivex.Single


/**
 * The implementation for the abstraction of data sources.
 */
class RepositoryImpl(
    val app: Application,
    val api: ApiService,
    val db: AppDatabase,
    override val profileManager: ProfileManagerImpl,
    override val consensusManager: ConsensusManagerImpl
) : Repository {

    override fun login(loginBody: LoginBody): Single<RepoData<LoginResponse?>> {
        return api.postLogin(loginBody).mapToRepoData(
            success = { result ->
                result?.let {
                    profileManager.updateProfile(Profile(result.userName, result.token))
                }
            }
        )
    }

    override fun register(registerBody: RegisterBody): Single<RepoData<RegisterResponse?>> {
        return api.postRegistration(registerBody).mapToRepoData()
    }

    override fun logout() {
        profileManager.removeProfile()
    }
}
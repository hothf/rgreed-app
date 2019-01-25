package de.ka.skyfallapp.repo

import android.app.Application
import de.ka.skyfallapp.repo.api.*

import de.ka.skyfallapp.repo.db.AppDatabase

import io.reactivex.Single


/**
 * The implementation for the abstraction of data sources.
 */
class RepositoryImpl(
    val app: Application,
    val api: ApiService,
    val db: AppDatabase,
    override val profileManager: ProfileManagerImpl
) : Repository {

    override fun getConsensus(): Single<RepoData<List<Consensus>?>> {
        return api.getConsensus().mapToRepoData()
    }

    override fun getConsensusDetail(id: String): Single<RepoData<ConsensusDetail?>> {
        return api.getConsensusDetail(id).mapToRepoData()
    }

    override fun getConsensusAcceptParticipation(id: String): Single<RepoData<ConsensusDetail?>> {
        return api.getConsensusParticipation(id).mapToRepoData()
    }

    override fun sendConsensus(consensus: Consensus): Single<RepoData<Consensus?>> {
        return api.postConsensus(consensus).mapToRepoData()
    }

    override fun getPersonalConsensus(): Single<RepoData<List<Consensus>?>> {
        return api.getPersonalConsensus().mapToRepoData()
    }

    override fun loginRegister(loginRegister: LoginRegister): Single<RepoData<Token?>> {
        return api.loginRegister(loginRegister).mapToRepoData(
            success = { result ->
                result?.let {
                    profileManager.updateProfile(Profile(loginRegister.user, result.token))
                }
            }
        )
    }

    override fun logout() {
        profileManager.removeProfile()
    }
}
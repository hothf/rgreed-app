package de.ka.skyfallapp.repo

import android.app.Application
import de.ka.skyfallapp.repo.api.*

import de.ka.skyfallapp.repo.db.AppDatabase
import de.ka.skyfallapp.ui.home.consensus.ConsensusDetailManager

import io.reactivex.Single
import okhttp3.ResponseBody


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
        return api.getConsensusDetail(id).mapToRepoData(
            success = {
                ConsensusDetailManager.setDetail(it)
            }
        )
    }

    override fun deleteConsensus(id: String): Single<RepoData<ResponseBody?>> {
        return api.deleteConsensus(id).mapToRepoData()
    }

    override fun sendConsensus(consensus: ConsensusDetail): Single<RepoData<ConsensusDetail?>> {
        return api.postConsensus(consensus).mapToRepoData(
            success = {
                ConsensusDetailManager.setDetail(it)
            }
        )
    }

    override fun getCreatedConsensus(): Single<RepoData<List<Consensus>?>> {
        return api.getCreatedConsensus().mapToRepoData()
    }

    override fun getParticipatingConsensus(): Single<RepoData<List<Consensus>?>> {
        return api.getParticipatingConsensus().mapToRepoData()
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
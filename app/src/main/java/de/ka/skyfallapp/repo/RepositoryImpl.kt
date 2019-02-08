package de.ka.skyfallapp.repo

import android.app.Application
import de.ka.skyfallapp.repo.api.*

import de.ka.skyfallapp.repo.db.AppDatabase

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

    override fun getConsensus(): Single<RepoData<List<ConsensusResponse>?>> {
        return api.getConsensus().mapToRepoData()
    }

    override fun getConsensusDetail(consensusId: Int): Single<RepoData<ConsensusResponse?>> {
        return api.getConsensusDetail(consensusId).mapToRepoData()
    }

    override fun deleteConsensus(consensusId: Int): Single<RepoData<ResponseBody?>> {
        return api.deleteConsensus(consensusId).mapToRepoData()
    }

    override fun updateConsensus(consensusId: Int, consensusBody: ConsensusBody): Single<RepoData<ConsensusResponse?>> {
        return api.updateConsensus(consensusId, consensusBody).mapToRepoData()
    }

    override fun sendConsensus(consensus: ConsensusBody): Single<RepoData<ConsensusResponse?>> {
        return api.postConsensus(consensus).mapToRepoData()
    }

    override fun getSuggestions(): Single<RepoData<List<SuggestionResponse>?>> {
        return api.getSuggestions().mapToRepoData()
    }

    override fun getSuggestionDetail(suggestionId: Int): Single<RepoData<SuggestionResponse?>> {
        return api.getSuggestionDetail(suggestionId).mapToRepoData()
    }

    override fun sendSuggestion(suggestionBody: SuggestionBody): Single<RepoData<SuggestionResponse?>> {
        return api.postSuggestion(suggestionBody).mapToRepoData()
    }

    override fun updateSuggestion(
        suggestionId: Int,
        suggestionBody: SuggestionBody
    ): Single<RepoData<SuggestionResponse?>> {
        return api.updateSuggestion(suggestionId, suggestionBody).mapToRepoData()
    }

    override fun deleteSuggestion(suggestionId: Int): Single<RepoData<ResponseBody?>> {
        return api.deleteSuggestion(suggestionId).mapToRepoData()
    }

    override fun voteForSuggestion(suggestionId: Int, voteBody: VoteBody): Single<RepoData<SuggestionResponse?>> {
        return api.voteForSuggestion(suggestionId, voteBody).mapToRepoData()
    }

    override fun login(loginBody: LoginBody): Single<RepoData<LoginResponse?>> {
        return api.postLogin(loginBody).mapToRepoData(
            success = { result ->
                result?.let {
                    profileManager.updateProfile(Profile(loginBody.name, result.token))
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
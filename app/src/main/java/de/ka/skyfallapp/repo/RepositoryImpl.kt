package de.ka.skyfallapp.repo

import android.app.Application
import de.ka.skyfallapp.repo.api.*

import de.ka.skyfallapp.repo.db.AppDatabase

import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
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

    override val observableConsensuses = BehaviorSubject.createDefault(mutableListOf<ConsensusResponse>())

    override fun getPersonalConsensus(): Single<RepoData<List<ConsensusResponse>?>> {
        return api.getPersonalConsensus().mapToRepoData()
    }

    override fun getConsensus(
        resetCurrent: Boolean,
        limit: Int,
        offset: Int
    ): Single<RepoData<List<ConsensusResponse>?>> {
        return api.getConsensus(limit, offset).mapToRepoData(
            success = { result ->
                result?.let {
                    if (resetCurrent) {
                        observableConsensuses.onNext(result.toMutableList())
                    } else {
                        observableConsensuses.value!!.addAll(result)
                        notifyObservableConsensuses()
                    }
                }
            }
        )
    }

    private fun updateObservableConsensuses(consensus: ConsensusResponse) {
        for (index in 0 until observableConsensuses.value!!.size) {
            if (observableConsensuses.value!![index].id == consensus.id) {
                observableConsensuses.value!![index] = consensus
                notifyObservableConsensuses()
                return
            }
        }
    }

    private fun notifyObservableConsensuses() {
        observableConsensuses.onNext(observableConsensuses.value!!)
    }

    override fun getConsensusDetail(consensusId: Int): Single<RepoData<ConsensusResponse?>> {
        return api.getConsensusDetail(consensusId).mapToRepoData(
            success = { result -> result?.let { updateObservableConsensuses(it) } }
        )
    }

    override fun deleteConsensus(consensusId: Int): Single<RepoData<ResponseBody?>> {
        return api.deleteConsensus(consensusId).mapToRepoData(
            success = {
                val item = observableConsensuses.value!!.find { it.id == consensusId }
                observableConsensuses.value!!.remove(item)
                notifyObservableConsensuses()
            }
        )
    }

    override fun updateConsensus(consensusId: Int, consensusBody: ConsensusBody): Single<RepoData<ConsensusResponse?>> {
        return api.updateConsensus(consensusId, consensusBody).mapToRepoData(
            success = { result -> result?.let { updateObservableConsensuses(it) } }
        )
    }

    override fun sendConsensus(consensus: ConsensusBody): Single<RepoData<ConsensusResponse?>> {
        return api.postConsensus(consensus).mapToRepoData(
            success = { result ->
                result?.let {
                    observableConsensuses.value!!.add(result)
                    notifyObservableConsensuses()
                }
            }
        )
    }

    override fun getConsensusSuggestions(consensusId: Int): Single<RepoData<List<SuggestionResponse>?>> {
        return api.getConsensusSuggestions(consensusId).mapToRepoData()
    }

    override fun getSuggestionDetail(consensusId: Int, suggestionId: Int): Single<RepoData<SuggestionResponse?>> {
        return api.getSuggestionDetail(consensusId, suggestionId).mapToRepoData()
    }

    override fun sendSuggestion(
        consensusId: Int,
        suggestionBody: SuggestionBody
    ): Single<RepoData<SuggestionResponse?>> {
        return api.postSuggestion(consensusId, suggestionBody).mapToRepoData()
    }

    override fun updateSuggestion(
        consensusId: Int,
        suggestionId: Int,
        suggestionBody: SuggestionBody
    ): Single<RepoData<SuggestionResponse?>> {
        return api.updateSuggestion(consensusId, suggestionId, suggestionBody).mapToRepoData()
    }

    override fun deleteSuggestion(consensusId: Int, suggestionId: Int): Single<RepoData<ResponseBody?>> {
        return api.deleteSuggestion(consensusId, suggestionId).mapToRepoData()
    }

    override fun voteForSuggestion(
        consensusId: Int,
        suggestionId: Int,
        voteBody: VoteBody
    ): Single<RepoData<SuggestionResponse?>> {
        return api.voteForSuggestion(consensusId, suggestionId, voteBody).mapToRepoData()
    }

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
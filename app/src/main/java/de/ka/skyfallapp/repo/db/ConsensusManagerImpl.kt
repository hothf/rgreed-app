package de.ka.skyfallapp.repo.db

import de.ka.skyfallapp.repo.ConsensusManager
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.*
import de.ka.skyfallapp.repo.mapToRepoData
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import okhttp3.ResponseBody

class ConsensusManagerImpl(val api: ApiService) : ConsensusManager {

    override val observableConsensuses = BehaviorSubject.createDefault(mutableListOf<ConsensusResponse>())
    override val observablePersonalConsensuses = BehaviorSubject.createDefault(mutableListOf<ConsensusResponse>())
    override val observableSuggestions = BehaviorSubject.createDefault(mutableListOf<SuggestionResponse>())

    override fun getPersonalConsensuses(resetCurrent: Boolean): Single<RepoData<List<ConsensusResponse>?>> {
        return api.getPersonalConsensus().mapToRepoData(
            success = { result ->
                result?.let {
                    if (resetCurrent) {
                        observablePersonalConsensuses.onNext(result.toMutableList())
                    } else {
                        observablePersonalConsensuses.value!!.addAll(result)
                        notifyObservablePersonalConsensuses()
                    }
                }
            }
        )
    }

    override fun getConsensuses(
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

    private fun updateAllObservableConsensuses(consensus: ConsensusResponse) {
        for (index in 0 until observableConsensuses.value!!.size) {
            if (observableConsensuses.value!![index].id == consensus.id) {
                observableConsensuses.value!![index] = consensus
                notifyObservableConsensuses()
                break
            }
        }
        for (index in 0 until observablePersonalConsensuses.value!!.size) {
            if (observablePersonalConsensuses.value!![index].id == consensus.id) {
                observablePersonalConsensuses.value!![index] = consensus
                notifyObservablePersonalConsensuses()
                return
            }
        }
    }

    private fun notifyObservableConsensuses() {
        observableConsensuses.onNext(observableConsensuses.value!!)
    }

    private fun notifyObservablePersonalConsensuses() {
        observablePersonalConsensuses.onNext(observablePersonalConsensuses.value!!)
    }

    override fun getConsensusDetail(consensusId: Int): Single<RepoData<ConsensusResponse?>> {
        return api.getConsensusDetail(consensusId).mapToRepoData(
            success = { result -> result?.let { updateAllObservableConsensuses(it) } }
        )
    }

    override fun deleteConsensus(consensusId: Int): Single<RepoData<ResponseBody?>> {
        return api.deleteConsensus(consensusId).mapToRepoData(
            success = {
                val item = observableConsensuses.value!!.find { it.id == consensusId }
                if (observableConsensuses.value!!.remove(item)) notifyObservableConsensuses()

                val personalItem = observablePersonalConsensuses.value!!.find { it.id == consensusId }
                if (observablePersonalConsensuses.value!!.remove(personalItem)) notifyObservablePersonalConsensuses()
            }
        )
    }

    override fun updateConsensus(consensusId: Int, consensusBody: ConsensusBody): Single<RepoData<ConsensusResponse?>> {
        return api.updateConsensus(consensusId, consensusBody).mapToRepoData(
            success = { result -> result?.let { updateAllObservableConsensuses(it) } }
        )
    }

    override fun sendConsensus(consensus: ConsensusBody): Single<RepoData<ConsensusResponse?>> {
        return api.postConsensus(consensus).mapToRepoData(
            success = { result ->
                result?.let {
                    observableConsensuses.value!!.add(0,result)
                    notifyObservableConsensuses()
                    observablePersonalConsensuses.value!!.add(0,result)
                    notifyObservablePersonalConsensuses()
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
}
package de.ka.skyfallapp.repo.db

import de.ka.skyfallapp.repo.ConsensusManager
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.*
import de.ka.skyfallapp.repo.mapToRepoData
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import okhttp3.ResponseBody

class ConsensusManagerImpl(val api: ApiService) : ConsensusManager {

    override val observableConsensuses = PublishSubject.create<MutableList<ConsensusResponse>>()
    override val observablePersonalConsensuses = PublishSubject.create<MutableList<ConsensusResponse>>()
    override val observableSuggestions = PublishSubject.create<MutableList<SuggestionResponse>>()

    private val consensuses = mutableListOf<ConsensusResponse>()
    private val personalConsensuses = mutableListOf<ConsensusResponse>()
    private val suggestions = mutableListOf<SuggestionResponse>()

    override fun getPersonalConsensuses(resetCurrent: Boolean): Single<RepoData<List<ConsensusResponse>?>> {
        return api.getPersonalConsensus().mapToRepoData(
            success = { result ->
                result?.let {
                    if (resetCurrent) {
                        personalConsensuses.clear()
                    }
                    personalConsensuses.addAll(result)
                    notifyObservablePersonalConsensusesChanged()
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
                        consensuses.clear()
                    }
                    consensuses.addAll(result)
                    notifyObservableConsensusesChanged()
                }
            }
        )
    }

    private fun updateAllObservableConsensuses(consensus: ConsensusResponse) {
        for (index in 0 until consensuses.size) {
            if (consensuses[index].id == consensus.id) {
                consensuses[index] = consensus
                notifyObservableConsensusesChanged()
                break
            }
        }
        for (index in 0 until personalConsensuses.size) {
            if (personalConsensuses[index].id == consensus.id) {
                personalConsensuses[index] = consensus
                notifyObservablePersonalConsensusesChanged()
                return
            }
        }
    }

    private fun updateObservableSuggestion(suggestion: SuggestionResponse) {
        for (index in 0 until suggestions.size) {
            if (suggestions[index].id == suggestion.id) {
                suggestions[index] = suggestion
                notifyObservableConsensusesChanged()
                break
            }
        }
    }

    private fun notifyObservableConsensusesChanged() {
        observableConsensuses.onNext(consensuses)
    }

    private fun notifyObservablePersonalConsensusesChanged() {
        observablePersonalConsensuses.onNext(personalConsensuses)
    }

    private fun notifyObservableSuggestionsChanged() {
        observableSuggestions.onNext(suggestions)
    }

    override fun getConsensusDetail(consensusId: Int): Single<RepoData<ConsensusResponse?>> {
        return api.getConsensusDetail(consensusId).mapToRepoData(
            success = { result -> result?.let { updateAllObservableConsensuses(it) } }
        )
    }

    override fun deleteConsensus(consensusId: Int): Single<RepoData<ResponseBody?>> {
        return api.deleteConsensus(consensusId).mapToRepoData(
            success = {
                val item = consensuses.find { it.id == consensusId }
                if (consensuses.remove(item)) notifyObservableConsensusesChanged()

                val personalItem = personalConsensuses.find { it.id == consensusId }
                if (personalConsensuses.remove(personalItem)) notifyObservablePersonalConsensusesChanged()
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
                    consensuses.add(0, result)
                    notifyObservableConsensusesChanged()
                    personalConsensuses.add(0, result)
                    notifyObservablePersonalConsensusesChanged()
                }
            }
        )
    }

    override fun getConsensusSuggestions(consensusId: Int): Single<RepoData<List<SuggestionResponse>?>> {
        return api.getConsensusSuggestions(consensusId).mapToRepoData(
            success = { result ->
                result?.let {
                    suggestions.clear()
                    suggestions.addAll(it)
                    notifyObservableSuggestionsChanged()
                }
            }
        )
    }

    override fun getSuggestionDetail(consensusId: Int, suggestionId: Int): Single<RepoData<SuggestionResponse?>> {
        return api.getSuggestionDetail(consensusId, suggestionId).mapToRepoData(
            success = { result -> result?.let { updateObservableSuggestion(it) } }
        )
    }

    override fun sendSuggestion(
        consensusId: Int,
        suggestionBody: SuggestionBody
    ): Single<RepoData<SuggestionResponse?>> {
        return api.postSuggestion(consensusId, suggestionBody).mapToRepoData(
            success = { result ->
                result?.let {
                    suggestions.add(0, result)
                    notifyObservableSuggestionsChanged()
                }
            }
        )
    }

    override fun updateSuggestion(
        consensusId: Int,
        suggestionId: Int,
        suggestionBody: SuggestionBody
    ): Single<RepoData<SuggestionResponse?>> {
        return api.updateSuggestion(consensusId, suggestionId, suggestionBody).mapToRepoData(
            success = { result -> result?.let { updateObservableSuggestion(it) } }
        )
    }

    override fun deleteSuggestion(consensusId: Int, suggestionId: Int): Single<RepoData<ResponseBody?>> {
        return api.deleteSuggestion(consensusId, suggestionId).mapToRepoData(
            success = {
                val item = suggestions.find { it.id == suggestionId }
                if (suggestions.remove(item)) notifyObservableSuggestionsChanged()
            }
        )
    }

    override fun voteForSuggestion(
        consensusId: Int,
        suggestionId: Int,
        voteBody: VoteBody
    ): Single<RepoData<SuggestionResponse?>> {
        return api.voteForSuggestion(consensusId, suggestionId, voteBody).mapToRepoData(
            success = { result -> result?.let { updateObservableSuggestion(it) } }
        )
    }
}
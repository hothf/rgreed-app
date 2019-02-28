package de.ka.skyfallapp.repo.db

import de.ka.skyfallapp.repo.*
import de.ka.skyfallapp.repo.api.*
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import okhttp3.ResponseBody

class ConsensusManagerImpl(val api: ApiService) : ConsensusManager {

    override val observableConsensuses = PublishSubject.create<InvalidateList<MutableList<ConsensusResponse>>>()
    override val observablePersonalConsensuses = PublishSubject.create<InvalidateList<MutableList<ConsensusResponse>>>()
    override val observableSuggestions = PublishSubject.create<InvalidateList<MutableList<SuggestionResponse>>>()

    private val consensuses = InvalidateList(mutableListOf<ConsensusResponse>())
    private val personalConsensuses = InvalidateList(mutableListOf<ConsensusResponse>())
    private val suggestions = InvalidateList(mutableListOf<SuggestionResponse>())

    override fun getPersonalConsensuses(resetCurrent: Boolean): Single<RepoData<List<ConsensusResponse>?>> {
        return api.getPersonalConsensus().mapToRepoData(
            success = { result ->
                if (result == null || resetCurrent) {
                    personalConsensuses.list.clear()
                }
                result?.let {
                    personalConsensuses.list.addAll(it)
                }
                notifyObservablePersonalConsensusesChanged()
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
                if (result == null || resetCurrent) {
                    consensuses.list.clear()
                }
                result?.let {
                    consensuses.list.addAll(it)
                }
                notifyObservableConsensusesChanged()
            }
        )
    }

    private fun updateAllObservableConsensuses(consensus: ConsensusResponse) {
        for (index in 0 until consensuses.list.size) {
            if (consensuses.list[index].id == consensus.id) {
                consensuses.list[index] = consensus
                notifyObservableConsensusesChanged()
                break
            }
        }
        for (index in 0 until personalConsensuses.list.size) {
            if (personalConsensuses.list[index].id == consensus.id) {
                personalConsensuses.list[index] = consensus
                notifyObservablePersonalConsensusesChanged()
                return
            }
        }
    }

    private fun updateObservableSuggestion(suggestion: SuggestionResponse) {
        for (index in 0 until suggestions.list.size) {
            if (suggestions.list[index].id == suggestion.id) {
                suggestions.list[index] = suggestion
                notifyObservableConsensusesChanged()
                break
            }
        }
    }

    private fun notifyObservableConsensusesChanged(invalidate: Boolean = false) {
        consensuses.invalidate = invalidate
        observableConsensuses.onNext(consensuses)
    }

    private fun notifyObservablePersonalConsensusesChanged(invalidate: Boolean = false) {
        personalConsensuses.invalidate = invalidate
        observablePersonalConsensuses.onNext(personalConsensuses)
    }

    private fun notifyObservableSuggestionsChanged(invalidate: Boolean = false) {
        suggestions.invalidate = invalidate
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
                val item = consensuses.list.find { it.id == consensusId }
                if (consensuses.list.remove(item)) notifyObservableConsensusesChanged()

                val personalItem = personalConsensuses.list.find { it.id == consensusId }
                if (personalConsensuses.list.remove(personalItem)) notifyObservablePersonalConsensusesChanged()
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
                    consensuses.list.add(0, result)
                    notifyObservableConsensusesChanged()
                    personalConsensuses.list.add(0, result)
                    notifyObservablePersonalConsensusesChanged()
                }
            }
        )
    }

    override fun getConsensusSuggestions(consensusId: Int): Single<RepoData<List<SuggestionResponse>?>> {
        return api.getConsensusSuggestions(consensusId).mapToRepoData(
            success = { result ->
                result?.let {
                    suggestions.list.clear()
                    suggestions.list.addAll(it)
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
                    suggestions.list.add(0, result)
                    notifyObservableSuggestionsChanged(invalidate = true)
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
                val item = suggestions.list.find { it.id == suggestionId }
                if (suggestions.list.remove(item)) notifyObservableSuggestionsChanged()
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
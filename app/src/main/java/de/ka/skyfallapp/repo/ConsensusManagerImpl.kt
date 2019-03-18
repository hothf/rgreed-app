package de.ka.skyfallapp.repo

import de.ka.skyfallapp.repo.api.*
import de.ka.skyfallapp.repo.api.models.*
import de.ka.skyfallapp.utils.ApiErrorManager
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import okhttp3.ResponseBody

class ConsensusManagerImpl(
    val api: ApiService,
    val apiErrorHandler: ApiErrorManager,
    override val searchManager: SearchManager
) : ConsensusManager {

    override val observableConsensuses =
        PublishSubject.create<InvalidateList<ConsensusResponse, List<ConsensusResponse>>>()
    override val observablePersonalConsensuses =
        PublishSubject.create<InvalidateList<ConsensusResponse, List<ConsensusResponse>>>()
    override val observableSuggestions =
        PublishSubject.create<InvalidateList<SuggestionResponse, List<SuggestionResponse>>>()

    private val consensuses = mutableListOf<ConsensusResponse>()
    private val personalConsensuses = mutableListOf<ConsensusResponse>()
    private val suggestions = mutableListOf<SuggestionResponse>()

    override fun getPersonalConsensuses(
        resetCurrent: Boolean,
        limit: Int,
        offset: Int,
        finished: Boolean?
    ): Single<RepoData<List<ConsensusResponse>?>> {
        return api.getPersonalConsensus(limit, offset, finished).mapToRepoData(
            success = { result ->
                if (result == null || resetCurrent) {
                    personalConsensuses.clear()
                }
                result?.let {
                    personalConsensuses.addAll(it)
                }
                notifyObservablePersonalConsensusesChanged()
            }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable, silenceUnAuthorized = true) }
    }

    override fun getConsensuses(
        resetCurrent: Boolean,
        limit: Int,
        offset: Int,
        finished: Boolean?
    ): Single<RepoData<List<ConsensusResponse>?>> {
        return api.getConsensus(limit, offset, finished).mapToRepoData(
            success = { result ->
                if (result == null || resetCurrent) {
                    consensuses.clear()
                }
                result?.let {
                    consensuses.addAll(it)
                }
                notifyObservableConsensusesChanged()
            }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    private fun updateAllObservableConsensuses(consensus: ConsensusResponse) {
        var isInConsensusList = false
        for (index in 0 until consensuses.size) {
            if (consensuses[index].id == consensus.id) {
                consensuses[index] = consensus
                notifyObservableConsensusesChanged()
                isInConsensusList = true
                break
            }
        }
        var isInPersonalList = false
        for (index in 0 until personalConsensuses.size) {
            if (personalConsensuses[index].id == consensus.id) {
                personalConsensuses[index] = consensus
                notifyObservablePersonalConsensusesChanged()
                isInPersonalList = true
                break
            }
        }

        if (!isInConsensusList) {
            notifyObservableConsensusesChanged(invalidate = true, item = consensus)
        }

        if (!isInPersonalList) {
            notifyObservablePersonalConsensusesChanged(invalidate = true, item = consensus)
        }
    }

    private fun updateObservableSuggestion(suggestion: SuggestionResponse) {
        for (index in 0 until suggestions.size) {
            if (suggestions[index].id == suggestion.id) {
                suggestions[index] = suggestion
                notifyObservableSuggestionsChanged()
                break
            }
        }
    }

    private fun notifyObservableConsensusesChanged(invalidate: Boolean = false, item: ConsensusResponse? = null) {
        observableConsensuses.onNext(InvalidateList(consensuses.toList(), invalidate, item))
    }

    private fun notifyObservablePersonalConsensusesChanged(
        invalidate: Boolean = false,
        item: ConsensusResponse? = null
    ) {
        observablePersonalConsensuses.onNext(InvalidateList(personalConsensuses.toList(), invalidate, item))
    }

    private fun notifyObservableSuggestionsChanged(invalidate: Boolean = false, item: SuggestionResponse? = null) {
        observableSuggestions.onNext(InvalidateList(suggestions.toList(), invalidate, item))
    }

    override fun sendConsensusAccessRequest(
        consensusId: Int,
        accessBody: RequestAccessBody
    ): Single<RepoData<ConsensusResponse?>> {
        return api.postConsensusRequestAccess(consensusId, accessBody).mapToRepoData(
            success = { result -> result?.let { updateAllObservableConsensuses(it) } }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun getConsensusDetail(consensusId: Int): Single<RepoData<ConsensusResponse?>> {
        return api.getConsensusDetail(consensusId).mapToRepoData(
            success = { result -> result?.let { updateAllObservableConsensuses(it) } }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun deleteConsensus(consensusId: Int): Single<RepoData<ResponseBody?>> {
        return api.deleteConsensus(consensusId).mapToRepoData(
            success = {
                val item = consensuses.find { it.id == consensusId }
                if (consensuses.remove(item)) notifyObservableConsensusesChanged(item = item)

                val personalItem = personalConsensuses.find { it.id == consensusId }
                if (personalConsensuses.remove(personalItem)) notifyObservablePersonalConsensusesChanged(item = item)
            }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun updateConsensus(consensusId: Int, consensusBody: ConsensusBody): Single<RepoData<ConsensusResponse?>> {
        return api.updateConsensus(consensusId, consensusBody).mapToRepoData(
            success = { result -> result?.let { updateAllObservableConsensuses(it) } }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun sendConsensus(consensus: ConsensusBody): Single<RepoData<ConsensusResponse?>> {
        return api.postConsensus(consensus).mapToRepoData(
            success = { result ->
                result?.let {
                    consensuses.add(0, result)
                    notifyObservableConsensusesChanged(item = it)
                    personalConsensuses.add(0, result)
                    notifyObservablePersonalConsensusesChanged(item = it)
                }
            }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
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
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun getSuggestionDetail(consensusId: Int, suggestionId: Int): Single<RepoData<SuggestionResponse?>> {
        return api.getSuggestionDetail(consensusId, suggestionId).mapToRepoData(
            success = { result -> result?.let { updateObservableSuggestion(it) } }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun sendSuggestion(
        consensusId: Int,
        suggestionBody: SuggestionBody
    ): Single<RepoData<SuggestionResponse?>> {
        return api.postSuggestion(consensusId, suggestionBody).mapToRepoData(
            success = { result ->
                result?.let {
                    suggestions.add(0, result)
                    notifyObservableSuggestionsChanged(invalidate = true)
                }
            }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun updateSuggestion(
        consensusId: Int,
        suggestionId: Int,
        suggestionBody: SuggestionBody
    ): Single<RepoData<SuggestionResponse?>> {
        return api.updateSuggestion(consensusId, suggestionId, suggestionBody).mapToRepoData(
            success = { result -> result?.let { updateObservableSuggestion(it) } }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun deleteSuggestion(consensusId: Int, suggestionId: Int): Single<RepoData<ResponseBody?>> {
        return api.deleteSuggestion(consensusId, suggestionId).mapToRepoData(
            success = {
                val item = suggestions.find { it.id == suggestionId }
                if (suggestions.remove(item)) notifyObservableSuggestionsChanged()
            }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun voteForSuggestion(
        consensusId: Int,
        suggestionId: Int,
        voteBody: VoteBody
    ): Single<RepoData<SuggestionResponse?>> {
        return api.voteForSuggestion(consensusId, suggestionId, voteBody).mapToRepoData(
            success = { result -> result?.let { notifyObservableSuggestionsChanged(invalidate = true) } }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }


}
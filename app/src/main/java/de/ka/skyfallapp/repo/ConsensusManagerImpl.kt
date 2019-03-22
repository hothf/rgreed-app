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
    override val observableAdminConsensuses =
        PublishSubject.create<InvalidateList<ConsensusResponse, List<ConsensusResponse>>>()
    override val observableFollowingConsensuses =
        PublishSubject.create<InvalidateList<ConsensusResponse, List<ConsensusResponse>>>()
    override val observableSuggestions =
        PublishSubject.create<InvalidateList<SuggestionResponse, List<SuggestionResponse>>>()

    private val consensuses = mutableListOf<ConsensusResponse>()
    private val adminConsensuses = mutableListOf<ConsensusResponse>()
    private val followingConsensuses = mutableListOf<ConsensusResponse>()
    private val suggestions = mutableListOf<SuggestionResponse>()

    override fun getAdminConsensuses(
        resetCurrent: Boolean,
        limit: Int,
        offset: Int,
        finished: Boolean?
    ): Single<RepoData<List<ConsensusResponse>?>> {
        return api.getAdminConsensus(limit, offset, finished).mapToRepoData(
            success = { result ->
                if (result == null || resetCurrent) {
                    adminConsensuses.clear()
                }
                result?.let {
                    adminConsensuses.addAll(it)
                }
                notifyObservableAdminConsensusesChanged()
            }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable, silenceUnAuthorized = true) }
    }

    override fun getFollowingConsensuses(
        resetCurrent: Boolean,
        limit: Int,
        offset: Int,
        finished: Boolean?
    ): Single<RepoData<List<ConsensusResponse>?>> {
        return api.getFollowingConsensus(limit, offset, finished).mapToRepoData(
            success = { result ->
                if (result == null || resetCurrent) {
                    followingConsensuses.clear()
                }
                result?.let {
                    followingConsensuses.addAll(it)
                }
                notifyObservableFollowingConsensusesChanged()
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
        var isInAdminList = false
        for (index in 0 until adminConsensuses.size) {
            if (adminConsensuses[index].id == consensus.id) {
                adminConsensuses[index] = consensus
                notifyObservableAdminConsensusesChanged()
                isInAdminList = true
                break
            }
        }
        var isInFollowingList = false
        for (index in 0 until followingConsensuses.size) {
            if (followingConsensuses[index].id == consensus.id) {
                followingConsensuses[index] = consensus
                notifyObservableFollowingConsensusesChanged()
                isInFollowingList = true
                break
            }
        }

        if (!isInConsensusList) {
            notifyObservableConsensusesChanged(invalidate = true, item = consensus)
        }

        if (!isInAdminList) {
            notifyObservableAdminConsensusesChanged(invalidate = true, item = consensus)
        }

        if (!isInFollowingList) {
            notifyObservableFollowingConsensusesChanged(inv = true, item = consensus)
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

    private fun notifyObservableAdminConsensusesChanged(invalidate: Boolean = false, item: ConsensusResponse? = null) {
        observableAdminConsensuses.onNext(InvalidateList(adminConsensuses.toList(), invalidate, item))
    }

    private fun notifyObservableFollowingConsensusesChanged(inv: Boolean = false, item: ConsensusResponse? = null) {
        observableFollowingConsensuses.onNext(InvalidateList(followingConsensuses.toList(), inv, item))
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

    override fun postFollowConsensus(consensusId: Int, followBody: FollowBody): Single<RepoData<ConsensusResponse?>> {
        return api.followConsensus(consensusId, followBody).mapToRepoData(
            success = { result -> result?.let(::updateAllObservableConsensuses) }
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

                val adminItem = adminConsensuses.find { it.id == consensusId }
                if (adminConsensuses.remove(adminItem)) notifyObservableAdminConsensusesChanged(item = item)

                val followItem = followingConsensuses.find { it.id == consensusId }
                if (followingConsensuses.remove(followItem)) notifyObservableFollowingConsensusesChanged(item = item)
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
                    adminConsensuses.add(0, result)
                    notifyObservableAdminConsensusesChanged(item = it)
                    // following does not have to be added because you can't auto follow on add
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
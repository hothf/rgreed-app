package de.ka.rgreed.repo

import de.ka.rgreed.repo.api.*
import de.ka.rgreed.repo.api.models.*
import de.ka.rgreed.utils.ApiErrorManager
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import okhttp3.ResponseBody

/**
 * A implementation for consensus management.
 * This offers different observables which can be subscribed to.
 *
 *
 * With this manager, the [api] can be consumed with the offered public methods. To recognise changes of data, simply
 * subscribe to the observables. A wrapper class ([IndicatedList]) is used as a data stream emitted via the observables
 * which contains a List of changed data  and and offers convenience indicators which directly tell how it has changed,
 * e.g. something has been **added, removed, updated** or if the items **previously loaded should be invalidated**.
 */
class ConsensusManagerImpl(
    val api: ApiService,
    val apiErrorHandler: ApiErrorManager,
    override val searchManager: SearchManager
) : ConsensusManager {

    override val observableConsensuses =
        PublishSubject.create<IndicatedList<ConsensusResponse, List<ConsensusResponse>>>()
    override val observableSuggestions =
        PublishSubject.create<IndicatedList<SuggestionResponse, List<SuggestionResponse>>>()

    override fun getAdminConsensuses(
        limit: Int,
        offset: Int,
        finished: Boolean?
    ): Single<RepoData<List<ConsensusResponse>?>> {
        return api.getAdminConsensus(limit, offset, finished).mapToRepoData(
            success = { result -> result?.let { notifyObservableConsensusesChanged(it) } }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable, silenceUnAuthorized = true) }
    }

    override fun getFollowingConsensuses(
        limit: Int,
        offset: Int,
        finished: Boolean?
    ): Single<RepoData<List<ConsensusResponse>?>> {
        return api.getFollowingConsensus(limit, offset, finished).mapToRepoData(
            success = { result ->
                result?.let {
                    notifyObservableConsensusesChanged(it)

                }
            }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable, silenceUnAuthorized = true) }
    }

    override fun getConsensuses(
        limit: Int,
        offset: Int,
        finished: Boolean?
    ): Single<RepoData<List<ConsensusResponse>?>> {
        return api.getConsensus(limit, offset, finished).mapToRepoData(
            success = { result -> result?.let { notifyObservableConsensusesChanged(it) } }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun sendConsensusAccessRequest(
        consensusId: Int,
        accessBody: RequestAccessBody
    ): Single<RepoData<ConsensusResponse?>> {
        return api.postConsensusRequestAccess(consensusId, accessBody).mapToRepoData(
            success = { result ->
                result?.let { notifyObservableConsensusesChanged(listOf(it), update = true) }
            }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun postFollowConsensus(consensusId: Int, followBody: FollowBody): Single<RepoData<ConsensusResponse?>> {
        return api.followConsensus(consensusId, followBody).mapToRepoData(
            success = { result ->
                result?.let {
                    notifyObservableConsensusesChanged(listOf(it), update = true)
                }
            }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun getConsensusDetail(consensusId: Int): Single<RepoData<ConsensusResponse?>> {
        return api.getConsensusDetail(consensusId).mapToRepoData(
            success = { result -> result?.let { notifyObservableConsensusesChanged(listOf(it), update = true) } }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun deleteConsensus(consensusId: Int): Single<RepoData<ResponseBody?>> {
        return api.deleteConsensus(consensusId).mapToRepoData(
            success = {
                notifyObservableConsensusesChanged(listOf(ConsensusResponse(id = consensusId)), remove = true)
            }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun updateConsensus(consensusId: Int, consensusBody: ConsensusBody): Single<RepoData<ConsensusResponse?>> {
        return api.updateConsensus(consensusId, consensusBody).mapToRepoData(
            success = { result ->
                result?.let {
                    notifyObservableConsensusesChanged(listOf(it), update = true)
                    notifyObservableSuggestionsChanged(listOf(), invalidate = true)
                }
            }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun sendConsensus(consensus: ConsensusBody): Single<RepoData<ConsensusResponse?>> {
        return api.postConsensus(consensus).mapToRepoData(
            success = { result -> result?.let { notifyObservableConsensusesChanged(listOf(it), addToTop = true) } }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun getConsensusSuggestions(consensusId: Int): Single<RepoData<List<SuggestionResponse>?>> {
        return api.getConsensusSuggestions(consensusId).mapToRepoData(
            success = { result ->
                result?.let {
                    notifyObservableSuggestionsChanged(it)
                }
            }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun getSuggestionDetail(consensusId: Int, suggestionId: Int): Single<RepoData<SuggestionResponse?>> {
        return api.getSuggestionDetail(consensusId, suggestionId).mapToRepoData(
            success = { result -> result?.let { notifyObservableSuggestionsChanged(listOf(it), update = true) } }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun sendSuggestion(
        consensusId: Int,
        suggestionBody: SuggestionBody
    ): Single<RepoData<SuggestionResponse?>> {
        return api.postSuggestion(consensusId, suggestionBody).mapToRepoData(
            success = { result -> result?.let { notifyObservableSuggestionsChanged(listOf(it), addToTop = true) } }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun updateSuggestion(
        consensusId: Int,
        suggestionId: Int,
        suggestionBody: SuggestionBody
    ): Single<RepoData<SuggestionResponse?>> {
        return api.updateSuggestion(consensusId, suggestionId, suggestionBody).mapToRepoData(
            success = { result -> result?.let { notifyObservableSuggestionsChanged(listOf(it), update = true) } }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun deleteSuggestion(consensusId: Int, suggestionId: Int): Single<RepoData<ResponseBody?>> {
        return api.deleteSuggestion(consensusId, suggestionId).mapToRepoData(
            success = {
                notifyObservableSuggestionsChanged(items = listOf(SuggestionResponse(suggestionId)), remove = true)
            }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun voteForSuggestion(
        consensusId: Int,
        suggestionId: Int,
        voteBody: VoteBody
    ): Single<RepoData<SuggestionResponse?>> {
        return api.voteForSuggestion(consensusId, suggestionId, voteBody).mapToRepoData(
            success = { result ->
                result?.let { notifyObservableSuggestionsChanged(invalidate = true, items = listOf()) }
            }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    private fun notifyObservableConsensusesChanged(
        items: List<ConsensusResponse>,
        invalidate: Boolean = false,
        remove: Boolean = false,
        addToTop: Boolean = false,
        update: Boolean = false
    ) {
        observableConsensuses.onNext(IndicatedList(items, invalidate, remove, addToTop, update))
    }

    private fun notifyObservableSuggestionsChanged(
        items: List<SuggestionResponse>,
        invalidate: Boolean = false,
        remove: Boolean = false,
        addToTop: Boolean = false,
        update: Boolean = false
    ) {
        observableSuggestions.onNext(IndicatedList(items, invalidate, remove, addToTop, update))
    }
}
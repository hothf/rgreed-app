package de.ka.rgreed.repo

import de.ka.rgreed.repo.api.models.*
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.ResponseBody

/**
 * A [MutableList] with an optional [invalidate] flag for giving the hint, that the list has invalidated data and
 * should be re-fetched.
 * A optional [item] can be defined to mark a single item that has been changed. This item is only set when the list
 * is likely not to contain the item.
 */
data class InvalidateList<E : Any, T : List<E>>(val list: T, var invalidate: Boolean = false, val item: E? = null)

/**
 * The consensus manager offers access to several [Observable] lists of consensus data.
 * All data streams of [ConsensusResponse] can be fetched here.
 */
interface ConsensusManager {

    /**
     * A search manager for doing and observing searches.
     */
    val searchManager: SearchManager

    /**
     * Observes consensus data.
     */
    val observableConsensuses: Observable<InvalidateList<ConsensusResponse, List<ConsensusResponse>>>

    /**
     * Observes consensus data, only containing data linked to a admin user.
     */
    val observableAdminConsensuses: Observable<InvalidateList<ConsensusResponse, List<ConsensusResponse>>>

    /**
     * Observes consensus data, only containing data to a user following consensuses.
     */
    val observableFollowingConsensuses: Observable<InvalidateList<ConsensusResponse, List<ConsensusResponse>>>

    /**
     * Observes suggestions of a consensus.
     */
    val observableSuggestions: Observable<InvalidateList<SuggestionResponse, List<SuggestionResponse>>>

    /**
     * Tries to find a previously downloaded consensus. If not available, returns null
     */
    fun findPreviouslyDownloadedConsensus(consensusId: Int): ConsensusResponse?

    /**
     * Retrieves a list of all admin consensuses where the user is an admin.
     */
    fun getAdminConsensuses(
        resetCurrent: Boolean,
        limit: Int,
        offset: Int,
        finished: Boolean? = null
    ): Single<RepoData<List<ConsensusResponse>?>>

    /**
     * Retrieves a list of all consensuses where the user is a follower.
     */
    fun getFollowingConsensuses(
        resetCurrent: Boolean,
        limit: Int,
        offset: Int,
        finished: Boolean? = null
    ): Single<RepoData<List<ConsensusResponse>?>>

    /**
     * Retrieves a list of all consensus.
     */
    fun getConsensuses(
        resetCurrent: Boolean,
        limit: Int,
        offset: Int,
        finished: Boolean? = null
    ): Single<RepoData<List<ConsensusResponse>?>>

    /**
     * Follows or un-follows a consensus, marking or un-marking a consensus as following.
     */
    fun postFollowConsensus(consensusId: Int, followBody: FollowBody): Single<RepoData<ConsensusResponse?>>

    /**
     * Retrieves the details of a consensus.
     */
    fun getConsensusDetail(consensusId: Int): Single<RepoData<ConsensusResponse?>>

    /**
     * Deletes the given consensus.
     */
    fun deleteConsensus(consensusId: Int): Single<RepoData<ResponseBody?>>

    /**
     * Updates a consensus
     */
    fun updateConsensus(consensusId: Int, consensusBody: ConsensusBody): Single<RepoData<ConsensusResponse?>>

    /**
     * Sends a consensus.
     */
    fun sendConsensus(consensus: ConsensusBody): Single<RepoData<ConsensusResponse?>>

    /**
     * Get suggestion details.
     */
    fun getSuggestionDetail(consensusId: Int, suggestionId: Int): Single<RepoData<SuggestionResponse?>>

    /**
     * Sends a suggestion.
     */
    fun sendSuggestion(consensusId: Int, suggestionBody: SuggestionBody): Single<RepoData<SuggestionResponse?>>

    /**
     * Updates a suggestion
     */
    fun updateSuggestion(
        consensusId: Int,
        suggestionId: Int,
        suggestionBody: SuggestionBody
    ): Single<RepoData<SuggestionResponse?>>

    /**
     * Deletes a suggestion
     */
    fun deleteSuggestion(consensusId: Int, suggestionId: Int): Single<RepoData<ResponseBody?>>

    /**
     * Votes for a suggestion.
     */
    fun voteForSuggestion(
        consensusId: Int,
        suggestionId: Int,
        voteBody: VoteBody
    ): Single<RepoData<SuggestionResponse?>>

    /**
     * Gets all consensus suggestions.
     */
    fun getConsensusSuggestions(consensusId: Int): Single<RepoData<List<SuggestionResponse>?>>

    /**
     * Sends a request for accessing a consensus. Only useful for consensuses set to 'private'.
     */
    fun sendConsensusAccessRequest(
        consensusId: Int,
        accessBody: RequestAccessBody
    ): Single<RepoData<ConsensusResponse?>>
}


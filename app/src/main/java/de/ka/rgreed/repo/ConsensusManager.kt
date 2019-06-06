package de.ka.rgreed.repo

import de.ka.rgreed.repo.api.models.*
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.ResponseBody

/**
 * A [List] container.
 *
 *
 * With an optional [invalidate] flag for giving the hint, that the list has invalidated data and
 * should be re-fetched.
 * A optional [remove] flag can be used to indicate, that the list contains items to be removed.
 * A optional [addToTop] flag can be used to indicate, that new items should be added to the top (instead of default
 * behaviour, which may be bottom).
 * A optional [update] flag can be used to indicate, that the list should only be updated and not extended or
 * manipulated somehow differently
 *
 * All flags default to **false** for a simple list indication, that could contain updated and new data.
 */
data class IndicatedList<E : Any, T : List<E>>(
    val list: T,
    var invalidate: Boolean = false,
    var remove: Boolean = false,
    var addToTop: Boolean = false,
    var update: Boolean = false
)

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
    val observableConsensuses: Observable<IndicatedList<ConsensusResponse, List<ConsensusResponse>>>

    /**
     * Observes suggestions of a consensus.
     */
    val observableSuggestions: Observable<IndicatedList<SuggestionResponse, List<SuggestionResponse>>>

    /**
     * Retrieves a list of all admin consensuses where the user is an admin.
     */
    fun getAdminConsensuses(
        limit: Int,
        offset: Int,
        finished: Boolean? = null
    ): Single<RepoData<List<ConsensusResponse>?>>

    /**
     * Retrieves a list of all consensuses where the user is a follower.
     */
    fun getFollowingConsensuses(
        limit: Int,
        offset: Int,
        finished: Boolean? = null
    ): Single<RepoData<List<ConsensusResponse>?>>

    /**
     * Retrieves a list of all consensus.
     */
    fun getConsensuses(
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


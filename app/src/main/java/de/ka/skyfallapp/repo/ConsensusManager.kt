package de.ka.skyfallapp.repo

import de.ka.skyfallapp.repo.api.*
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.ResponseBody

interface ConsensusManager {

    val observableConsensuses: Observable<MutableList<ConsensusResponse>>

    /**
     * Retrieves a list of all personal consensus where the user is an admin, has created a suggestion or voted on one.
     */
    fun getPersonalConsensus(): Single<RepoData<List<ConsensusResponse>?>>

    /**
     * Retrieves a list of all consensus.
     */
    fun getConsensus(resetCurrent: Boolean, limit: Int, offset: Int): Single<RepoData<List<ConsensusResponse>?>>

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
}


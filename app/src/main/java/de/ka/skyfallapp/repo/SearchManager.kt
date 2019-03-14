package de.ka.skyfallapp.repo

import de.ka.skyfallapp.repo.api.models.ConsensusResponse
import de.ka.skyfallapp.repo.db.SearchHistoryDao
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Allows for searching consensuses.
 */
interface SearchManager {

    /**
     * Observes search consensus data.
     */
    val observableSearchResults: Observable<List<ConsensusResponse>>

    /**
     * Observes search history data.
     */
    val observableSearchHistory: Observable<List<SearchHistoryDao>>

    /**
     * Searches with the given query for consensuses.
     *
     * @param query the search query
     */
    fun search(query: String): Single<RepoData<List<ConsensusResponse>?>>

    /**
     * Clears the current search results.
     */
    fun clearSearchResults()

    /**
     * Loads the search history.
     */
    fun loadSearchHistory()

    /**
     * Deletes a history string.
     */
    fun deleteSearchHistory(history: String)
}
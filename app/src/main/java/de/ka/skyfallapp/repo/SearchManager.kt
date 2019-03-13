package de.ka.skyfallapp.repo

import de.ka.skyfallapp.repo.api.models.ConsensusResponse
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Allows for searching consensuses.
 */
interface SearchManager {

    /**
     * Observes consensus data.
     */
    val observableSearchResults: Observable<List<ConsensusResponse>>

    /**
     * Observes the last search queryl
     */
    val observableLastSearchQuery: Observable<String>

    /**
     * Searches with the given query for consensuses.
     *
     * @param query the search query
     */
    fun search(query: String): Single<RepoData<List<ConsensusResponse>?>>

    /**
     * Update the search query string.
     *
     * @param query the query to update to
     */
    fun updateSearchQuery(query: String)
}
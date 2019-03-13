package de.ka.skyfallapp.repo

import de.ka.skyfallapp.repo.api.ApiService
import de.ka.skyfallapp.repo.api.models.ConsensusResponse
import de.ka.skyfallapp.repo.db.SearchHistory
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class SearchManagerImpl(val api: ApiService) : SearchManager {

    override val observableSearchResults: BehaviorSubject<List<ConsensusResponse>> = BehaviorSubject.create()

    override val observableSearchHistory: PublishSubject<List<SearchHistory>> = PublishSubject.create()

    override val observableLastSearchQuery: BehaviorSubject<String> = BehaviorSubject.create()

    private val searchResults = mutableListOf<ConsensusResponse>()

    override fun search(query: String): Single<RepoData<List<ConsensusResponse>?>> {
        return api.searchConsensus(query).mapToRepoData(
            success = { result ->
                result?.let {
                    searchResults.clear()
                    searchResults.addAll(it)
                }
                notifySearchChanged()
            }
        )
    }

    override fun updateSearchQuery(query: String) {
        observableLastSearchQuery.onNext(query)
    }

    override fun loadSearchHistory() {
        observableSearchHistory.onNext(listOf(SearchHistory(12, "what"), SearchHistory(13, "ok")))
    }

    private fun notifySearchChanged() {
        observableSearchResults.onNext(searchResults.toList())
    }
}
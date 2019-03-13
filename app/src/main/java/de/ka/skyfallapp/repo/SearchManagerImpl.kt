package de.ka.skyfallapp.repo

import de.ka.skyfallapp.repo.api.ApiService
import de.ka.skyfallapp.repo.api.models.ConsensusResponse
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject

class SearchManagerImpl(val api: ApiService) : SearchManager {

    override val observableSearchResults: BehaviorSubject<List<ConsensusResponse>> = BehaviorSubject.create()

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

    private fun notifySearchChanged() {
        observableSearchResults.onNext(searchResults.toList())
    }
}
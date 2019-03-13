package de.ka.skyfallapp.repo

import de.ka.skyfallapp.repo.api.ApiService
import de.ka.skyfallapp.repo.api.models.ConsensusResponse
import de.ka.skyfallapp.repo.db.AppDatabase
import de.ka.skyfallapp.repo.db.SearchHistoryDao
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class SearchManagerImpl(val db: AppDatabase, val api: ApiService) : SearchManager {

    override val observableSearchResults: BehaviorSubject<List<ConsensusResponse>> = BehaviorSubject.create()
    override val observableSearchHistory: PublishSubject<List<SearchHistoryDao>> = PublishSubject.create()
    override val observableLastSearchQuery: BehaviorSubject<String> = BehaviorSubject.create()

    private val searchResults = mutableListOf<ConsensusResponse>()
    private val history = mutableListOf<SearchHistoryDao>()

    override fun search(query: String): Single<RepoData<List<ConsensusResponse>?>> {
        return api.searchConsensus(query).mapToRepoData(
            success = { result ->
                result?.let {
                    searchResults.clear()
                    searchResults.addAll(it)
                    notifySearchChanged()

                    if (!it.isEmpty()) {
                        addToHistoryAndNotifyChanged(query)
                    }
                }

            }
        )
    }

    override fun notifySearchQueryChanged(query: String) {
        observableLastSearchQuery.onNext(query)
    }

    override fun loadSearchHistory() {
        val historyBox: Box<SearchHistoryDao> = db.get().boxFor()

        history.clear()
        history.addAll(historyBox.all.reversed())

        notifyHistoryChanged()
    }

    private fun notifySearchChanged() {
        observableSearchResults.onNext(searchResults.toList())
    }

    private fun notifyHistoryChanged() {
        observableSearchHistory.onNext(history.toList())
    }

    private fun addToHistoryAndNotifyChanged(query: String) {
        if (history.find { it.text == query } == null) {
            val historyBox: Box<SearchHistoryDao> = db.get().boxFor()
            val historyDao = SearchHistoryDao(0, text = query)
            historyBox.put(historyDao)
            history.add(0, historyDao)

            //TODO auto remove first item when adding this last item

            notifyHistoryChanged()
        } else {
            //TODO rearrange if already in list to appear on top!
        }
    }
}
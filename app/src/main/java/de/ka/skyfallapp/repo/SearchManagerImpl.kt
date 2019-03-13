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
import de.ka.skyfallapp.repo.db.SearchHistoryDao_


class SearchManagerImpl(val db: AppDatabase, val api: ApiService) : SearchManager {

    override val observableSearchResults: BehaviorSubject<List<ConsensusResponse>> = BehaviorSubject.create()
    override val observableSearchHistory: PublishSubject<List<SearchHistoryDao>> = PublishSubject.create()
    override val observableLastSearchQuery: BehaviorSubject<String> = BehaviorSubject.create()

    private val searchResults = mutableListOf<ConsensusResponse>()

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

    override fun deleteSearchHistory(history: String) {
        val historyBox: Box<SearchHistoryDao> = db.get().boxFor()
        val foundHistory = historyBox.query().equal(SearchHistoryDao_.text, history).build().find()
        historyBox.remove(foundHistory)

        notifyHistoryChanged()
    }

    override fun notifySearchQueryChanged(query: String) {
        observableLastSearchQuery.onNext(query)
    }

    override fun loadSearchHistory() {

        notifyHistoryChanged()
    }

    private fun notifySearchChanged() {
        observableSearchResults.onNext(searchResults.toList())
    }

    private fun notifyHistoryChanged() {
        val historyBox: Box<SearchHistoryDao> = db.get().boxFor()
        observableSearchHistory.onNext(historyBox.all.reversed())
    }

    private fun addToHistoryAndNotifyChanged(query: String) {
        val historyBox: Box<SearchHistoryDao> = db.get().boxFor()
        val foundHistoryDao = historyBox.query().equal(SearchHistoryDao_.text, query).build().findFirst()

        // save new
        val newHistoryDao = SearchHistoryDao(0, text = query)
        historyBox.put(newHistoryDao)

        // delete oldest, if exceeding [MAX_HISTORY_SIZE]
        if (historyBox.all.size > MAX_HISTORY_SIZE) {
            val lastHistoryDao = historyBox.all.first()
            historyBox.remove(lastHistoryDao)
        }

        // or rearrange if already present
        if (foundHistoryDao != null) {
            historyBox.remove(foundHistoryDao)
        }

        notifyHistoryChanged()
    }

    companion object {
        const val MAX_HISTORY_SIZE = 50
    }
}
package de.ka.rgreed.repo

import de.ka.rgreed.repo.api.ApiService
import de.ka.rgreed.repo.api.models.ConsensusResponse
import de.ka.rgreed.repo.db.AppDatabase
import de.ka.rgreed.repo.db.SearchHistoryDao
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import de.ka.rgreed.repo.db.SearchHistoryDao_
import de.ka.rgreed.utils.ApiErrorManager


class SearchManagerImpl(val db: AppDatabase, val api: ApiService, val apiErrorHandler: ApiErrorManager) : SearchManager {

    override val observableSearchResults: BehaviorSubject<List<ConsensusResponse>> = BehaviorSubject.create()
    override val observableSearchHistory: PublishSubject<List<SearchHistoryDao>> = PublishSubject.create()

    override fun search(query: String): Single<RepoData<List<ConsensusResponse>?>> {
        return api.searchConsensus(query).mapToRepoData(
            success = { result ->
                result?.let {
                    notifySearchChanged(it)
                    if (it.isNotEmpty()) {
                        addToHistoryAndNotifyChanged(query)
                    }
                }

            }
        ).doOnEvent { result, throwable -> apiErrorHandler.handle(result, throwable) }
    }

    override fun deleteSearchHistory(history: String) {
        val historyBox: Box<SearchHistoryDao> = db.get().boxFor()
        val foundHistory = historyBox.query().equal(SearchHistoryDao_.text, history).build().find()
        historyBox.remove(foundHistory)

        notifySearchHistoryChanged()
    }

    override fun clearSearchResults() {
        notifySearchChanged(listOf())
    }

    override fun loadSearchHistory() {
        notifySearchHistoryChanged()
    }

    private fun notifySearchChanged(items: List<ConsensusResponse>) {
        observableSearchResults.onNext(items)
    }

    private fun notifySearchHistoryChanged() {
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

        notifySearchHistoryChanged()
    }

    companion object {
        const val MAX_HISTORY_SIZE = 50
    }
}
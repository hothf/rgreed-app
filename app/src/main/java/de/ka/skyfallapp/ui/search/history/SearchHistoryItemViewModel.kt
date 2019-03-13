package de.ka.skyfallapp.ui.search.history

import de.ka.skyfallapp.base.BaseItemViewModel
import de.ka.skyfallapp.repo.db.SearchHistory

class SearchHistoryItemViewModel(val item: SearchHistory, val listener: (SearchHistoryItemViewModel) -> Unit) :
    BaseItemViewModel() {

    val text = item.text

    fun onItemClick() {
        listener(this)
    }

}
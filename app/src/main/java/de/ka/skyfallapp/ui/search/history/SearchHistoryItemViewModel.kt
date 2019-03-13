package de.ka.skyfallapp.ui.search.history

import de.ka.skyfallapp.base.BaseItemViewModel
import de.ka.skyfallapp.repo.db.SearchHistoryDao

class SearchHistoryItemViewModel(
    val item: SearchHistoryDao,
    val listener: (SearchHistoryItemViewModel) -> Unit,
    val deleteClickListener: (SearchHistoryItemViewModel) -> Unit
) :
    BaseItemViewModel() {

    val text = item.text

    fun onItemClick() {
        listener(this)
    }

    fun onDeleteClick() {
        deleteClickListener(this)
    }
}
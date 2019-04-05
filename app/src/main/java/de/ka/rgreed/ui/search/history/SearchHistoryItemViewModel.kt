package de.ka.rgreed.ui.search.history

import de.ka.rgreed.base.BaseItemViewModel
import de.ka.rgreed.repo.db.SearchHistoryDao

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
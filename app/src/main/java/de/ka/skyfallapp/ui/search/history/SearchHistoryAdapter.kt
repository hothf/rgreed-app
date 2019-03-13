package de.ka.skyfallapp.ui.search.history

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import de.ka.skyfallapp.base.BaseAdapter
import de.ka.skyfallapp.base.BaseViewHolder
import de.ka.skyfallapp.databinding.ItemSearchHistoryBinding
import de.ka.skyfallapp.repo.db.SearchHistory

class SearchHistoryAdapter(owner: LifecycleOwner, list: ArrayList<SearchHistoryItemViewModel> = arrayListOf()) :
    BaseAdapter<SearchHistoryItemViewModel>(owner, list, SearchHistoryAdapterDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return BaseViewHolder(ItemSearchHistoryBinding.inflate(layoutInflater, parent, false))
    }

    /**
     * Inserts the given items to the list.
     *
     * @param newItems the new items to append or replace
     * @param itemClickListener a click listener for individual items
     */
    fun insert(newItems: List<SearchHistory>, itemClickListener: (SearchHistoryItemViewModel) -> Unit) {
        setItems(newItems.map { history ->
            SearchHistoryItemViewModel(history, itemClickListener)
        })
    }
}

class SearchHistoryAdapterDiffCallBack : DiffUtil.ItemCallback<SearchHistoryItemViewModel>() {

    override fun areItemsTheSame(oldItem: SearchHistoryItemViewModel, newItem: SearchHistoryItemViewModel): Boolean {
        return oldItem.item.id == newItem.item.id
    }

    override fun areContentsTheSame(
        oldItem: SearchHistoryItemViewModel,
        newItem: SearchHistoryItemViewModel
    ): Boolean {
        return oldItem == newItem
    }
}

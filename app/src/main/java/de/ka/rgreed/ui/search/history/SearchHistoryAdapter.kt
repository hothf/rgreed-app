package de.ka.rgreed.ui.search.history

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import de.ka.rgreed.base.BaseAdapter
import de.ka.rgreed.base.BaseViewHolder
import de.ka.rgreed.databinding.ItemSearchHistoryBinding
import de.ka.rgreed.repo.db.SearchHistoryDao

class SearchHistoryAdapter(list: ArrayList<SearchHistoryItemViewModel> = arrayListOf()) :
    BaseAdapter<SearchHistoryItemViewModel>(list, SearchHistoryAdapterDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return BaseViewHolder(ItemSearchHistoryBinding.inflate(layoutInflater, parent, false))
    }

    /**
     * Overwrites the current list with the [newItems] and applies a [itemClickListener] and [deleteClickListener].
     *
     * @param newItems the new items to append or replace
     * @param itemClickListener a click listener for individual items
     */
    fun overwriteList(
        newItems: List<SearchHistoryDao>,
        itemClickListener: (SearchHistoryItemViewModel) -> Unit,
        deleteClickListener: (SearchHistoryItemViewModel) -> Unit
    ) {
        setItems(newItems.map { history ->
            SearchHistoryItemViewModel(history, itemClickListener, deleteClickListener)
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

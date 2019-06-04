package de.ka.rgreed.ui.consensus.consensuslist

import android.view.View

import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import de.ka.rgreed.base.BaseAdapter
import de.ka.rgreed.base.BaseViewHolder
import de.ka.rgreed.databinding.ItemConsensusBinding
import de.ka.rgreed.repo.api.models.ConsensusResponse

/**
 * Adapter for displaying [ConsensusItemViewModel]s out of [ConsensusResponse]s.
 */
class ConsensusAdapter(owner: LifecycleOwner, list: ArrayList<ConsensusItemViewModel> = arrayListOf()) :
    BaseAdapter<ConsensusItemViewModel>(owner, list, HomeAdapterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return BaseViewHolder(ItemConsensusBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        getItems()[position].apply {
            DataBindingUtil.getBinding<ItemConsensusBinding>(holder.itemView)?.let { binding ->
                val sharedTransitionView = binding.itemContainer
                ViewCompat.setTransitionName(sharedTransitionView, this.item.id.toString())
                binding.itemContainer.setOnClickListener {
                    listener(this, sharedTransitionView)
                }
            }
        }

        super.onBindViewHolder(holder, position)
    }

    /**
     * Inserts the given items to the list.
     *
     * @param newItems the new items to append or replace
     * @param itemClickListener a click listener for individual items
     */
    fun insert(
        newItems: List<ConsensusResponse>,
        itemClickListener: (ConsensusItemViewModel, View) -> Unit
    ) {
        setItems(newItems.map { consensus -> ConsensusItemViewModel(consensus, itemClickListener) })
    }

    /**
     * Removes the specified [itemsToRemove].
     */
    fun remove(itemsToRemove: List<ConsensusResponse>) {
        val items: MutableList<ConsensusItemViewModel> = getItems().toMutableList()

        itemsToRemove.forEach { item ->
            val foundIndex = items.indexOfFirst { it.item.id == item.id }

            if (foundIndex > -1 && items.isNotEmpty()) {
                items.removeAt(foundIndex)
            }
        }

        setItems(items.toList())
    }

    /**
     * Simply adds the given [newItems] to the top of the list and applies a [itemClickListener].
     */
    fun addToTop(newItems: List<ConsensusResponse>, itemClickListener: (ConsensusItemViewModel, View) -> Unit) {
        val items: MutableList<ConsensusItemViewModel> = getItems().toMutableList()

        newItems.forEach { item ->
            items.add(0, ConsensusItemViewModel(item, itemClickListener))
        }

        setItems(items.toList())
    }

    /**
     * Updates items to the current list with [newItems] and applies a [itemClickListener] to them.
     *
     * The [onlyUpdate] flag can be used to also allow for adding items which aren't in the list jet if set to false
     */
    fun addOrUpdate(
        newItems: List<ConsensusResponse>,
        itemClickListener: (ConsensusItemViewModel, View) -> Unit,
        onlyUpdate: Boolean
    ) {
        val items: MutableList<ConsensusItemViewModel> = getItems().toMutableList()

        newItems.forEach { item ->
            val updatedItemIndex = items.indexOfFirst { it.item.id == item.id }

            if (updatedItemIndex > -1 && items.isNotEmpty()) {
                items[updatedItemIndex] = ConsensusItemViewModel(item, itemClickListener)
            } else if (!onlyUpdate) {
                items.add(ConsensusItemViewModel(item, itemClickListener))
            }
        }

        setItems(items.toList())
    }
}

class HomeAdapterDiffCallback : DiffUtil.ItemCallback<ConsensusItemViewModel>() {

    override fun areItemsTheSame(oldItem: ConsensusItemViewModel, newItem: ConsensusItemViewModel): Boolean {
        return oldItem.item.id == newItem.item.id
    }

    override fun areContentsTheSame(
        oldItem: ConsensusItemViewModel,
        newItem: ConsensusItemViewModel
    ): Boolean {
        return oldItem == newItem
    }

}





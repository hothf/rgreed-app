package de.ka.skyfallapp.ui.consensus.consensuslist

import android.view.View

import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import de.ka.skyfallapp.base.BaseAdapter
import de.ka.skyfallapp.base.BaseViewHolder
import de.ka.skyfallapp.databinding.ItemConsensusBinding
import de.ka.skyfallapp.repo.api.models.ConsensusResponse

/**
 * Adapter for displaying [ConsensusItemViewModel]s out of [ConsensusResponse]s.
 */
class HomeAdapter(owner: LifecycleOwner, list: ArrayList<ConsensusItemViewModel> = arrayListOf()) :
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





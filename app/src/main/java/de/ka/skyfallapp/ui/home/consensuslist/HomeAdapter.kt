package de.ka.skyfallapp.ui.home.consensuslist

import android.view.View

import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import de.ka.skyfallapp.base.BaseAdapter
import de.ka.skyfallapp.base.BaseViewHolder
import de.ka.skyfallapp.databinding.ItemHomeBinding
import de.ka.skyfallapp.repo.api.ConsensusResponse

class HomeAdapter(owner: LifecycleOwner, list: ArrayList<HomeItemViewModel> = arrayListOf()) :
    BaseAdapter<HomeItemViewModel>(owner, list, HomeAdapterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return BaseViewHolder(ItemHomeBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {

        getItems()[position].apply {

            if (holder.adapterPosition == itemCount - 1) {
                dividerVisibility.postValue(View.GONE)
            } else {
                dividerVisibility.postValue(View.VISIBLE)
            }

            DataBindingUtil.getBinding<ItemHomeBinding>(holder.itemView)?.let { binding ->
                val sharedTransitionView = binding.itemContainer
                ViewCompat.setTransitionName(sharedTransitionView, this.item.id.toString())
                binding.itemContainer.setOnClickListener {
                    listener(this, sharedTransitionView)
                }
            }
        }

        super.onBindViewHolder(holder, position)
    }

    fun insert(
        append: Boolean,
        newItems: List<ConsensusResponse>,
        itemClickListener: (HomeItemViewModel, View) -> Unit
    ) {
        val newSetItems = newItems.map { consensus -> HomeItemViewModel(consensus, itemClickListener) }

        val newList = if (append) {
            getItems().toMutableList().apply {
                addAll(newSetItems)
                distinctBy { it.item.id }
            }
        } else {
            newSetItems
        }
        setItems(newList)
    }
}


class HomeAdapterDiffCallback : DiffUtil.ItemCallback<HomeItemViewModel>() {

    override fun areItemsTheSame(oldItem: HomeItemViewModel, newItem: HomeItemViewModel): Boolean {
        return oldItem.item.id == newItem.item.id
    }

    override fun areContentsTheSame(
        oldItem: HomeItemViewModel,
        newItem: HomeItemViewModel
    ): Boolean {
        return oldItem == newItem
    }

}





package de.ka.skyfallapp.ui.home.consensuslist

import android.view.View

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.TextViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import de.ka.skyfallapp.R
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

                if (this.item.description.isNullOrBlank()) {
                    TextViewCompat.setTextAppearance(
                        binding.textDescription,
                        R.style.defaultText_Title_Alternative_Italic
                    )
                } else {
                    TextViewCompat.setTextAppearance(binding.textDescription, R.style.defaultText_Title_Alternative)
                }
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
    fun insert(newItems: List<ConsensusResponse>, itemClickListener: (HomeItemViewModel, View) -> Unit) {
        setItems(newItems.map { consensus -> HomeItemViewModel(consensus, itemClickListener) })
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





package de.ka.skyfallapp.ui.personal.consensuslist

import android.view.View

import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import de.ka.skyfallapp.base.BaseAdapter
import de.ka.skyfallapp.base.BaseViewHolder
import de.ka.skyfallapp.databinding.ItemPersonalBinding
import de.ka.skyfallapp.repo.api.ConsensusResponse

class PersonalAdapter(owner: LifecycleOwner, list: ArrayList<PersonalItemViewModel> = arrayListOf()) :
    BaseAdapter<PersonalItemViewModel>(owner, list, PersonalAdapterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return BaseViewHolder(ItemPersonalBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {

        getItems()[position].apply {

            if (holder.adapterPosition == itemCount - 1) {
                dividerVisibility.postValue(View.GONE)
            } else {
                dividerVisibility.postValue(View.VISIBLE)
            }

            DataBindingUtil.getBinding<ItemPersonalBinding>(holder.itemView)?.let { binding ->
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
    fun insert(newItems: List<ConsensusResponse>, itemClickListener: (PersonalItemViewModel, View) -> Unit) {
        setItems(newItems.map { consensus ->
            PersonalItemViewModel(consensus, itemClickListener)
        })
    }
}


class PersonalAdapterDiffCallback : DiffUtil.ItemCallback<PersonalItemViewModel>() {

    override fun areItemsTheSame(oldItem: PersonalItemViewModel, newItem: PersonalItemViewModel): Boolean {
        return oldItem.item.id == newItem.item.id
    }

    override fun areContentsTheSame(
        oldItem: PersonalItemViewModel,
        newItem: PersonalItemViewModel
    ): Boolean {
        return oldItem == newItem
    }

}





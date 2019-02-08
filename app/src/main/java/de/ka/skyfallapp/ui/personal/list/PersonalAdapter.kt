package de.ka.skyfallapp.ui.personal.list

import android.view.View

import android.view.ViewGroup
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
        }

        super.onBindViewHolder(holder, position)
    }

    fun insert(newItems: List<ConsensusResponse>, itemClickListener: (PersonalItemViewModel) -> Unit) {
        addItems(newItems.map { consensus ->
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





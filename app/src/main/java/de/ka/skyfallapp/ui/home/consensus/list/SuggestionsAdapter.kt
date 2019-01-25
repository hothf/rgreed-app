package de.ka.skyfallapp.ui.home.consensus.list

import android.view.View

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import de.ka.skyfallapp.base.BaseAdapter
import de.ka.skyfallapp.base.BaseViewHolder
import de.ka.skyfallapp.databinding.ItemSuggestionsBinding
import de.ka.skyfallapp.databinding.ItemSuggestionsMoreBinding
import de.ka.skyfallapp.repo.api.Suggestion
import de.ka.skyfallapp.ui.home.consensus.list.SuggestionsItemViewModel.Companion.MORE_ID

class SuggestionsAdapter(
    owner: LifecycleOwner,
    list: ArrayList<SuggestionsItemBaseViewModel> = arrayListOf(),
    private val addMoreClickListener: () -> Unit
) :
    BaseAdapter<SuggestionsItemBaseViewModel>(owner, list, SuggestionsAdapterDiffCallback()) {

    var isAddingAllowed: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {

        if (viewType == 1) {
            return BaseViewHolder(ItemSuggestionsMoreBinding.inflate(layoutInflater, parent, false))
        }

        return BaseViewHolder(ItemSuggestionsBinding.inflate(layoutInflater, parent, false))
    }

    override fun getItemViewType(position: Int): Int {

        if (isAddingAllowed && getItems()[position].id == MORE_ID) {
            return 1
        }

        return super.getItemViewType(position)
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

    fun insert(newItems: List<Suggestion>) {

        val mappedList: ArrayList<SuggestionsItemBaseViewModel> =
            ArrayList(newItems.map { suggestion -> SuggestionsItemViewModel(suggestion) })

        mappedList.add(SuggestionsItemMoreViewModel(addMoreClickListener))

        addItems(mappedList)
    }

    class SuggestionsAdapterDiffCallback : DiffUtil.ItemCallback<SuggestionsItemBaseViewModel>() {

        override fun areItemsTheSame(
            oldItem: SuggestionsItemBaseViewModel,
            newItem: SuggestionsItemBaseViewModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: SuggestionsItemBaseViewModel,
            newItem: SuggestionsItemBaseViewModel
        ): Boolean {
            return oldItem == newItem
        }

    }
}





package de.ka.skyfallapp.ui.consensus.consensusdetail.suggestionlist


import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import de.ka.skyfallapp.base.BaseAdapter
import de.ka.skyfallapp.base.BaseViewHolder
import de.ka.skyfallapp.databinding.ItemSuggestionBinding
import de.ka.skyfallapp.databinding.ItemSuggestionsMoreBinding
import de.ka.skyfallapp.repo.api.SuggestionResponse
import de.ka.skyfallapp.ui.consensus.consensusdetail.suggestionlist.SuggestionsItemViewModel.Companion.MORE_ID

class SuggestionsAdapter(
    owner: LifecycleOwner,
    list: ArrayList<SuggestionsItemBaseViewModel> = arrayListOf(),
    private val addMoreClickListener: () -> Unit,
    private val toolsClickListener: () -> Unit
) :
    BaseAdapter<SuggestionsItemBaseViewModel>(owner, list, SuggestionsAdapterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {

        if (viewType == 1) {
            return BaseViewHolder(ItemSuggestionsMoreBinding.inflate(layoutInflater, parent, false))
        }

        return BaseViewHolder(ItemSuggestionBinding.inflate(layoutInflater, parent, false))
    }

    override fun getItemViewType(position: Int): Int {

        if (getItems()[position].id == MORE_ID) {
            return 1
        }

        return super.getItemViewType(position)
    }

    /**
     * Inserts the suggestion reponse items to the list and a 'add more' button at the end.
     * If [isFinished] is set to true, this will not add a 'add more' button at the end of the list.
     *
     * @param newItems the new items to add
     * @param isFinished set to false to show an add more button at the end of the list
     */
    fun insert(newItems: List<SuggestionResponse>, isFinished: Boolean) {
        val mappedList: ArrayList<SuggestionsItemBaseViewModel> =
            ArrayList(newItems.map { suggestion ->
                SuggestionsItemViewModel(
                    suggestion,
                    isFinished,
                    toolsClickListener
                )
            })

        if (!isFinished) {
            mappedList.add(SuggestionsItemMoreViewModel(addMoreClickListener))
        }

        setItems(mappedList)
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
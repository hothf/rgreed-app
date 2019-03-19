package de.ka.skyfallapp.ui.consensus.consensusdetail.suggestionlist


import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseAdapter
import de.ka.skyfallapp.base.BaseViewHolder
import de.ka.skyfallapp.databinding.ItemSuggestionBinding
import de.ka.skyfallapp.databinding.ItemSuggestionsHeaderBinding
import de.ka.skyfallapp.databinding.ItemSuggestionsMoreBinding
import de.ka.skyfallapp.repo.api.models.SuggestionResponse
import de.ka.skyfallapp.ui.consensus.consensusdetail.suggestionlist.SuggestionsItemViewModel.Companion.HEADER_ID
import de.ka.skyfallapp.ui.consensus.consensusdetail.suggestionlist.SuggestionsItemViewModel.Companion.MORE_ID

/**
 * Adapter for handling [SuggestionResponse]s and displaying [SuggestionsItemBaseViewModel]s.
 */
class SuggestionsAdapter(
    owner: LifecycleOwner,
    list: ArrayList<SuggestionsItemBaseViewModel> = arrayListOf(),
    private val addMoreClickListener: () -> Unit,
    private val voteClickListener: (suggestion: SuggestionResponse) -> Unit,
    private val toolsClickListener: (view: View, suggestion: SuggestionResponse) -> Unit
) :
    BaseAdapter<SuggestionsItemBaseViewModel>(owner, list, SuggestionsAdapterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {

        if (viewType == 1) {
            return BaseViewHolder(ItemSuggestionsMoreBinding.inflate(layoutInflater, parent, false))
        } else if (viewType == 2) {
            return BaseViewHolder(ItemSuggestionsHeaderBinding.inflate(layoutInflater, parent, false))
        }

        return BaseViewHolder(ItemSuggestionBinding.inflate(layoutInflater, parent, false))
    }

    override fun getItemViewType(position: Int): Int {

        if (getItems()[position].id == MORE_ID) {
            return 1
        } else if (getItems()[position].id == HEADER_ID) {
            return 2
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
    fun insert(context: Context, newItems: List<SuggestionResponse>, isFinished: Boolean) {
        val mappedList: ArrayList<SuggestionsItemBaseViewModel> =
            ArrayList(newItems.map { suggestion ->
                SuggestionsItemViewModel(suggestion, isFinished, voteClickListener, toolsClickListener)
            })

        if (isFinished && !mappedList.isEmpty()) {
            if (mappedList.size > 1) {
                mappedList.add(1, SuggestionsItemHeaderViewModel(context.getString(R.string.suggestions_header_others)))
            }
            mappedList.add(0, SuggestionsItemHeaderViewModel(context.getString(R.string.suggestions_header_winner)))
        }

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
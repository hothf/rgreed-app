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
     * Inserts the suggestion response items to the list and a 'add more' button at the end.
     * If [isFinished] is set to true, this will not add a 'add more' button at the end of the list.
     *
     * @param newItems the new items to add
     * @param isFinished set to false to show an add more button at the end of the list
     */
    fun insert(context: Context, newItems: List<SuggestionResponse>, isFinished: Boolean) {
        var lowestAcceptance = 999.9f
        var placement = 0

        val mappedList: ArrayList<SuggestionsItemBaseViewModel> =
            ArrayList(newItems.map { suggestion ->
                suggestion.overallAcceptance?.let {
                    if (it <= lowestAcceptance) { // the lower the better, this list is already sorted from low to high
                        lowestAcceptance = suggestion.overallAcceptance
                        placement = 1
                    } else {
                        placement += 1
                    }
                }
                SuggestionsItemViewModel(suggestion, isFinished, voteClickListener, toolsClickListener, placement)
            })

        // if finished, we add placements and headers for the winners and others, if the list is not empty
        if (isFinished && !mappedList.isEmpty()) {
            mappedList.add(
                0,
                SuggestionsItemHeaderViewModel(context.getString(R.string.suggestions_header_winner))
            )
            val indexOfPlace2 = mappedList.indexOfFirst { viewModel -> viewModel.placement == 2 }
            if (indexOfPlace2 > 1) {
                mappedList.add(
                    indexOfPlace2,
                    SuggestionsItemHeaderViewModel(context.getString(R.string.suggestions_header_others))
                )
            }
        }

        // if not finished, we add a header, if list is not empty and a add more button
        if (!isFinished) {
            if (!mappedList.isEmpty()) {
                mappedList.add(0, SuggestionsItemHeaderViewModel(context.getString(R.string.suggestions_header_all)))
            }
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
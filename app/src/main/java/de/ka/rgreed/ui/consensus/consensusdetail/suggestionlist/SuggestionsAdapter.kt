package de.ka.rgreed.ui.consensus.consensusdetail.suggestionlist


import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import de.ka.rgreed.R
import de.ka.rgreed.base.BaseAdapter
import de.ka.rgreed.base.BaseViewHolder
import de.ka.rgreed.databinding.ItemSuggestionBinding
import de.ka.rgreed.databinding.ItemSuggestionsHeaderBinding
import de.ka.rgreed.repo.api.models.SuggestionResponse
import de.ka.rgreed.ui.consensus.consensusdetail.suggestionlist.SuggestionsItemViewModel.Companion.HEADER_ID
import de.ka.rgreed.utils.toDateTime
import kotlin.math.min

/**
 * Adapter for handling [SuggestionResponse]s and displaying [SuggestionsItemBaseViewModel]s.
 */
class SuggestionsAdapter(
    owner: LifecycleOwner,
    list: ArrayList<SuggestionsItemBaseViewModel> = arrayListOf(),
    private val voteClickListener: (suggestion: SuggestionResponse, placement: Int) -> Unit,
    private val toolsClickListener: (view: View, suggestion: SuggestionResponse) -> Unit
) :
    BaseAdapter<SuggestionsItemBaseViewModel>(owner, list, SuggestionsAdapterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        if (viewType == 2) {
            return BaseViewHolder(ItemSuggestionsHeaderBinding.inflate(layoutInflater, parent, false))
        }

        return BaseViewHolder(ItemSuggestionBinding.inflate(layoutInflater, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        if (getItems()[position].id == HEADER_ID) {
            return 2
        }

        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val item = getItems()[position] as? SuggestionsItemViewModel

        item?.let {
            DataBindingUtil.getBinding<ItemSuggestionBinding>(holder.itemView)?.let { binding ->
                binding.acceptanceMeter.post {
                    binding.acceptanceMeter.apply {
                        // fancy animation, filling up a bar
                        scaleX = 1.0f
                        pivotX = binding.acceptanceMeter.width.toFloat()

                        val delay = min((300 + (position * 100)).toLong(), 1_000)
                        animate().scaleX(it.overallAcceptance).setStartDelay(delay)
                    }
                }
            }

        }
        super.onBindViewHolder(holder, position)
    }

    /**
     * Removes the specified [updatedItems] or inserts them or updates a part of it, depending on the flags given
     * as paremeters in this method.
     *
     * @param context the base context, used for constructing useful header information, if needed to seperate items
     * @param updatedItems the updated items to either remove, add, update or mix
     * @param isFinished a indicator for creating better headers for individual suggestion items
     * @param votingStartDate the voting start date needed for populating different sections
     * @param remove the flag to indicate that items should be removed
     * @param onlyUpdate a flag to indicate that only updates should be made and no items should be added
     * @param addToTop a flag indicating if adding a new item, it is added to the top of the list, default to true
     */
    fun removeAddOrUpdate(
        context: Context,
        updatedItems: List<SuggestionResponse>,
        isFinished: Boolean,
        votingStartDate: Long,
        remove: Boolean,
        onlyUpdate: Boolean,
        addToTop: Boolean = true
    ) {
        val items: MutableList<SuggestionResponse> =
            getItems().filterIsInstance<SuggestionsItemViewModel>().map { it.item }.toMutableList()

        updatedItems.forEach { item ->
            val foundIndex = items.indexOfFirst { it.id == item.id }

            if (foundIndex > -1 && items.isNotEmpty()) {
                if (remove) {                                       // remove
                    items.removeAt(foundIndex)
                } else {                                            // update
                    items[foundIndex] = item
                }
            } else if (!onlyUpdate) {                               // add
                if (addToTop) {
                    items.add(0, item)
                } else {
                    items.add(item)
                }
            }
        }

        createHeadersAndInsertList(
            context, items, isFinished, votingStartDate
        )
    }

    /**
     * Inserts the suggestion response items to the list and all headers.
     *
     * @param context the base context
     * @param newItems the new items to add
     * @param isFinished set to false to show an add more button at the end of the list
     * @param votingStartDate the voting start date
     */
    private fun createHeadersAndInsertList(
        context: Context,
        newItems: List<SuggestionResponse>,
        isFinished: Boolean,
        votingStartDate: Long
    ) {
        val canVote = votingStartDate < System.currentTimeMillis()
        var lowestAcceptance = 999.9f
        var placement = 0

        val votedList: List<SuggestionsItemBaseViewModel> = newItems.mapNotNull { suggestion ->
            if (suggestion.overallAcceptance != null) {
                if (suggestion.overallAcceptance <= lowestAcceptance) { // the lower the better, this list is sorted
                    lowestAcceptance = suggestion.overallAcceptance
                    placement = 1
                } else {
                    placement += 1
                }
                SuggestionsItemViewModel(
                    suggestion,
                    canVote,
                    isFinished,
                    voteClickListener,
                    toolsClickListener,
                    placement
                )
            } else {
                null
            }
        }

        val notVotedList: List<SuggestionsItemBaseViewModel> = newItems.mapNotNull { suggestion ->
            if (suggestion.overallAcceptance == null) {
                SuggestionsItemViewModel(
                    suggestion,
                    canVote,
                    isFinished,
                    voteClickListener,
                    toolsClickListener,
                    placement
                )
            } else {
                null
            }
        }

        val mappedList = mutableListOf<SuggestionsItemBaseViewModel>()

        // if finished, we add placements and headers for the winners and others, if the list is not empty
        if (isFinished) {
            if (votedList.isNotEmpty()) {
                mappedList.add(
                    0,
                    SuggestionsItemHeaderViewModel(context.getString(R.string.suggestions_header_winner))
                )
                mappedList.addAll(votedList)
                val indexOfPlace2 = mappedList.indexOfFirst { viewModel -> viewModel.placement == 2 }
                if (indexOfPlace2 > 1) {
                    mappedList.add(
                        indexOfPlace2,
                        SuggestionsItemHeaderViewModel(context.getString(R.string.suggestions_header_others))
                    )
                }
            }
            if (!notVotedList.isEmpty()) {
                mappedList.add(
                    if (mappedList.isEmpty()) 0 else mappedList.size,
                    SuggestionsItemHeaderViewModel(context.getString(R.string.suggestions_header_not_voted))
                )
                mappedList.addAll(notVotedList)
            }
        } else {  // if not finished, we add a header, if list is not empty
            if (canVote && notVotedList.isNotEmpty()) {
                mappedList.add(
                    0,
                    SuggestionsItemHeaderViewModel(context.getString(R.string.suggestions_header_all_canvote))
                )
                mappedList.addAll(notVotedList)
            } else if (!canVote && notVotedList.isNotEmpty()) {
                mappedList.add(
                    0,
                    SuggestionsItemHeaderViewModel(
                        String.format(
                            context.getString(R.string.suggestions_header_all_cannotvote),
                            votingStartDate.toDateTime()
                        )
                    )
                )
                mappedList.addAll(notVotedList)
            }
        }

        if (mappedList.isEmpty()) {
            mappedList.add(SuggestionsItemHeaderViewModel("")) //spacer
            mappedList.add(SuggestionsItemHeaderViewModel(context.getString(R.string.suggestions_header_empty), true))
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
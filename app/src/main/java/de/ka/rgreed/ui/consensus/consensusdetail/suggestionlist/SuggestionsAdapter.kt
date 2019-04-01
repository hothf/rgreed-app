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
    private val voteClickListener: (suggestion: SuggestionResponse) -> Unit,
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
     * Inserts the suggestion response items to the list and a 'add more' button at the end.
     * If [isFinished] is set to true, this will not add a 'add more' button at the end of the list.
     *
     * @param context the base context
     * @param newItems the new items to add
     * @param isFinished set to false to show an add more button at the end of the list
     * @param votingStartDate the voting start date
     */
    fun insert(context: Context, newItems: List<SuggestionResponse>, isFinished: Boolean, votingStartDate: Long) {
        val canVote = votingStartDate < System.currentTimeMillis()
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
                SuggestionsItemViewModel(
                    suggestion,
                    canVote,
                    isFinished,
                    voteClickListener,
                    toolsClickListener,
                    placement
                )
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

        // if not finished, we add a header, if list is not empty
        if (!isFinished && !mappedList.isEmpty()) {
            if (canVote) {
                mappedList.add(
                    0, SuggestionsItemHeaderViewModel(context.getString(R.string.suggestions_header_all_canvote))
                )
            } else {
                mappedList.add(
                    0,
                    SuggestionsItemHeaderViewModel(
                        String.format(
                            context.getString(R.string.suggestions_header_all_cannotvote),
                            votingStartDate.toDateTime()
                        )
                    )
                )
            }
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
package de.ka.rgreed.ui.consensus.consensusdetail.suggestionlist

import android.view.View
import androidx.core.content.ContextCompat
import de.ka.rgreed.R
import de.ka.rgreed.repo.api.models.SuggestionResponse

/**
 * A default suggestion item view model.
 */
class SuggestionsItemViewModel(
    var item: SuggestionResponse,
    canVote: Boolean = false,
    isFinished: Boolean = false,
    val voteClickListener: (suggestion: SuggestionResponse, placement: Int) -> Unit,
    val toolsClickListener: (view: View, suggestion: SuggestionResponse) -> Unit,
    override val placement: Int = 0
) :
    SuggestionsItemBaseViewModel(placement) {

    override val id = item.id

    val title = item.title
    val elevation = if (placement == 1) appContext.resources.getDimensionPixelSize(R.dimen.default_16) else
        appContext.resources.getDimensionPixelSize(R.dimen.default_4)
    val placementVisibility = if (placement != 0 && item.overallAcceptance != null) View.VISIBLE else View.GONE
    val badVotesVisibility =
        if (item.heavyObjectionsCount != null && item.heavyObjectionsCount!! > 0) View.VISIBLE else View.GONE
    val winnerVisibility =
        if (isFinished && placement < 2 && item.overallAcceptance != null) View.VISIBLE else View.GONE
    val placementText = String.format(appContext.getString(R.string.suggestions_vote_placement), placement)
    val adminVisibility = if (item.admin && !isFinished && !canVote) View.VISIBLE else View.GONE
    val votedColor = if (item.ownAcceptance != null) ContextCompat.getColor(
        appContext,
        R.color.colorHighlight
    ) else ContextCompat.getColor(appContext, R.color.colorAccent)
    val overallAcceptance = adjustAcceptance()
    val voteText = if (isFinished && item.ownAcceptance != null || !isFinished && item.ownAcceptance != null)
        String.format(
            appContext.getString(R.string.suggestions_vote_value),
            item.ownAcceptance?.toInt().toString()
        ) else if (isFinished || !canVote) "" else appContext.getString(R.string.suggestions_vote_placeholder)

    /**
     * Handle the click on a vote
     */
    fun voteClick() {
        voteClickListener(item, placement)
    }

    /**
     * Called on a click for opening tools of the suggestion.
     *
     * @param view the view requesting the tools
     */
    fun onToolsClick(view: View) {
        toolsClickListener(view, item)
    }

    private fun adjustAcceptance(): Float {
        item.overallAcceptance?.let {
            return 1.0f - Math.max(0.0f, 1.0f - it / 10)
        }
        return 1.0f
    }

    override fun equals(other: Any?): Boolean {
        // there is so much to consider right now, that we just invalidate it every time here.
        return false
    }

    companion object {
        const val HEADER_ID = -2
    }
}

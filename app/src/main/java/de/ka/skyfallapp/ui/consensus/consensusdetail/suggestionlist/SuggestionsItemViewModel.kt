package de.ka.skyfallapp.ui.consensus.consensusdetail.suggestionlist

import android.view.View
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.R
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.models.SuggestionResponse
import de.ka.skyfallapp.repo.api.models.VoteBody
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.start
import de.ka.skyfallapp.utils.toDateTime
import de.ka.skyfallapp.utils.with

/**
 * A default suggestion item view model.
 */
class SuggestionsItemViewModel(
    private var item: SuggestionResponse,
    private val isFinished: Boolean = false,
    val voteClickListener: (suggestion: SuggestionResponse) -> Unit,
    val toolsClickListener: (view: View, suggestion: SuggestionResponse) -> Unit
) :
    SuggestionsItemBaseViewModel() {

    override val id = item.id

    val title = item.title
    val adminVisibility = if (item.admin && !isFinished) View.VISIBLE else View.GONE
    val overallAcceptance = MutableLiveData<Float>().apply { value = adjustAcceptance() }
    val voteText = MutableLiveData<String>().apply {
        value = if (isFinished && item.ownAcceptance != null || !isFinished && item.ownAcceptance != null) {
            String.format(appContext.getString(R.string.suggestions_vote_value), item.ownAcceptance?.toInt().toString())
        } else if (isFinished) {
            ""
        } else {
            appContext.getString(R.string.suggestions_vote_placeholder)
        }
    }

    /**
     * Handle the click on a vote
     */
    fun voteClick() {
        voteClickListener(item)
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
            return Math.max(0.0f, 1.0f - it / 10)
        }
        return 0.0f
    }

    override fun equals(other: Any?): Boolean {
        /*
        if (other is SuggestionsItemViewModel) {
            return id == other.id
                    && item.admin == other.item.admin
                    && item.description == other.item.description
                    && item.voteStartDate == other.item.voteStartDate
                    && item.creationDate == other.item.creationDate
                    && item.overallAcceptance == other.item.overallAcceptance
        }*///TODO rethink this, as we do show some things differently, like a button when the votestart date has passed, we can not simply compare items.

        return false
    }

    companion object {
        const val MORE_ID = -1
    }
}

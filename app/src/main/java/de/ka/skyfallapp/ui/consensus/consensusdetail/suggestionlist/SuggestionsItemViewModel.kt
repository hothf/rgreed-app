package de.ka.skyfallapp.ui.consensus.consensusdetail.suggestionlist

import android.view.View
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.R
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.SuggestionResponse
import de.ka.skyfallapp.repo.api.VoteBody
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
    val toolsClickListener: (view: View, suggestion: SuggestionResponse) -> Unit
) :
    SuggestionsItemBaseViewModel() {

    override val id = item.id

    val title = item.title
    val description = item.description
    val loadingVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val adminVisibility = if (item.admin && !isFinished) View.VISIBLE else View.GONE
    val overallAcceptance = MutableLiveData<Float>().apply { value = adjustAcceptance() }
    val notReadyVisibility = MutableLiveData<Int>().apply { value = getVisibilityForVotingReadyStatus(false) }
    val votingVisibility = MutableLiveData<Int>().apply { value = getVisibilityForVotingReadyStatus(true) }
    val voteStartDate =
        String.format(appContext.getString(R.string.suggestions_votingstart), item.voteStartDate.toDateTime())

    /**
     * Votes on the suggestion of this view model.
     */
    fun vote() {
        compositeDisposable?.let {

            repository.consensusManager.voteForSuggestion(item.consensusId, item.id, VoteBody(12.0f))
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion(::handleVotingResult)
                .start(it) {
                    loadingVisibility.postValue(View.VISIBLE)
                    notReadyVisibility.postValue(View.GONE)
                    votingVisibility.postValue(View.GONE)
                }

        }

    }

    private fun handleVotingResult(result: RepoData<SuggestionResponse?>) {
        result.data?.let {
            item = it
            overallAcceptance.postValue(adjustAcceptance())
        }

        apiErrorHandler.handle(result) {
            // do nothing?
        }


        loadingVisibility.postValue(View.GONE)
        notReadyVisibility.postValue(getVisibilityForVotingReadyStatus(false))
        votingVisibility.postValue(getVisibilityForVotingReadyStatus(true))
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
        if (item.overallAcceptance > 0.0f) {
            return Math.max(1.0f, item.overallAcceptance)
        }
        return 0.0f
    }

    private fun votingReady(): Boolean {
        if (isFinished) {
            return false
        } else if (System.currentTimeMillis() >= item.voteStartDate) {
            return true
        }
        return false
    }

    private fun getVisibilityForVotingReadyStatus(isReady: Boolean): Int {
        if (isFinished) {
            return View.GONE
        }
        return if (votingReady()) {
            if (isReady) {
                View.VISIBLE
            } else {
                View.GONE
            }
        } else {
            if (isReady) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
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

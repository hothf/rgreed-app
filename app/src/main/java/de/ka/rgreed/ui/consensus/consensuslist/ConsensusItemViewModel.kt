package de.ka.rgreed.ui.consensus.consensuslist

import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.MutableLiveData
import de.ka.rgreed.R
import de.ka.rgreed.base.BaseItemViewModel

import de.ka.rgreed.repo.api.models.ConsensusResponse
import de.ka.rgreed.utils.TimeAwareUpdate

/**
 * Handles the display of consensus data in a compact way.
 */
class ConsensusItemViewModel(
    val item: ConsensusResponse,
    val listener: (ConsensusItemViewModel, View) -> Unit
) : BaseItemViewModel() {

    private val finishedColor =
        if (item.finished) ContextCompat.getColor(appContext, R.color.defaultBackgroundPrimary) else
            ContextCompat.getColor(appContext, R.color.defaultBackgroundPrimary)

    val description = item.description
    val gravity = Gravity.START
    val statusColor = MutableLiveData<Int>().apply { value = if (item.finished) ContextCompat.getColor(appContext, R.color.colorStatusFinished) else
        ContextCompat.getColor(appContext, R.color.colorStatusOpen)}
    val title = item.title
    val followingVisibility = if (item.following) View.VISIBLE else View.GONE
    val statusImage = MutableLiveData<Drawable>().apply {
        var drawable = ContextCompat.getDrawable(appContext, R.drawable.ic_locked)
        if (item.finished) {
            drawable = ContextCompat.getDrawable(appContext, R.drawable.ic_finished)
        } else {
            if (item.hasAccess) {
                drawable = if (item.public) {
                    ContextCompat.getDrawable(appContext, R.drawable.ic_public)
                } else {
                    ContextCompat.getDrawable(appContext, R.drawable.ic_unlocked)
                }
            }
        }
        DrawableCompat.setTint(drawable!!, finishedColor)
        postValue(drawable)
    }
    val endTime = when {
        item.finished -> TimeAwareUpdate(R.string.consensus_finished_on, item.endDate)
        item.votingStartDate > System.currentTimeMillis() -> TimeAwareUpdate(
            R.string.consensus_startvoting,
            item.votingStartDate
        )
        else -> TimeAwareUpdate(R.string.consensus_until, item.endDate)
    }

    override fun equals(other: Any?): Boolean {
        if (other is ConsensusItemViewModel) {
            return item.finished == other.item.finished
                    && item.admin == other.item.admin
                    && item.public == other.item.public
                    && item.description == other.item.description
                    && item.suggestionsCount == other.item.suggestionsCount
                    && item.endDate == other.item.endDate
                    && item.creator == other.item.creator
                    && item.title == other.item.title
                    && item.creationDate == other.item.creationDate
                    && item.hasAccess == other.item.hasAccess
                    && item.voters == other.item.voters
        }
        return false
    }
}

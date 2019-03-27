package de.ka.skyfallapp.ui.consensus.consensuslist

import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseItemViewModel

import de.ka.skyfallapp.repo.api.models.ConsensusResponse
import de.ka.skyfallapp.utils.toDateTime

/**
 * Handles the display of consensus data in a compact way.
 */
class ConsensusItemViewModel(
    val item: ConsensusResponse,
    val listener: (ConsensusItemViewModel, View) -> Unit
) : BaseItemViewModel() {

    private val finishedColor = if (item.finished) ContextCompat.getColor(appContext, R.color.defaultBackgroundPrimary) else
        ContextCompat.getColor(appContext, R.color.defaultBackgroundPrimary)

    val description = item.description
    val gravity = Gravity.START
    val adminVisibility = if (item.admin) View.VISIBLE else View.GONE
    val votedTextColor = MutableLiveData<Int>().apply {
        value =
            if (isUserAVoter()) ContextCompat.getColor(appContext, R.color.colorHighlight) else ContextCompat.getColor(
                appContext,
                R.color.fontDefaultInverted
            )
    }
    val statusBackground = MutableLiveData<Drawable>().apply {
        value = if (item.finished) ContextCompat.getDrawable(appContext, R.drawable.bg_rounded_finished) else
            ContextCompat.getDrawable(appContext, R.drawable.bg_rounded_open)
    }
    val cardBackgound = MutableLiveData<Int>().apply { value = finishedColor }
    val title = item.title
    val statusImage = MutableLiveData<Drawable>().apply {
        var drawable = ContextCompat.getDrawable(appContext, R.drawable.ic_small_lock)
        if (item.finished) {
            drawable = ContextCompat.getDrawable(appContext, R.drawable.ic_small_flag)
        } else {
            if (item.hasAccess) {
                drawable = if (item.public) {
                    ContextCompat.getDrawable(appContext, R.drawable.ic_small_public)
                } else {
                    ContextCompat.getDrawable(appContext, R.drawable.ic_small_unlock)
                }
            }
        }
        DrawableCompat.setTint(drawable!!, finishedColor)
        postValue(drawable)
    }
    val ended = if (item.finished) String.format(
        appContext.getString(R.string.consensus_finished_on), item.endDate.toDateTime()
    ) else if (item.votingStartDate > System.currentTimeMillis()) {
        String.format(
            appContext.getString(R.string.consensus_startvoting), item.votingStartDate.toDateTime()
        )
    } else {
        String.format(
            appContext.getString(R.string.consensus_until), item.endDate.toDateTime()
        )
    }

    val voters =
        appContext.resources.getQuantityString(R.plurals.voters, item.voters.size, item.voters.size)

    private fun isUserAVoter(): Boolean {
        return item.voters.contains(repository.profileManager.currentProfile.username)
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

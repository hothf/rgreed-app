package de.ka.skyfallapp.ui.consensus.consensuslist

import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseItemViewModel

import de.ka.skyfallapp.repo.api.ConsensusResponse
import de.ka.skyfallapp.utils.toDateTime
import timber.log.Timber
import java.text.SimpleDateFormat

import java.util.concurrent.TimeUnit


class ConsensusItemViewModel(
    val item: ConsensusResponse,
    val listener: (ConsensusItemViewModel, View) -> Unit
) : BaseItemViewModel() {

    val adminVisibility = if (item.admin) View.VISIBLE else View.GONE
    val statusColor =
        MutableLiveData<Int>().apply { value = ContextCompat.getColor(appContext, R.color.colorStatusUnlocked) }
    val title = item.title
    val statusImage = MutableLiveData<Drawable>().apply {
        var drawable = ContextCompat.getDrawable(appContext, R.drawable.ic_small_lock)
        var statusBackgroundColor = ContextCompat.getColor(appContext, R.color.colorStatusLocked)
        if (item.finished) {
            drawable = ContextCompat.getDrawable(appContext, R.drawable.ic_small_flag)
            statusBackgroundColor = ContextCompat.getColor(appContext, R.color.colorStatusFinished)
        } else {
            if (item.hasAccess) {
                drawable = if (item.public) {
                    ContextCompat.getDrawable(appContext, R.drawable.ic_small_public)
                } else {
                    ContextCompat.getDrawable(appContext, R.drawable.ic_small_unlock)
                }
                statusBackgroundColor = ContextCompat.getColor(appContext, R.color.colorStatusUnlocked)
            }
        }
        DrawableCompat.setTint(drawable!!, statusBackgroundColor)
        postValue(drawable)
        statusColor.postValue(statusBackgroundColor)
    }
    val description = item.description

    val descriptionVisibility = if (item.description.isNullOrBlank()) View.GONE else View.VISIBLE

    val ended = if (item.finished) String.format(
        appContext.getString(R.string.consensus_finished_on), item.endDate.toDateTime()
    ) else ""
    val remains = if (item.finished) "" else
        String.format(
            appContext.getString(R.string.consensus_until), item.endDate.toDateTime()
        )
    val suggestions =
        appContext.resources.getQuantityString(R.plurals.suggestions, item.suggestionsCount, item.suggestionsCount)

    fun onShare() {
        //TODO add sharing function
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
        }
        return false
    }
}

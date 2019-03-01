package de.ka.skyfallapp.ui.consensus.consensuslist

import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseItemViewModel

import de.ka.skyfallapp.repo.api.ConsensusResponse
import timber.log.Timber
import java.text.SimpleDateFormat

import java.util.concurrent.TimeUnit


class ConsensusItemViewModel(
    val item: ConsensusResponse,
    val listener: (ConsensusItemViewModel, View) -> Unit
) : BaseItemViewModel() {

    var timeEnds = 0L

    init {


        val timeDiffMillis = Math.abs(item.endDate - System.currentTimeMillis())

        timeEnds = TimeUnit.DAYS.convert(timeDiffMillis, TimeUnit.MILLISECONDS)

        if (timeEnds == 0L) {
            timeEnds = TimeUnit.MINUTES.convert(timeDiffMillis, TimeUnit.MILLISECONDS)

            if (timeEnds == 0L) {
                timeEnds = TimeUnit.SECONDS.convert(timeDiffMillis, TimeUnit.MILLISECONDS)

                if (timeEnds == 0L) {
                    timeEnds = timeDiffMillis
                }


            }
        }

        Timber.e("$timeEnds, ::: $timeDiffMillis")


    }

    val dividerVisibility = MutableLiveData<Int>().apply { postValue(View.VISIBLE) }
    val adminVisibility =
        MutableLiveData<Int>().apply { if (item.admin) postValue(View.VISIBLE) else postValue(View.GONE) }
    val participatingVisibility = MutableLiveData<Int>().apply { postValue(View.GONE) } // dont like this...


    val title = item.title

    fun onShare() {
        //TODO add sharing function
    }

    val statusImage = MutableLiveData<Drawable>().apply {
        var drawable = ContextCompat.getDrawable(appContext, R.drawable.ic_small_doing)
        var color = ContextCompat.getColor(appContext, R.color.fontDefault)
        if (item.finished) {
            drawable = ContextCompat.getDrawable(appContext, R.drawable.ic_small_done)
            color = ContextCompat.getColor(appContext, R.color.colorAccent)
        }
        DrawableCompat.setTint(drawable!!, color)
        postValue(drawable)
    }


    val description =
        if (item.description.isNullOrBlank()) appContext.getString(R.string.consensus_fallback_description) else item.description

    val status = if (item.admin) "Admin" else ""

    val ended = if (item.finished) String.format(
        appContext.getString(
            R.string.consensus_finished_on
        ),
        SimpleDateFormat().format(item.endDate)
    ) else ""
    val remains = if (item.finished) "" else SimpleDateFormat().format(item.endDate)

    val creator = String.format(
        appContext.getString(R.string.consensus_created_by),
        item.creator,
        SimpleDateFormat().format(item.creationDate)
    )

    val backColor = if (item.finished) ContextCompat.getColor(
        appContext,
        R.color.defaultBackgroundOkay
    ) else ContextCompat.getColor(appContext, R.color.defaultBackgroundPrimary)

    val participants = String.format(appContext.getString(R.string.consensus_participants), 2)

    val creationDate = item.title//SimpleDateFormat("DD/MM/YY", Locale.getDefault()).format(Date(item.creationDate))

    val suggestions = String.format(appContext.getString(R.string.consensus_suggestions), item.suggestionsCount)


    val titleTextColor =
        if (item.finished) {
            ContextCompat.getColor(appContext, R.color.colorAccent)
        } else {
            ContextCompat.getColor(appContext, R.color.fontNav)
        }


    val descriptionTextColor = if (item.finished) {
        ContextCompat.getColor(appContext, R.color.colorAccent)
    } else {
        ContextCompat.getColor(appContext, R.color.fontDefault)
    }

    //TODO dirty in list should not reset #?

    // TODO should not scroll to top ?

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
        }
        return false
    }
}

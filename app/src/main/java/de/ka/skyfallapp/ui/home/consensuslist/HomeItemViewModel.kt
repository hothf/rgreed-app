package de.ka.skyfallapp.ui.home.consensuslist

import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseItemViewModel

import de.ka.skyfallapp.repo.api.ConsensusResponse
import timber.log.Timber
import java.text.SimpleDateFormat

import java.util.concurrent.TimeUnit


class HomeItemViewModel(
    val item: ConsensusResponse,
    val listener: (HomeItemViewModel, View) -> Unit
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

    val description = item.description

    val status = if (item.admin) "Admin" else ""

    val endsIn = SimpleDateFormat().format(item.endDate)

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
}

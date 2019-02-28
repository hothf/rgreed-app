package de.ka.skyfallapp.ui.personal.consensuslist

import android.view.View
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.base.BaseItemViewModel

import de.ka.skyfallapp.repo.api.ConsensusResponse

class PersonalItemViewModel(
    val item: ConsensusResponse,
    val listener: (PersonalItemViewModel, View) -> Unit
) : BaseItemViewModel() {

    val dividerVisibility = MutableLiveData<Int>().apply { postValue(View.VISIBLE) }

    val title = item.title

    val participants = item.suggestionsCount.toString()

    val creationDate = item.title //SimpleDateFormat("DD/MM/YY", Locale.getDefault()).format(Date(item.))

    val suggestionCount = item.suggestionsCount.toString()

    override fun equals(other: Any?): Boolean {
        if (other is PersonalItemViewModel) {
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

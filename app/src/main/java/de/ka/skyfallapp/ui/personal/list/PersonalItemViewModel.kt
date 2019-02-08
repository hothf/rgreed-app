package de.ka.skyfallapp.ui.personal.list

import android.view.View
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.base.BaseItemViewModel

import de.ka.skyfallapp.repo.api.ConsensusResponse

class PersonalItemViewModel(
    val item: ConsensusResponse,
    private val listener: (PersonalItemViewModel) -> Unit
) : BaseItemViewModel() {

    val dividerVisibility = MutableLiveData<Int>().apply { postValue(View.VISIBLE) }

    val title = item.title

    val participants = item.suggestionsCount.toString()

    val creationDate = item.title //SimpleDateFormat("DD/MM/YY", Locale.getDefault()).format(Date(item.))

    val suggestionCount = item.suggestionsCount.toString()

    fun onClick() {
        listener(this)
    }
}

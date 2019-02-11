package de.ka.skyfallapp.ui.home.consensuslist

import android.view.View
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.base.BaseItemViewModel

import de.ka.skyfallapp.repo.api.ConsensusResponse

class HomeItemViewModel(
    val item: ConsensusResponse,
    private val listener: (HomeItemViewModel) -> Unit
) : BaseItemViewModel() {

    val dividerVisibility = MutableLiveData<Int>().apply { postValue(View.VISIBLE) }

    val title = item.title

    val participants = item.suggestionsCount.toString()

    val creationDate = item.title//SimpleDateFormat("DD/MM/YY", Locale.getDefault()).format(Date(item.creationDate))

    val suggestionCount = item.suggestionsCount.toString()

    fun onClick() {
        listener(this)
    }
}

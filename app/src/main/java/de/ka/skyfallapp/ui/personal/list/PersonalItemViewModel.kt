package de.ka.skyfallapp.ui.personal.list

import android.view.View
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.base.BaseItemViewModel

import de.ka.skyfallapp.repo.api.Consensus
import java.text.SimpleDateFormat
import java.util.*

class PersonalItemViewModel(
    val item: Consensus,
    private val listener: (PersonalItemViewModel) -> Unit
) : BaseItemViewModel() {

    val dividerVisibility = MutableLiveData<Int>().apply { postValue(View.VISIBLE) }

    val title = item.title

    val participants = item.participants.size.toString()

    val creationDate = SimpleDateFormat("DD/MM/YY", Locale.getDefault()).format(Date(item.creationDate))

    val suggestionCount = item.suggestions.size.toString()

    fun onClick() {
        listener(this)
    }
}

package de.ka.skyfallapp.ui.home.list

import android.view.View
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.base.BaseItemViewModel

import de.ka.skyfallapp.repo.api.Consensus
import java.text.SimpleDateFormat
import java.util.*

class HomeItemViewModel(
    val item: Consensus,
    private val listener: (HomeItemViewModel) -> Unit
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

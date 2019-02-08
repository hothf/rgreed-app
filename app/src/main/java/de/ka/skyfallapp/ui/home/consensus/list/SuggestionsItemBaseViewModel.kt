package de.ka.skyfallapp.ui.home.consensus.list

import android.view.View
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.base.BaseItemViewModel

abstract class SuggestionsItemBaseViewModel : BaseItemViewModel() {

    abstract val id: Int

    val dividerVisibility = MutableLiveData<Int>().apply { postValue(View.VISIBLE) }
}
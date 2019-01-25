package de.ka.skyfallapp.ui.home.consensus.list

import de.ka.skyfallapp.ui.home.consensus.list.SuggestionsItemViewModel.Companion.MORE_ID

class SuggestionsItemMoreViewModel(val clickListener: () -> Unit) : SuggestionsItemBaseViewModel() {

    override val id = MORE_ID

    fun onClick() {
        clickListener()
    }
}

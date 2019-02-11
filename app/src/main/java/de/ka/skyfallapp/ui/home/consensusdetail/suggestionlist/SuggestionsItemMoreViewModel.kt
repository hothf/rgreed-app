package de.ka.skyfallapp.ui.home.consensusdetail.suggestionlist

import de.ka.skyfallapp.ui.home.consensusdetail.suggestionlist.SuggestionsItemViewModel.Companion.MORE_ID

class SuggestionsItemMoreViewModel(val clickListener: () -> Unit) : SuggestionsItemBaseViewModel() {

    override val id = MORE_ID

    fun onClick() {
        clickListener()
    }
}

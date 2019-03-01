package de.ka.skyfallapp.ui.consensus.consensusdetail.suggestionlist

import de.ka.skyfallapp.ui.consensus.consensusdetail.suggestionlist.SuggestionsItemViewModel.Companion.MORE_ID

class SuggestionsItemMoreViewModel(val clickListener: () -> Unit) : SuggestionsItemBaseViewModel() {

    override val id = MORE_ID

    fun onClick() {
        clickListener()
    }
}

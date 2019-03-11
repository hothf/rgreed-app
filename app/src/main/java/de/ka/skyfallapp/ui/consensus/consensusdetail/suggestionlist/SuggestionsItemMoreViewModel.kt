package de.ka.skyfallapp.ui.consensus.consensusdetail.suggestionlist

import de.ka.skyfallapp.ui.consensus.consensusdetail.suggestionlist.SuggestionsItemViewModel.Companion.MORE_ID

/**
 * A view model for showing a button to add additional suggestions.
 */
class SuggestionsItemMoreViewModel(val clickListener: () -> Unit) : SuggestionsItemBaseViewModel() {

    override val id = MORE_ID

    /**
     * Called on the click of the button to add new suggestions.
     */
    fun onClick() {
        clickListener()
    }
}

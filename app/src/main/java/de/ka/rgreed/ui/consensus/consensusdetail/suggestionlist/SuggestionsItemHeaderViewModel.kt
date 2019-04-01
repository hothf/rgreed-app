package de.ka.rgreed.ui.consensus.consensusdetail.suggestionlist

import de.ka.rgreed.ui.consensus.consensusdetail.suggestionlist.SuggestionsItemViewModel.Companion.HEADER_ID

/**
 * A view model for showing a header of a suggestion.
 */
class SuggestionsItemHeaderViewModel(val title: String) :
    SuggestionsItemBaseViewModel() {

    override val id = HEADER_ID
}

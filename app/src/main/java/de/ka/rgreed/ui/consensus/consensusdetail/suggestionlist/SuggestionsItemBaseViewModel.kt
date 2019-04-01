package de.ka.rgreed.ui.consensus.consensusdetail.suggestionlist

import de.ka.rgreed.base.BaseItemViewModel

/**
 * The base view model for suggestions.
 */
abstract class SuggestionsItemBaseViewModel(open val placement: Int = 0) : BaseItemViewModel() {

    abstract val id: Int
}
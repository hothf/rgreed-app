package de.ka.skyfallapp.ui.consensus.consensusdetail.suggestionlist

import de.ka.skyfallapp.base.BaseItemViewModel

/**
 * The base view model for suggestions.
 */
abstract class SuggestionsItemBaseViewModel(open val placement: Int = 0) : BaseItemViewModel() {

    abstract val id: Int
}
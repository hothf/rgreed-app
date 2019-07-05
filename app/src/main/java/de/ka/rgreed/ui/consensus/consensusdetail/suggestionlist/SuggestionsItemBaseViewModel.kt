package de.ka.rgreed.ui.consensus.consensusdetail.suggestionlist

import de.ka.rgreed.base.BaseItemViewModel

/**
 * The base view model for suggestions.
 */
abstract class SuggestionsItemBaseViewModel(open val placement: Int = 0) : BaseItemViewModel() {

    abstract val id: Int

    override fun equals(other: Any?): Boolean {
        if (other is SuggestionsItemBaseViewModel && other.id == this.id ) return true
        return false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
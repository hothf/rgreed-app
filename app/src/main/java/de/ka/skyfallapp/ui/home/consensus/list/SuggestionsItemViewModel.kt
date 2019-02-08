package de.ka.skyfallapp.ui.home.consensus.list

import de.ka.skyfallapp.repo.api.SuggestionResponse


class SuggestionsItemViewModel(val item: SuggestionResponse) : SuggestionsItemBaseViewModel() {

    override val id = item.id

    val title = item.title

    val participants = item.overallAcceptance.toString()

    val creationDate = item.creationDate.toString()

    val suggestionCount = item.title


    override fun equals(other: Any?): Boolean {

        if (other is SuggestionsItemViewModel) {
            return id == other.id
        }

        return super.equals(other)
    }

    companion object {
        const val MORE_ID = -1
    }
}

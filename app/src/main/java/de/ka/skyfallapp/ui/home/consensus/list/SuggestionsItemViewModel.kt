package de.ka.skyfallapp.ui.home.consensus.list

import de.ka.skyfallapp.repo.api.Suggestion

class SuggestionsItemViewModel(val item: Suggestion) : SuggestionsItemBaseViewModel() {

    override val id = item.id

    val title = item.title

    val participants = item.acceptance.toString()

    val creationDate = item.description

    val suggestionCount = item.title


    override fun equals(other: Any?): Boolean {

        if (other is SuggestionsItemViewModel) {
            return id == other.id
        }

        return super.equals(other)
    }

    companion object {
        const val MORE_ID = "-1"
    }
}

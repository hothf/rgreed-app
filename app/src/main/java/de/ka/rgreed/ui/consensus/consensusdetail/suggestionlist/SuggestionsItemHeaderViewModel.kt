package de.ka.rgreed.ui.consensus.consensusdetail.suggestionlist

import android.view.Gravity
import de.ka.rgreed.ui.consensus.consensusdetail.suggestionlist.SuggestionsItemViewModel.Companion.HEADER_ID

/**
 * A view model for showing a header of a suggestion.
 */
class SuggestionsItemHeaderViewModel(val title: String, val center: Boolean = false) :
    SuggestionsItemBaseViewModel() {

    override val id = HEADER_ID

    val gravity = if (center) Gravity.CENTER or Gravity.BOTTOM else Gravity.START or Gravity.CENTER_VERTICAL
}

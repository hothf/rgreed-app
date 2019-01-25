package de.ka.skyfallapp.ui.home.consensus.newsuggestion

import android.app.Application
import android.os.Bundle
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.repo.api.Suggestion
import de.ka.skyfallapp.ui.home.consensus.ConsensusDetailFragment

class NewSuggestionViewModel(app: Application) : BaseViewModel(app) {


    fun onUploadSuggestion() {
        dirtyDataWatcher.markDirty(ConsensusDetailFragment.CONS_DETAIL_DIRTY)

        navigateTo(
            R.id.action_newSuggestionFragment_to_consensusDetailFragment
        )
    }

}
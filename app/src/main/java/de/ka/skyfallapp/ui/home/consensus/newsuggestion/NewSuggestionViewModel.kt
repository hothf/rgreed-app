package de.ka.skyfallapp.ui.home.consensus.newsuggestion

import android.app.Application
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.base.events.BACK
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.SuggestionBody
import de.ka.skyfallapp.repo.api.SuggestionResponse
import de.ka.skyfallapp.repo.subscribeRepoCompletion

import de.ka.skyfallapp.ui.home.consensus.ConsensusDetailFragment
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.start
import de.ka.skyfallapp.utils.with


class NewSuggestionViewModel(app: Application) : BaseViewModel(app) {

    var id: Int = 0
    //var currentSuggestion = SuggestionBody(title = "", consensusId = 0)

    fun setup(consensusId: Int) {
        if (consensusId != id) {
            id = consensusId
        } // later deeplinking could refetch info; this is so far not handling the id of a suggestion, just consensus
    }

    fun onUploadSuggestion() {
        val consensusId = id ?: return

        val suggestion = SuggestionBody(consensusId, "dadad")

        repository.sendSuggestion(suggestion)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion { onUploaded(it) }
            .start(compositeDisposable, ::showLoading)
    }


    private fun onUploaded(result: RepoData<SuggestionResponse?>) {
        //refresh.postValue(false)

        result.data?.let {

            dirtyDataWatcher.markDirty(ConsensusDetailFragment.CONS_DETAIL_DIRTY)

            navigateTo(BACK)
            return
        }

        result.info.throwable?.let { showSnack(it.toString()) }
    }


    private fun showLoading() {

    }

}
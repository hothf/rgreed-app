package de.ka.skyfallapp.ui.home.consensus.newsuggestion

import android.app.Application
import android.os.Bundle
import android.view.View
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.ConsensusDetail
import de.ka.skyfallapp.repo.api.Suggestion
import de.ka.skyfallapp.repo.subscribeRepoCompletion

import de.ka.skyfallapp.ui.home.consensus.ConsensusDetailFragment
import de.ka.skyfallapp.ui.home.consensus.ConsensusDetailManager
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.start
import de.ka.skyfallapp.utils.with


class NewSuggestionViewModel(app: Application) : BaseViewModel(app) {

    var id: String? = null

    fun setup(consensusId: String) {
        if (consensusId != id) {
            id = consensusId
        } // later deeplinking could refetch info; this is so far not handling the id of a suggestion, just consensus
    }

    fun onUploadSuggestion() {
        val consensusDetail = ConsensusDetailManager.getDetail(id) ?: return

        consensusDetail.suggestions = listOf(Suggestion())

        repository.sendConsensus(consensusDetail)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion { onUploaded(it) }
            .start(compositeDisposable, ::showLoading)
    }


    private fun onUploaded(result: RepoData<ConsensusDetail?>) {
        //refresh.postValue(false)

        result.data?.let {

            dirtyDataWatcher.markDirty(ConsensusDetailFragment.CONS_DETAIL_DIRTY)

            navigateTo(
                R.id.action_newSuggestionFragment_to_consensusDetailFragment
            )
            return
        }

        result.info.throwable?.let { showSnack(it.toString()) }
    }


    private fun showLoading() {

    }

}
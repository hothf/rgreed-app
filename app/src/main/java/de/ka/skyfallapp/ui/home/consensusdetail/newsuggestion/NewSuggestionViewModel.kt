package de.ka.skyfallapp.ui.home.consensusdetail.newsuggestion

import android.app.Application
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.base.events.BACK
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.SuggestionBody
import de.ka.skyfallapp.repo.api.SuggestionResponse
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.ui.home.HomeFragment
import de.ka.skyfallapp.ui.home.HomeViewModel

import de.ka.skyfallapp.ui.home.consensusdetail.ConsensusDetailFragment
import de.ka.skyfallapp.ui.home.consensusdetail.ConsensusDetailViewModel
import de.ka.skyfallapp.ui.personal.PersonalFragment
import de.ka.skyfallapp.ui.personal.PersonalViewModel
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
        val consensusId = id

        val suggestion =
            SuggestionBody("Random Suggestion", "Random description", System.currentTimeMillis()+2000)

        repository.sendSuggestion(consensusId, suggestion)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion { onUploaded(it) }
            .start(compositeDisposable, ::showLoading)
    }


    private fun onUploaded(result: RepoData<SuggestionResponse?>) {
        //refresh.postValue(false)

        result.data?.let {

            dirtyDataWatcher.markDirty(ConsensusDetailViewModel.CONSENSUS_DETAIL_DATA)
            dirtyDataWatcher.markDirty(HomeViewModel.HOME_DATA, id)
            dirtyDataWatcher.markDirty(PersonalViewModel.PERSONAL_DATA)

            navigateTo(BACK)
            return
        }

        result.info.throwable?.let { showSnack(it.toString()) }
    }


    private fun showLoading() {

    }

}
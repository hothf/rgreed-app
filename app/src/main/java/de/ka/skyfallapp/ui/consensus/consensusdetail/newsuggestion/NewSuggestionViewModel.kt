package de.ka.skyfallapp.ui.consensus.consensusdetail.newsuggestion

import android.app.Application
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.base.events.BACK
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.SuggestionBody
import de.ka.skyfallapp.repo.api.SuggestionResponse
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.start
import de.ka.skyfallapp.utils.with
import io.reactivex.Completable


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
            SuggestionBody("Random Suggestion", "Random description", System.currentTimeMillis() + 2000)

        val first = Completable.fromSingle(repository.consensusManager.sendSuggestion(consensusId, suggestion))
        val second = Completable.fromSingle(repository.consensusManager.getConsensusDetail(consensusId))

       /* first
            .andThen(second)
            .with(AndroidSchedulerProvider())
            .subscribe({ onSuccess() }, { onError(it) })
            .start(compositeDisposable, ::showLoading) */


        repository.consensusManager.sendSuggestion(consensusId, suggestion)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion { onUploaded(it) }
            .start(compositeDisposable, ::showLoading)
    }


    private fun onUploaded(result: RepoData<SuggestionResponse?>) {
        //refresh.postValue(false)

        result.data?.let {
            navigateTo(BACK)
            return
        }

        apiErrorHandler.handle(result) {}

        result.info.throwable?.let { showSnack(it.toString()) }
    }

    private fun onSuccess() {
        navigateTo(BACK)
    }

    private fun onError(throwable: Throwable) {
        showSnack(throwable.toString())
    }


    private fun showLoading() {

    }

}
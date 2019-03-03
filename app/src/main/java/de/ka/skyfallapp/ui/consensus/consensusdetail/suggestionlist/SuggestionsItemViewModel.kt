package de.ka.skyfallapp.ui.consensus.consensusdetail.suggestionlist

import android.view.View
import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.repo.api.SuggestionResponse
import de.ka.skyfallapp.repo.api.VoteBody
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.start
import de.ka.skyfallapp.utils.with


class SuggestionsItemViewModel(var item: SuggestionResponse) : SuggestionsItemBaseViewModel() {

    override val id = item.id

    val loadingVisibility = MutableLiveData<Int>().apply { postValue(View.GONE) }

    val overallAcceptance = MutableLiveData<String>().apply { postValue("${item.overallAcceptance}") }

    val title = item.title

    val participants = item.overallAcceptance.toString()

    val creationDate = item.creationDate.toString()

    val suggestionCount = item.title

    fun vote() {
        repository.consensusManager.voteForSuggestion(item.consensusId, item.id, VoteBody(12.0f))
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion { response ->

                response.data?.let {
                    item = it
                    overallAcceptance.postValue("${it.overallAcceptance}")
                    //dirtyDataWatcher.markDirty(HomeViewModel.HOME_DATA)
                    //dirtyDataWatcher.markDirty(PersonalViewModel.PERSONAL_DATA)
                }

                apiErrorHandler.handle(response) {
                    // do nothing?
                }

                loadingVisibility.postValue(View.GONE)
            }
            .start(compositeDisposable!!) {
                loadingVisibility.postValue(View.VISIBLE)
            }
    }

    override fun equals(other: Any?): Boolean {

        if (other is SuggestionsItemViewModel) {
            return id == other.id
        }

        return false
    }

    companion object {
        const val MORE_ID = -1
    }
}
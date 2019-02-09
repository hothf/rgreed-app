package de.ka.skyfallapp.ui.home.consensus.list

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.ObservableField
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

    val participants = ObservableField<String>().apply { set(item.overallAcceptance.toString()) }

    val creationDate = item.creationDate.toString()

    val suggestionCount = item.title

    fun vote() {
        repository.voteForSuggestion(item.id, VoteBody(12.0f))
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion { response ->

                response.data?.let {
                    item = it
                    overallAcceptance.postValue("${it.overallAcceptance}")

                    participants.set("dfwkfjnegoegmeo")

                    //TODO mark data changed ...
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

package de.ka.skyfallapp.ui.consensus.consensusdetail.neweditsuggestion

import android.app.Application

import android.view.View

import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.base.events.SnackType
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.models.SuggestionBody
import de.ka.skyfallapp.repo.api.models.SuggestionResponse
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.utils.*
import de.ka.skyfallapp.utils.NavigationUtils.BACK
import java.util.*


/**
 * A view model dealing with editing or creating a new suggestion, depending on the used initializer
 * [setupEdit] or [setupNew].
 */
class NewEditSuggestionViewModel(app: Application) : BaseViewModel(app) {

    private var newFromConsensusId: Int = -1
    private var currentSuggestion: SuggestionResponse? = null
    private var currentTitle = ""

    val getDoneListener = ViewUtils.TextDoneListener()
    val title = MutableLiveData<String>().apply { value = "" }
    val header = MutableLiveData<String>().apply { value = "" }
    val titleSelection = MutableLiveData<Int>().apply { value = 0 }
    val saveDrawableRes = MutableLiveData<Int>().apply { value = R.drawable.ic_add }
    val bar = MutableLiveData<AppToolbar.AppToolbarState>().apply { value = AppToolbar.AppToolbarState.ACTION_VISIBLE }
    val getTextChangedListener = ViewUtils.TextChangeListener {
        currentTitle = it
        title.postValue(it)
        titleSelection.postValue(it.length)
    }

    /**
     * Sets up this view model with a given consensus id. This will result in the creation of a new suggestion.
     */
    fun setupNew(consensusId: Int) {
        newFromConsensusId = consensusId
        currentSuggestion = null
        currentTitle = ""

        header.postValue(app.getString(R.string.suggestions_newedit_title))
        saveDrawableRes.postValue(R.drawable.ic_small_add)

        updateTextViews()
    }

    /**
     * Sets up this view model with a given suggestion. This will result in the update of the suggestion.
     */
    fun setupEdit(suggestion: SuggestionResponse) {
        currentSuggestion = suggestion
        newFromConsensusId = -1
        currentTitle = suggestion.title

        header.postValue(app.getString(R.string.suggestions_newedit_edit))
        saveDrawableRes.postValue(R.drawable.ic_small_done)

        updateTextViews()
    }

    private fun updateTextViews() {
        title.postValue(currentTitle)
        titleSelection.postValue(currentTitle.length)
    }

    /**
     * Goes back.
     */
    fun onBack() {
        navigateTo(BACK)
    }

    /**
     * Called on a save press.
     */
    fun onSave() {
        val body = SuggestionBody(title = currentTitle)

        if (currentSuggestion != null) {
            repository.consensusManager.updateSuggestion(
                currentSuggestion!!.consensusId,
                currentSuggestion!!.id,
                body
            )
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion(::onUploaded)
                .start(compositeDisposable, ::showLoading)
        } else {
            repository.consensusManager.sendSuggestion(newFromConsensusId, body)
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion(::onUploaded)
                .start(compositeDisposable, ::showLoading)
        }
    }

    private fun onUploaded(result: RepoData<SuggestionResponse?>) {
        bar.postValue(AppToolbar.AppToolbarState.ACTION_VISIBLE)

        result.data?.let {
            navigateTo(BACK)
        }
    }

    private fun showLoading() {
        bar.postValue(AppToolbar.AppToolbarState.LOADING)
    }

    /**
     * A event for opening a date picker or a time picker.
     *
     * @param date set to true to open a date picker, false for a time picker
     * @param data the data containing the initializing time or date for the picker
     */
    class OpenPickerEvent(val date: Boolean, val data: Long)
}
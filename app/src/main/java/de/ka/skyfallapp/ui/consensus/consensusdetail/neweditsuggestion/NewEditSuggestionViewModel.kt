package de.ka.skyfallapp.ui.consensus.consensusdetail.neweditsuggestion

import android.app.Application
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent

import android.view.View

import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.base.events.SnackType
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.SuggestionBody
import de.ka.skyfallapp.repo.api.SuggestionResponse
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
    private var currentVoteStartDate = Calendar.getInstance().timeInMillis

    val getDoneListener = ViewUtils.TextDoneListener()
    val title = MutableLiveData<String>().apply { value = "" }
    val header = MutableLiveData<String>().apply { value = "" }
    val titleSelection = MutableLiveData<Int>().apply { value = 0 }
    val voteStartDate = MutableLiveData<String>().apply { value = "" }
    val voteStartTime = MutableLiveData<String>().apply { value = "" }
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
        currentVoteStartDate = Calendar.getInstance().timeInMillis

        header.postValue(app.getString(R.string.suggestions_newedit_title))
        saveDrawableRes.postValue(R.drawable.ic_small_add)
    }

    /**
     * Sets up this view model with a given suggestion. This will result in the update of the suggestion.
     */
    fun setupEdit(suggestion: SuggestionResponse) {
        currentSuggestion = suggestion
        newFromConsensusId = -1
        currentTitle = suggestion.title
        currentVoteStartDate = suggestion.voteStartDate

        header.postValue(app.getString(R.string.suggestions_newedit_edit))
        saveDrawableRes.postValue(R.drawable.ic_small_done)
    }

    /**
     * Restores all values.
     */
    fun restore() {
        updateAllViews()
    }

    private fun updateAllViews() {
        title.postValue(currentTitle)
        titleSelection.postValue(currentTitle.length)

        updateTimeViews()
    }

    private fun updateTimeViews() {
        voteStartDate.postValue(currentVoteStartDate.toDate())
        voteStartTime.postValue(currentVoteStartDate.toTime())
    }

    fun updateVoteStartDate(year: Int, month: Int, day: Int) {
        // TODO  think to put this into consensus and not the suggestion itself
        currentVoteStartDate = Calendar.getInstance().apply {
            time = Date(currentVoteStartDate)
            set(year, month, day)
        }.timeInMillis

        updateTimeViews()
    }

    fun updateVoteStartTime(hourOfDay: Int, minute: Int) {
        currentVoteStartDate = Calendar.getInstance().apply {
            time = Date(currentVoteStartDate)
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }.timeInMillis

        updateTimeViews()
    }

    fun onOpenDatePicker(view: View) {
        view.closeAttachedKeyboard()
        handle(OpenPickerEvent(true, currentVoteStartDate))
    }

    fun onOpenTimePicker(view: View) {
        view.closeAttachedKeyboard()
        handle(OpenPickerEvent(false, currentVoteStartDate))
    }

    fun onBack() {
        navigateTo(BACK)
    }

    /**
     * Called on a save press.
     */
    fun onSave() {
        val body = SuggestionBody(title = currentTitle, voteStartDate = currentVoteStartDate)

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
            return
        }

        apiErrorHandler.handle(result) { showSnack(message = it.toString(), snackType = SnackType.ERROR) }
    }

    private fun showLoading() {
        bar.postValue(AppToolbar.AppToolbarState.LOADING)
    }

    class OpenPickerEvent(val date: Boolean, val data: Long)
}
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

        updateTextViews()
        updateTimeViews()
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

        updateTextViews()
        updateTimeViews()
    }

    private fun updateTextViews() {
        title.postValue(currentTitle)
        titleSelection.postValue(currentTitle.length)
    }

    private fun updateTimeViews() {
        voteStartDate.postValue(currentVoteStartDate.toDate())
        voteStartTime.postValue(currentVoteStartDate.toTime())
    }

    /**
     * Updates the voting start date by the given date parts.
     *
     * @param year the start date year
     * @param month the start date month
     * @param day the start date day
     */
    fun updateVoteStartDate(year: Int, month: Int, day: Int) {
        // TODO  think to put this into consensus and not the suggestion itself
        currentVoteStartDate = Calendar.getInstance().apply {
            time = Date(currentVoteStartDate)
            set(year, month, day)
        }.timeInMillis

        updateTimeViews()
    }

    /**
     * Updates the voting start time by the given time parts.
     *
     * @param hourOfDay the hour of the vote start time
     * @param minute the minute of the vote start time
     */
    fun updateVoteStartTime(hourOfDay: Int, minute: Int) {
        currentVoteStartDate = Calendar.getInstance().apply {
            time = Date(currentVoteStartDate)
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }.timeInMillis

        updateTimeViews()
    }

    /**
     * Requests to open the date picker for the vote start date.
     *
     * @param view the view requesting the open
     */
    fun onOpenDatePicker(view: View) {
        view.closeAttachedKeyboard()
        handle(OpenPickerEvent(true, currentVoteStartDate))
    }

    /**
     * Requests to open the time picker for the vote start time.
     *
     * @param view the view reqeuesting the open
     */
    fun onOpenTimePicker(view: View) {
        view.closeAttachedKeyboard()
        handle(OpenPickerEvent(false, currentVoteStartDate))
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
        val body =
            SuggestionBody(title = currentTitle, voteStartDate = currentVoteStartDate)

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

    /**
     * A event for opening a date picker or a time picker.
     *
     * @param date set to true to open a date picker, false for a time picker
     * @param data the data containing the initializing time or date for the picker
     */
    class OpenPickerEvent(val date: Boolean, val data: Long)
}
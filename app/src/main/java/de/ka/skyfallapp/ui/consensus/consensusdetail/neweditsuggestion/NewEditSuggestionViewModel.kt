package de.ka.skyfallapp.ui.consensus.consensusdetail.neweditsuggestion

import android.app.Application
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView

import androidx.lifecycle.MutableLiveData
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.base.events.BACK
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.SuggestionBody
import de.ka.skyfallapp.repo.api.SuggestionResponse
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.closeAttachedKeyboard
import de.ka.skyfallapp.utils.start
import de.ka.skyfallapp.utils.with
import java.text.SimpleDateFormat
import java.util.*


/**
 * A view model dealing with editing or creating a new suggestion, depending on the used initializer
 * [setupEdit] or [setupNew].
 */
class NewEditSuggestionViewModel(app: Application) : BaseViewModel(app) {

    val title = MutableLiveData<String>().apply { value = "" }
    val header = MutableLiveData<String>().apply { value = "" }
    val saveText = MutableLiveData<String>().apply { value = "" }
    val titleSelection = MutableLiveData<Int>().apply { value = 0 }
    val voteStartDate = MutableLiveData<String>().apply { value = "" }
    val voteStartTime = MutableLiveData<String>().apply { value = "" }
    val loadingVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val buttonVisibility = MutableLiveData<Int>().apply { value = View.VISIBLE }
    val getTextChangedListener = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) { /* not needed */
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /* not needed */
        }

        override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
            currentTitle = text.toString()
        }
    }
    val getDoneListener = object : TextView.OnEditorActionListener {
        override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.closeAttachedKeyboard()
                return true
            }
            return false
        }
    }

    private var newFromConsensusId: Int = -1
    private var currentSuggestion: SuggestionResponse? = null
    private var currentTitle = ""
    private var currentVoteStartDate = Calendar.getInstance().timeInMillis

    /**
     * Sets up this view model with a given consensus id. This will result in the creation of a new suggestion.
     */
    fun setupNew(consensusId: Int) {
        newFromConsensusId = consensusId
        currentSuggestion = null
        currentTitle = ""
        currentVoteStartDate = Calendar.getInstance().timeInMillis
        title.postValue(currentTitle)
        voteStartDate.postValue((SimpleDateFormat().format(currentVoteStartDate)))
        voteStartTime.postValue((SimpleDateFormat().format(currentVoteStartDate)))
        titleSelection.postValue(currentTitle.length)
        header.postValue(app.getString(R.string.suggestions_newedit_title))
        saveText.postValue(app.getString(R.string.suggestions_newedit_create))
    }

    /**
     * Sets up this view model with a given suggestion. This will result in the update of the suggestion.
     */
    fun setupEdit(suggestion: SuggestionResponse) {
        currentSuggestion = suggestion
        newFromConsensusId = -1
        currentTitle = suggestion.title
        currentVoteStartDate = suggestion.voteStartDate
        title.postValue(currentTitle)
        voteStartDate.postValue((SimpleDateFormat().format(currentVoteStartDate)))
        voteStartTime.postValue((SimpleDateFormat().format(currentVoteStartDate)))
        titleSelection.postValue(currentTitle.length)
        header.postValue(app.getString(R.string.suggestions_newedit_edit))
        saveText.postValue(app.getString(R.string.suggestions_newedit_save))
    }

    fun updateVoteStartDate(year: Int, month: Int, day: Int) {
        //TODO validation ? Maybe only server side + error showing? (date could be set to before consensus date ...)
        currentVoteStartDate = Calendar.getInstance().apply {
            time = Date(currentVoteStartDate)
            set(year, month, day)
        }.timeInMillis
        voteStartDate.postValue((SimpleDateFormat().format(currentVoteStartDate)))
        voteStartTime.postValue((SimpleDateFormat().format(currentVoteStartDate)))
    }

    fun updateVoteStartTime(hourOfDay: Int, minute: Int) {
        currentVoteStartDate = Calendar.getInstance().apply {
            time = Date(currentVoteStartDate)
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }.timeInMillis
        voteStartDate.postValue((SimpleDateFormat().format(currentVoteStartDate)))
        voteStartTime.postValue((SimpleDateFormat().format(currentVoteStartDate)))
    }


    fun onOpenDatePicker(view: View) {
        view.closeAttachedKeyboard()
        handle(OpenPickerEvent(true, currentVoteStartDate))
    }

    fun onOpenTimePicker(view: View) {
        view.closeAttachedKeyboard()
        handle(OpenPickerEvent(false, currentVoteStartDate))
    }

    fun onBack(view: View) {
        view.closeAttachedKeyboard()
        navigateTo(BACK)
    }

    /**
     * Called on a save press.
     */
    fun onSave(view: View) {
        view.closeAttachedKeyboard()
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
        loadingVisibility.postValue(View.GONE)
        buttonVisibility.postValue(View.VISIBLE)

        result.data?.let {
            navigateTo(BACK)
            return
        }

        apiErrorHandler.handle(result) { showSnack(it.toString()) }
    }

    private fun showLoading() {
        loadingVisibility.postValue(View.VISIBLE)
        buttonVisibility.postValue(View.GONE)
    }

    class OpenPickerEvent(val date: Boolean, val data: Long)
}
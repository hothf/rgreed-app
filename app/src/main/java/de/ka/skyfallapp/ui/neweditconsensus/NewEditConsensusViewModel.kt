package de.ka.skyfallapp.ui.neweditconsensus

import android.app.Application
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.MutableLiveData

import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.base.events.BACK
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.ConsensusBody
import de.ka.skyfallapp.repo.api.ConsensusResponse
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.utils.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * A view model dealing with editing or creating a new consensus, depending on the used initializeer
 * [setupEdit] or [setupNew].
 */
class NewEditConsensusViewModel(app: Application) : BaseViewModel(app) {

    val title = MutableLiveData<String>().apply { value = "" }
    val header = MutableLiveData<String>().apply { value = "" }
    val saveText = MutableLiveData<String>().apply { value = "" }
    val titleSelection = MutableLiveData<Int>().apply { value = 0 }
    val finishDate = MutableLiveData<String>().apply { value = "" }
    val finishTime = MutableLiveData<String>().apply { value = "" }
    val loadingVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val buttonVisibility = MutableLiveData<Int>().apply { value = View.VISIBLE }
    val getTitleTextChangedListener = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) { /* not needed */
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /* not needed */
        }

        override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
            currentTitle = text.toString()
        }
    }

    private var currentConsensus: ConsensusResponse? = null
    private var currentTitle = ""
    private var currentFinishDate = Calendar.getInstance().timeInMillis

    /**
     * Sets up this view model with no additional info. This will result in the creation of a new consensus.
     */
    fun setupNew() {
        currentConsensus = null
        currentTitle = ""
        currentFinishDate = Calendar.getInstance().timeInMillis
        title.postValue(currentTitle)
        finishDate.postValue(currentFinishDate.toDate())
        finishTime.postValue(currentFinishDate.toTime())
        titleSelection.postValue(currentTitle.length)
        header.postValue(app.getString(R.string.suggestions_newedit_title))
        saveText.postValue(app.getString(R.string.suggestions_newedit_create))
    }

    /**
     * Sets up this view model with a given consensus. This will result in the update of the consensus.
     */
    fun setupEdit(consensusResponse: ConsensusResponse) {
        currentConsensus = consensusResponse
        currentTitle = consensusResponse.title
        currentFinishDate = consensusResponse.endDate
        title.postValue(currentTitle)
        finishDate.postValue(currentFinishDate.toDate())
        finishTime.postValue(currentFinishDate.toTime())
        titleSelection.postValue(currentTitle.length)
        header.postValue(app.getString(R.string.suggestions_newedit_edit))
        saveText.postValue(app.getString(R.string.suggestions_newedit_save))
    }

    fun updateFinishDate(year: Int, month: Int, day: Int) {
        currentFinishDate = Calendar.getInstance().apply {
            time = Date(currentFinishDate)
            set(year, month, day)
        }.timeInMillis
        finishDate.postValue(currentFinishDate.toDate())
        finishTime.postValue(currentFinishDate.toTime())
    }

    fun updateFinishTime(hourOfDay: Int, minute: Int) {
        currentFinishDate = Calendar.getInstance().apply {
            time = Date(currentFinishDate)
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }.timeInMillis
        finishDate.postValue(currentFinishDate.toDate())
        finishTime.postValue(currentFinishDate.toTime())
    }

    fun onOpenDatePicker(view: View) {
        view.closeAttachedKeyboard()
        handle(OpenFinishPickerEvent(true, currentFinishDate))
    }

    fun onOpenTimePicker(view: View) {
        view.closeAttachedKeyboard()
        handle(OpenFinishPickerEvent(false, currentFinishDate))
    }

    fun onBack(view: View) {
        view.closeAttachedKeyboard()
        navigateTo(BACK)
    }

    fun onSave(view: View) {
        view.closeAttachedKeyboard()
        val body = ConsensusBody(
            title = currentTitle,
            description = "Random description",
            isPublic = true,
            endDate = currentFinishDate,
            privatePassword = ""
        )

        if (currentConsensus != null) {
            repository.consensusManager.updateConsensus(currentConsensus!!.id, body)
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion { onUploaded(it, false) }
                .start(compositeDisposable, ::showLoading)
        } else {
            repository.consensusManager.sendConsensus(body)
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion { onUploaded(it, true) }
                .start(compositeDisposable, ::showLoading)
        }
    }

    private fun onUploaded(result: RepoData<ConsensusResponse?>, update: Boolean) {
        loadingVisibility.postValue(View.GONE)
        buttonVisibility.postValue(View.VISIBLE)

        result.data?.let {
            if (update) {
                navigateTo(BACK)
            } else {
                navigateTo(R.id.action_newConsensusFragment_to_personalFragment)
            }
            return
        }

        apiErrorHandler.handle(result) { showSnack(it.toString()) }
    }

    private fun showLoading() {
        loadingVisibility.postValue(View.VISIBLE)
        buttonVisibility.postValue(View.GONE)
    }

    class OpenFinishPickerEvent(val date: Boolean, val data: Long)
}
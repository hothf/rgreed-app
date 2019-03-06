package de.ka.skyfallapp.ui.neweditconsensus

import android.app.Application
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.MutableLiveData

import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.base.events.BACK
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.ConsensusBody
import de.ka.skyfallapp.repo.api.ConsensusResponse
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.ui.consensus.consensusdetail.ConsensusDetailFragment
import de.ka.skyfallapp.utils.*
import java.util.*

/**
 * A view model dealing with editing or creating a new consensus, depending on the used initializeer
 * [setupEdit] or [setupNew].
 */
class NewEditConsensusViewModel(app: Application) : BaseViewModel(app) {

    private var currentConsensus: ConsensusResponse? = null
    private var currentTitle = ""
    private var currentDescription = ""
    private var currentPrivatePassword = ""
    private var currentFinishDate = Calendar.getInstance().timeInMillis
    private var currentIsPublic = false

    val getDoneListener = ViewUtils.TextDoneListener()
    val title = MutableLiveData<String>().apply { value = "" }
    val header = MutableLiveData<String>().apply { value = "" }
    val saveText = MutableLiveData<String>().apply { value = "" }
    val titleSelection = MutableLiveData<Int>().apply { value = 0 }
    val finishDate = MutableLiveData<String>().apply { value = "" }
    val finishTime = MutableLiveData<String>().apply { value = "" }
    val description = MutableLiveData<String>().apply { value = "" }
    val isNotPublic = MutableLiveData<Boolean>().apply { value = false }
    val privatePassword = MutableLiveData<String>().apply { value = "" }
    val descriptionSelection = MutableLiveData<Int>().apply { value = 0 }
    val privatePasswordSelection = MutableLiveData<Int>().apply { value = 0 }
    val loadingVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val buttonVisibility = MutableLiveData<Int>().apply { value = View.VISIBLE }
    val isPrivatePasswordEnabled = MutableLiveData<Boolean>().apply { value = false }
    val getTitleTextChangedListener = ViewUtils.TextChangeListener { currentTitle = it }
    val getDescriptionChangedListener = ViewUtils.TextChangeListener { currentDescription = it }
    val getPrivatePasswordTextChangedListener = ViewUtils.TextChangeListener { currentPrivatePassword = it }
    val checkedChangeListener = CompoundButton.OnCheckedChangeListener { _, checked ->
        currentIsPublic = !checked
        isPrivatePasswordEnabled.postValue(checked)
    }

    /**
     * Sets up this view model with no additional info. This will result in the creation of a new consensus.
     */
    fun setupNew() {
        currentConsensus = null
        currentTitle = ""
        currentPrivatePassword = ""
        currentIsPublic = true
        currentFinishDate = Calendar.getInstance().timeInMillis

        header.postValue(app.getString(R.string.consensus_newed_title))
        saveText.postValue(app.getString(R.string.consensus_newed__create))

        updateAllViews()
    }

    /**
     * Sets up this view model with a given consensus. This will result in the update of the consensus.
     */
    fun setupEdit(consensusResponse: ConsensusResponse) {
        currentConsensus = consensusResponse
        currentTitle = consensusResponse.title
        currentPrivatePassword = ""
        currentIsPublic = consensusResponse.public
        currentFinishDate = consensusResponse.endDate

        header.postValue(app.getString(R.string.consensus_newed_edit))
        saveText.postValue(app.getString(R.string.consensus_newed__save))

        updateAllViews()
    }

    fun updateFinishDate(year: Int, month: Int, day: Int) {
        currentFinishDate = Calendar.getInstance().apply {
            time = Date(currentFinishDate)
            set(year, month, day)
        }.timeInMillis

        updateTimeViews()
    }

    fun updateFinishTime(hourOfDay: Int, minute: Int) {
        currentFinishDate = Calendar.getInstance().apply {
            time = Date(currentFinishDate)
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }.timeInMillis

        updateTimeViews()
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
            description = currentDescription,
            isPublic = currentIsPublic,
            endDate = currentFinishDate,
            privatePassword = currentPrivatePassword
        )

        if (currentConsensus != null) {
            repository.consensusManager.updateConsensus(currentConsensus!!.id, body)
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion { onUploaded(it, true) }
                .start(compositeDisposable, ::showLoading)
        } else {
            repository.consensusManager.sendConsensus(body)
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion { onUploaded(it, false) }
                .start(compositeDisposable, ::showLoading)
        }
    }

    private fun updateAllViews() {
        title.postValue(currentTitle)
        titleSelection.postValue(currentTitle.length)
        description.postValue(currentDescription)
        descriptionSelection.postValue(currentDescription.length)
        privatePassword.postValue(currentPrivatePassword)
        privatePasswordSelection.postValue(currentPrivatePassword.length)
        isNotPublic.postValue(currentIsPublic.not())

        updateTimeViews()
    }

    private fun updateTimeViews() {
        finishDate.postValue(currentFinishDate.toDate())
        finishTime.postValue(currentFinishDate.toTime())
    }

    private fun onUploaded(result: RepoData<ConsensusResponse?>, update: Boolean) {
        loadingVisibility.postValue(View.GONE)
        buttonVisibility.postValue(View.VISIBLE)

        result.data?.let {
            if (update) {
                navigateTo(BACK)
            } else {
                navigateTo(
                    R.id.action_newConsensusFragment_to_consensusDetailFragment,
                    false,
                    Bundle().apply { putString(ConsensusDetailFragment.CONS_ID_KEY, it.id.toString()) })
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
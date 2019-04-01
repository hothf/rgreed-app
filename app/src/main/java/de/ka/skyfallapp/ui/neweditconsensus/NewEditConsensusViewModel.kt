package de.ka.skyfallapp.ui.neweditconsensus

import android.app.Application
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.lifecycle.MutableLiveData

import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.models.ConsensusBody
import de.ka.skyfallapp.repo.api.models.ConsensusResponse
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.ui.consensus.consensusdetail.ConsensusDetailFragment
import de.ka.skyfallapp.ui.neweditconsensus.NewEditConsensusFragment.Companion.CALLER_FINISH
import de.ka.skyfallapp.ui.neweditconsensus.NewEditConsensusFragment.Companion.CALLER_VOTING
import de.ka.skyfallapp.utils.*
import de.ka.skyfallapp.utils.NavigationUtils.BACK
import java.util.*

/**
 * A view model dealing with editing or creating a new consensus, depending on the used initializer
 * [setupEdit] or [setupNew].
 */
class NewEditConsensusViewModel(app: Application) : BaseViewModel(app) {

    private var currentIsPublic = false
    private var currentTitle = ""
    private var currentDescription = ""
    private var currentPrivatePassword = ""
    private var currentConsensus: ConsensusResponse? = null
    private var currentFinishDate = Calendar.getInstance().timeInMillis
    private var currentVotingStartDate = Calendar.getInstance().timeInMillis

    val getDoneListener = ViewUtils.TextDoneListener()
    val title = MutableLiveData<String>().apply { value = "" }
    val header = MutableLiveData<String>().apply { value = "" }
    val titleSelection = MutableLiveData<Int>().apply { value = 0 }
    val finishDate = MutableLiveData<String>().apply { value = "" }
    val titleError = MutableLiveData<String>().apply { value = "" }
    val finishTime = MutableLiveData<String>().apply { value = "" }
    val description = MutableLiveData<String>().apply { value = "" }
    val endDateError = MutableLiveData<String>().apply { value = "" }
    val isNotPublic = MutableLiveData<Boolean>().apply { value = false }
    val votingStartDate = MutableLiveData<String>().apply { value = "" }
    val votingStartTime = MutableLiveData<String>().apply { value = "" }
    val privatePassword = MutableLiveData<String>().apply { value = "" }
    val descriptionSelection = MutableLiveData<Int>().apply { value = 0 }
    val votingStartDateError = MutableLiveData<String>().apply { value = "" }
    val privatePasswordSelection = MutableLiveData<Int>().apply { value = 0 }
    val saveDrawableRes = MutableLiveData<Int>().apply { value = R.drawable.ic_add }
    val isPrivatePasswordEnabled = MutableLiveData<Boolean>().apply { value = false }
    val bar = MutableLiveData<AppToolbar.AppToolbarState>().apply { value = AppToolbar.AppToolbarState.ACTION_VISIBLE }
    val getTitleTextChangedListener = ViewUtils.TextChangeListener {
        currentTitle = it
        title.postValue(it)
        titleError.postValue("")
    }
    val getDescriptionChangedListener = ViewUtils.TextChangeListener {
        currentDescription = it
        description.postValue(it)
    }
    val getPrivatePasswordTextChangedListener = ViewUtils.TextChangeListener {
        currentPrivatePassword = it
        privatePassword.postValue(it)
    }
    val checkedChangeListener = CompoundButton.OnCheckedChangeListener { _, checked ->
        currentIsPublic = !checked
        isPrivatePasswordEnabled.postValue(checked)
        isNotPublic.postValue(currentIsPublic.not())
    }

    /**
     * Sets up this view model with no additional info. This will result in the creation of a new consensus.
     */
    fun setupNew() {
        currentConsensus = null
        currentTitle = ""
        currentDescription = ""
        currentPrivatePassword = ""
        currentFinishDate = Calendar.getInstance().timeInMillis + (1000 * 60 * 60 * 24 * 2) // two days
        currentVotingStartDate = Calendar.getInstance().timeInMillis + (1000 * 60 * 60 * 24) // a day
        currentIsPublic = true

        header.postValue(app.getString(R.string.consensus_newedit_title))
        saveDrawableRes.postValue(R.drawable.ic_small_add)

        updateTextViews()
        updateTimeViews()
    }

    /**
     * Sets up this view model with a given consensus. This will result in the update of the consensus.
     *
     * @param consensusResponse the consensus
     */
    fun setupEdit(consensusResponse: ConsensusResponse) {
        currentConsensus = consensusResponse
        currentTitle = consensusResponse.title
        currentDescription = consensusResponse.description ?: ""
        currentPrivatePassword = ""
        currentFinishDate = consensusResponse.endDate
        currentVotingStartDate = consensusResponse.votingStartDate
        currentIsPublic = consensusResponse.public

        header.postValue(app.getString(R.string.consensus_newedit_edit))
        saveDrawableRes.postValue(R.drawable.ic_small_done)

        updateTextViews()
        updateTimeViews()
    }

    private fun updateTextViews() {
        title.postValue(currentTitle)
        titleSelection.postValue(currentTitle.length)
        description.postValue(currentDescription)
        descriptionSelection.postValue(currentDescription.length)
        privatePassword.postValue(currentPrivatePassword)
        privatePasswordSelection.postValue(currentPrivatePassword.length)
        isPrivatePasswordEnabled.postValue(currentIsPublic.not())
        isNotPublic.postValue(currentIsPublic.not())
        titleError.postValue("")
    }

    private fun updateTimeViews() {
        finishDate.postValue(currentFinishDate.toDate())
        finishTime.postValue(currentFinishDate.toTime())
        votingStartDate.postValue(currentVotingStartDate.toDate())
        votingStartTime.postValue(currentVotingStartDate.toTime())
        endDateError.postValue("")
        votingStartDateError.postValue("")
    }

    /**
     * Updates the finishing date components of the consensus
     *
     * @param year the finishing date year
     * @param month the finishing date month
     * @param day the finishing date day
     */
    fun updateFinishDate(year: Int, month: Int, day: Int) {
        currentFinishDate = Calendar.getInstance().apply {
            time = Date(currentFinishDate)
            set(year, month, day)
        }.timeInMillis

        updateTimeViews()
    }

    /**
     * Updates the voting start date components of the consensus
     *
     * @param year the start date year
     * @param month the start date month
     * @param day the start date day
     */
    fun updateVoteStartDate(year: Int, month: Int, day: Int) {
        currentVotingStartDate = Calendar.getInstance().apply {
            time = Date(currentVotingStartDate)
            set(year, month, day)
        }.timeInMillis

        updateTimeViews()
    }

    /**
     * Updates the finishing date time components.
     *
     * @param hourOfDay the hour of the finishing time
     * @param minute the minute of the finishing time
     */
    fun updateFinishTime(hourOfDay: Int, minute: Int) {
        currentFinishDate = Calendar.getInstance().apply {
            time = Date(currentFinishDate)
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }.timeInMillis

        updateTimeViews()
    }

    /**
     * Updates the voting start date time components.
     *
     * @param hourOfDay the hour of the vote start time
     * @param minute the minute of the vote start time
     */
    fun updateVoteStartTime(hourOfDay: Int, minute: Int) {
        currentVotingStartDate = Calendar.getInstance().apply {
            time = Date(currentVotingStartDate)
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }.timeInMillis

        updateTimeViews()
    }

    /**
     * Requests to open the date picker for the finishing date.
     *
     * @param view the view requesting the open
     */
    fun onOpenDatePicker(view: View) {
        view.closeAttachedKeyboard()
        handle(OpenPickerEvent(true, currentFinishDate, CALLER_FINISH))
    }

    /**
     * Requests to open the time picker for the finishing date.
     *
     * @param view the view requesting the open
     */
    fun onOpenTimePicker(view: View) {
        view.closeAttachedKeyboard()
        handle(OpenPickerEvent(false, currentFinishDate, CALLER_FINISH))
    }

    /**
     * Requests to open the date picker for the voting start date.
     *
     * @param view the view requesting the open
     */
    fun onOpenVotingStartDatePicker(view: View) {
        view.closeAttachedKeyboard()
        handle(OpenPickerEvent(true, currentVotingStartDate, CALLER_VOTING))
    }

    /**
     * Requests to open the time picker for the voting start date.
     *
     * @param view the view requesting the open
     */
    fun onOpenVotingStartTimePicker(view: View) {
        view.closeAttachedKeyboard()
        handle(OpenPickerEvent(false, currentVotingStartDate, CALLER_VOTING))
    }

    /**
     * Goes back
     */
    fun onBack() {
        navigateTo(BACK)
    }

    /**
     * Called on saving the current manipulating (either editing or creating the consensus).
     */
    fun onSave() {
        InputValidator(
            listOf(
                ValidatorInput(currentTitle, titleError, listOf(ValidationRules.NOT_EMPTY))
            )
        ).apply {
            if (!validateAll(app)) {
                return
            }
        }

        val body = ConsensusBody(
            title = currentTitle,
            description = currentDescription,
            isPublic = currentIsPublic,
            endDate = currentFinishDate,
            privatePassword = currentPrivatePassword,
            votingStartDate = currentVotingStartDate
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

    private fun onUploaded(result: RepoData<ConsensusResponse?>, update: Boolean) {
        bar.postValue(AppToolbar.AppToolbarState.ACTION_VISIBLE)

        result.data?.let {
            if (update) {
                navigateTo(BACK)
            } else {
                navigateTo(
                    navigationTargetId = R.id.action_newConsensusFragment_to_consensusDetailFragment,
                    args = Bundle().apply { putString(ConsensusDetailFragment.CONS_ID_KEY, it.id.toString()) },
                    popupToId = R.id.newConsensusFragment
                )
            }
        }

        result.repoError?.errors?.forEach {
            when (it.parameter) {
                "titleText" -> titleError.postValue(it.localizedMessage(app))
                "endDate" -> endDateError.postValue(it.localizedMessage(app))
                "votingStartDate" -> votingStartDateError.postValue(it.localizedMessage(app))
            }
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
     * @param caller the caller id to identify which method has called this
     */
    class OpenPickerEvent(val date: Boolean, val data: Long, val caller: Int)
}
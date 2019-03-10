package de.ka.skyfallapp.ui.neweditconsensus

import android.app.Application
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.lifecycle.MutableLiveData

import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.ConsensusBody
import de.ka.skyfallapp.repo.api.ConsensusResponse
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.ui.consensus.consensusdetail.ConsensusDetailFragment
import de.ka.skyfallapp.utils.*
import de.ka.skyfallapp.utils.NavigationUtils.BACK
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
    val titleSelection = MutableLiveData<Int>().apply { value = 0 }
    val finishDate = MutableLiveData<String>().apply { value = "" }
    val finishTime = MutableLiveData<String>().apply { value = "" }
    val description = MutableLiveData<String>().apply { value = "" }
    val isNotPublic = MutableLiveData<Boolean>().apply { value = false }
    val privatePassword = MutableLiveData<String>().apply { value = "" }
    val descriptionSelection = MutableLiveData<Int>().apply { value = 0 }
    val privatePasswordSelection = MutableLiveData<Int>().apply { value = 0 }
    val saveDrawableRes = MutableLiveData<Int>().apply { value = R.drawable.ic_add }
    val isPrivatePasswordEnabled = MutableLiveData<Boolean>().apply { value = false }
    val getTitleTextChangedListener = ViewUtils.TextChangeListener { currentTitle = it }
    val getDescriptionChangedListener = ViewUtils.TextChangeListener { currentDescription = it }
    val getPrivatePasswordTextChangedListener = ViewUtils.TextChangeListener { currentPrivatePassword = it }
    val bar = MutableLiveData<AppToolbar.AppToolbarState>().apply { value = AppToolbar.AppToolbarState.ACTION_VISIBLE }
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
        currentDescription = ""
        currentPrivatePassword = ""
        currentFinishDate = Calendar.getInstance().timeInMillis
        currentIsPublic = true

        header.postValue(app.getString(R.string.consensus_newedit_title))
        saveDrawableRes.postValue(R.drawable.ic_small_add)

        updateAllViews()
    }

    /**
     * Sets up this view model with a given consensus. This will result in the update of the consensus.
     */
    fun setupEdit(consensusResponse: ConsensusResponse) {
        currentConsensus = consensusResponse
        currentTitle = consensusResponse.title
        currentDescription = consensusResponse.description ?: ""
        currentPrivatePassword = ""
        currentFinishDate = consensusResponse.endDate
        currentIsPublic = consensusResponse.public

        header.postValue(app.getString(R.string.consensus_newedit_edit))
        saveDrawableRes.postValue(R.drawable.ic_small_done)

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

    fun onBack() {
        navigateTo(BACK)
    }

    fun onSave() {
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
            return
        }

        apiErrorHandler.handle(result) { showSnack(it.toString()) }
    }

    private fun showLoading() {
        bar.postValue(AppToolbar.AppToolbarState.LOADING)
    }

    class OpenFinishPickerEvent(val date: Boolean, val data: Long)
}
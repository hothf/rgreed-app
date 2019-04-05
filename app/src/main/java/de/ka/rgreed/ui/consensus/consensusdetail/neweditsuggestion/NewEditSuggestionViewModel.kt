package de.ka.rgreed.ui.consensus.consensusdetail.neweditsuggestion

import android.app.Application


import androidx.lifecycle.MutableLiveData
import de.ka.rgreed.R
import de.ka.rgreed.base.BaseViewModel
import de.ka.rgreed.repo.RepoData
import de.ka.rgreed.repo.api.models.SuggestionBody
import de.ka.rgreed.repo.api.models.SuggestionResponse
import de.ka.rgreed.repo.subscribeRepoCompletion
import de.ka.rgreed.utils.*
import de.ka.rgreed.utils.NavigationUtils.BACK


/**
 * A view model dealing with editing or creating a new suggestion, depending on the used initializer
 * [setupEdit] or [setupNew].
 */
class NewEditSuggestionViewModel(app: Application) : BaseViewModel(app) {

    private var newFromConsensusId: Int = -1
    private var currentSuggestion: SuggestionResponse? = null
    private var currentTitle = ""

    val getDoneListener = ViewUtils.TextDoneListener { onSave() }
    val title = MutableLiveData<String>().apply { value = "" }
    val header = MutableLiveData<String>().apply { value = "" }
    val titleError = MutableLiveData<String>().apply { value = "" }
    val titleSelection = MutableLiveData<Int>().apply { value = 0 }
    val saveDrawableRes = MutableLiveData<Int>().apply { value = R.drawable.ic_add }
    val bar = MutableLiveData<AppToolbar.AppToolbarState>().apply { value = AppToolbar.AppToolbarState.ACTION_VISIBLE }
    val getTextChangedListener = ViewUtils.TextChangeListener {
        currentTitle = it
        title.value = it
        titleError.postValue("")
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
        titleError.postValue("")
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
        // perform a quick low level validation
        InputValidator(
            listOf(
                ValidatorInput(currentTitle, titleError, listOf(ValidationRules.NOT_EMPTY))
            )
        ).apply {
            if (!validateAll(app)) {
                return
            }
        }

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

        result.repoError?.errors?.forEach {
            when (it.parameter) {
                "titleText" -> titleError.postValue(it.localizedMessage(app))
            }
        }
    }

    private fun showLoading() {
        bar.postValue(AppToolbar.AppToolbarState.LOADING)
    }
}
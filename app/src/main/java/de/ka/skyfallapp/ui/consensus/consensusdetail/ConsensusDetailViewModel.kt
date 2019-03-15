package de.ka.skyfallapp.ui.consensus.consensusdetail

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.base.events.AnimType
import de.ka.skyfallapp.base.events.SnackType
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.models.ConsensusResponse
import de.ka.skyfallapp.repo.api.models.RequestAccessBody
import de.ka.skyfallapp.repo.api.models.SuggestionResponse
import de.ka.skyfallapp.repo.api.models.VoteBody
import de.ka.skyfallapp.repo.subscribeRepoCompletion

import de.ka.skyfallapp.ui.consensus.consensusdetail.suggestionlist.SuggestionsAdapter
import de.ka.skyfallapp.ui.consensus.consensusdetail.neweditsuggestion.NewEditSuggestionFragment
import de.ka.skyfallapp.utils.*
import de.ka.skyfallapp.utils.NavigationUtils.BACK


import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator
import okhttp3.ResponseBody

/**
 * The view model for displaying consensus detail data. Depending on the state of the consensus and the login state
 * of the user, this will display differently.
 *
 * Also, keep in mind that a consensus can be private. In this case a [LockView] will be displayed over the actual
 * content.
 */
class ConsensusDetailViewModel(app: Application) : BaseViewModel(app), LockView.UnlockListener {

    private var currentConsensus: ConsensusResponse? = null
    private var currentId = -1

    val unlockListener: LockView.UnlockListener = this
    val adapter = MutableLiveData<SuggestionsAdapter>()
    val title = MutableLiveData<String>().apply { value = "" }
    val creator = MutableLiveData<String>().apply { value = "" }
    val endDate = MutableLiveData<String>().apply { value = "" }
    val voterCount = MutableLiveData<String>().apply { value = "" }
    val description = MutableLiveData<String>().apply { value = "" }
    val refresh = MutableLiveData<Boolean>().apply { value = false }
    val creationDate = MutableLiveData<String>().apply { value = "" }
    val voterVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val blankVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val adminVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val publicVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val unlockedVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val finishedVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val descriptionVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val swipeToRefreshListener = SwipeRefreshLayout.OnRefreshListener { refreshDetails() }
    val unlockState = MutableLiveData<LockView.LockedViewState>().apply { value = LockView.LockedViewState.HIDDEN }
    val statusColor = MutableLiveData<Int>().apply {
        value = ContextCompat.getColor(app.applicationContext, R.color.colorStatusLocked)
    }

    private val voteClickListener = { suggestion: SuggestionResponse ->
        val consensus = currentConsensus
        if (consensus != null && consensus.finished) {
            showSnack(app.getString(R.string.consensus_detail_cannot_vote), SnackType.DEFAULT)
        } else {
            handle(SuggestionVoteAsk(suggestion))
        }
    }
    private val addMoreClickListener = {
        navigateTo(
            R.id.action_consensusDetailFragment_to_newSuggestionFragment,
            false,
            Bundle().apply { putString(NewEditSuggestionFragment.CONS_ID_KEY, currentId.toString()) },
            animType = AnimType.MODAL
        )
    }

    init {
        repository.consensusManager.observableSuggestions
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onError = { error -> error.printStackTrace() },
                onNext = {
                    if (it.invalidate) {
                        refreshDetails()
                    } else {
                        adapter.value?.insert(it.list, currentConsensus?.finished ?: false)

                        if (it.list.isEmpty()) {
                            blankVisibility.postValue(View.VISIBLE)
                            voterVisibility.postValue(View.GONE)
                        } else {
                            blankVisibility.postValue(View.GONE)
                            voterVisibility.postValue(View.VISIBLE)
                        }
                    }
                }
            )
            .addTo(compositeDisposable)

        repository.consensusManager.observableConsensuses
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onError = { error -> error.printStackTrace() },
                onNext = {
                    // either the whole list of items has been changed, or a single item. This is a special case,
                    // for example, when coming from a deeplink, we do no want to add this to the whole list, but still
                    // be able to show the details
                    if (it.item != null) {
                        updateDetails(it.item)
                    } else {
                        it.list.find { consensus -> consensus.id == currentId }?.let(::updateDetails)
                    }
                }
            )
            .addTo(compositeDisposable)

        repository.profileManager.observableProfile
            .with(AndroidSchedulerProvider())
            .subscribeBy(onNext = { refreshDetails() })
            .addTo(compositeDisposable)
    }

    /**
     * Sets up the whole view and loads the needed data, but only if the consensus is not already displayed - in that
     * case the data is simply update.
     *
     * @param owner the lifecycle owner, needed for keeping new data in sync with the lifecycle owner
     * @param id the id of the consensus to display.
     */
    fun setupAdapterAndLoad(owner: LifecycleOwner, id: Int) {
        if (currentId == id) {
            currentConsensus?.let { updateDetails(it) }
            return
        }

        currentId = id

        adapter.value = (SuggestionsAdapter(
            owner = owner,
            addMoreClickListener = addMoreClickListener,
            voteClickListener = voteClickListener,
            toolsClickListener = ::askForSuggestionTools
        ))

        // TODO add a nicer empty state .. because we could come to this screen with no internet connection and it will look a bit ugly.
        // resets all current saved details
        currentConsensus = null
        title.postValue("")
        description.postValue("")
        creator.postValue("")
        creationDate.postValue("")
        endDate.postValue("")
        voterCount.postValue("")
        blankVisibility.postValue(View.GONE)
        voterVisibility.postValue(View.GONE)
        adminVisibility.postValue(View.GONE)
        publicVisibility.postValue(View.GONE)
        finishedVisibility.postValue(View.GONE)
        unlockedVisibility.postValue(View.GONE)
        unlockState.value = LockView.LockedViewState.HIDDEN
        statusColor.postValue(ContextCompat.getColor(app.applicationContext, R.color.colorStatusLocked))

        refreshDetails()
    }

    private fun refreshDetails() {
        repository.consensusManager.getConsensusDetail(currentId)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion { onDetailsLoaded(it, fromLock = false) }
            .start(compositeDisposable) {
                showLoading()
                showLockLoading()
            }
    }

    override fun onUnlockRequested(password: String) {
        currentConsensus?.let {
            repository.consensusManager.sendConsensusAccessRequest(
                it.id,
                RequestAccessBody(password)
            )
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion { result -> onDetailsLoaded(result, fromLock = true) }
                .start(compositeDisposable, ::showLockLoading)
        }
    }

    /**
     * Requests the deletion of the consensus. Should only be possible for users, that are administrators of
     * the consensus.
     */
    fun deleteConsensus() {
        currentConsensus?.let {
            repository.consensusManager.deleteConsensus(it.id)
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion(::showDeletion)
                .start(compositeDisposable, ::showLoading)
        }
    }

    /**
     * Requests the deletion of a suggestion. Should only be possible for the administrators of the consensus and the
     * creator of the suggestion.
     *
     * @param suggestionId the id of the suggestion
     */
    fun deleteSuggestion(suggestionId: Int) {
        currentConsensus?.let {
            repository.consensusManager.deleteSuggestion(it.id, suggestionId)
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion { refresh.postValue(false) }
                .start(compositeDisposable, ::showLoading)
        }
    }

    /**
     * Loads suggestions of the consensus.
     */
    private fun loadSuggestions() {
        currentConsensus?.let {
            repository.consensusManager.getConsensusSuggestions(it.id)
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion(::onSuggestionsLoaded)
                .start(compositeDisposable, ::showLoading)
        }
    }

    /**
     * Votes a suggestion.
     */
    fun voteOnSuggestion(suggestionResponse: SuggestionResponse, amount: Float) {
        repository.consensusManager.voteForSuggestion(
            suggestionResponse.consensusId, suggestionResponse.id, VoteBody(amount)
        )
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion(::handleVotingResult)
            .start(compositeDisposable, ::showLoading)
    }

    private fun handleVotingResult(result: RepoData<SuggestionResponse?>) {
        refresh.postValue(false)
    }

    /**
     * Asks for the tools to manipulate suggestions.
     *
     * @param view the view to ask for the tools
     * @param suggestionResponse the suggestion to edit
     */
    fun askForSuggestionTools(view: View, suggestionResponse: SuggestionResponse) {
        handle(SuggestionToolsAsk(view, suggestionResponse))
    }

    /**
     * Asks for the tools to manipulate a consensus.
     *
     * @param view the view to ask for the tools
     */
    fun askForConsensusTools(view: View) {
        handle(ConsensusToolsAsk(view, currentConsensus))
    }

    /**
     * Goes back.
     */
    fun onBack() {
        navigateTo(BACK)
    }

    /**
     * Called on a click on voter.
     */
    fun onVoterClick() {
        handle(VoterDialogAsk(currentConsensus?.voters ?: listOf()))
    }

    private fun showDeletion(result: RepoData<ResponseBody?>) {
        refresh.postValue(false)

        if (result.info.code == 200) {
            navigateTo(BACK)
        }
    }

    private fun updateDetails(it: ConsensusResponse) {
        currentConsensus = it

        title.postValue(it.title)
        description.postValue(it.description)
        creator.postValue(it.creator)
        creationDate.postValue(it.creationDate.toDateTime())
        endDate.postValue(it.endDate.toDateTime())
        voterCount.postValue(it.voters.size.toString())

        if (it.description.isNullOrBlank()) {
            descriptionVisibility.postValue(View.GONE)
        } else {
            descriptionVisibility.postValue(View.VISIBLE)
        }

        if (it.admin) {
            adminVisibility.postValue(View.VISIBLE)
        } else {
            adminVisibility.postValue(View.GONE)
        }

        if (it.public) {
            publicVisibility.postValue(View.VISIBLE)
        } else {
            publicVisibility.postValue(View.GONE)
        }

        var statColor = ContextCompat.getColor(app.applicationContext, R.color.colorStatusLocked)
        var lockState = LockView.LockedViewState.HIDE

        if (it.hasAccess) { // has to be before finished, to show the right color
            statColor = ContextCompat.getColor(app.applicationContext, R.color.colorStatusUnlocked)
            unlockedVisibility.postValue(View.VISIBLE)
        } else {
            lockState = LockView.LockedViewState.SHOW
            unlockedVisibility.postValue(View.GONE)
        }

        if (it.finished) {
            statColor = ContextCompat.getColor(app.applicationContext, R.color.colorStatusFinished)
            finishedVisibility.postValue(View.VISIBLE)
            lockState = LockView.LockedViewState.HIDE
        } else {
            finishedVisibility.postValue(View.GONE)
        }

        unlockState.postValue(lockState)
        statusColor.postValue(statColor)
    }

    private fun onDetailsLoaded(result: RepoData<ConsensusResponse?>, fromLock: Boolean) {
        refresh.postValue(false)

        result.data?.let {
            loadSuggestions()

            if (fromLock && !it.hasAccess) {
                unlockState.postValue(LockView.LockedViewState.ERROR) // must be wrong password.. TODO animate on error?
            }
        }

        if (result.data == null) {
            if (fromLock) {
                unlockState.postValue(LockView.LockedViewState.ERROR)
            }
        }
    }

    private fun onSuggestionsLoaded(result: RepoData<List<SuggestionResponse>?>) {
        refresh.postValue(false)
        //TODO handle errors
    }

    private fun showLockLoading() {
        unlockState.postValue(LockView.LockedViewState.LOAD)
    }

    private fun showLoading() {
        refresh.postValue(true)
    }

    fun itemAnimator() = SlideInDownAnimator()

    fun layoutManager() = LinearLayoutManager(app.applicationContext)

    /**
     * Asks for the tools of a suggestion for manipulation.
     *
     * @param view the view asking
     * @param data the suggestion
     */
    class SuggestionToolsAsk(val view: View, val data: SuggestionResponse)

    /**
     * Asks for the tools of a consensus for manipulator.
     *
     * @param view the view asking
     * @param data the consensus
     */
    class ConsensusToolsAsk(val view: View, val data: ConsensusResponse?)

    /**
     * Asks for the voting of a suggestion.
     *
     * @param suggestion the suggestion to vote on
     */
    class SuggestionVoteAsk(val suggestion: SuggestionResponse)

    /**
     * Asks for a dialog with a list of voters.
     */
    class VoterDialogAsk(val voters: List<String>)
}

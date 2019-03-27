package de.ka.skyfallapp.ui.consensus.consensusdetail

import android.app.Application
import android.graphics.drawable.Drawable
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
import de.ka.skyfallapp.repo.api.models.*
import de.ka.skyfallapp.repo.subscribeRepoCompletion

import de.ka.skyfallapp.ui.consensus.consensusdetail.suggestionlist.SuggestionsAdapter
import de.ka.skyfallapp.ui.consensus.consensusdetail.neweditsuggestion.NewEditSuggestionFragment
import de.ka.skyfallapp.ui.consensus.consensuslist.ConsensusItemDecoration
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

    val hasTransparentActionButton = true
    val actionDrawableRes = R.drawable.ic_more_horiz
    val unlockListener: LockView.UnlockListener = this
    val adapter = MutableLiveData<SuggestionsAdapter>()
    val title = MutableLiveData<String>().apply { value = "" }
    val status = MutableLiveData<String>().apply { value = "" }
    val creator = MutableLiveData<String>().apply { value = "" }
    val endDate = MutableLiveData<String>().apply { value = "" }
    val voterCount = MutableLiveData<String>().apply { value = "0" }
    val refresh = MutableLiveData<Boolean>().apply { value = false }
    val creationDate = MutableLiveData<String>().apply { value = "" }
    val votingStartDate = MutableLiveData<String>().apply { value = "" }
    val controlsEnabled = MutableLiveData<Boolean>().apply { value = true }
    val blankVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val adminVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val addMoreVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val swipeToRefreshListener = SwipeRefreshLayout.OnRefreshListener { refreshDetails() }
    val itemDecoration = ConsensusItemDecoration(app.resources.getDimensionPixelSize(R.dimen.default_8))
    val votedColor = MutableLiveData<Int>().apply { value = ContextCompat.getColor(app, R.color.colorAccent) }
    val followingColor = MutableLiveData<Int>().apply { value = ContextCompat.getColor(app, R.color.colorAccent) }
    val bar = MutableLiveData<AppToolbar.AppToolbarState>().apply { value = AppToolbar.AppToolbarState.NO_ACTION }
    val unlockState = MutableLiveData<LockView.LockedViewState>().apply { value = LockView.LockedViewState.HIDDEN }
    val creatorColor = MutableLiveData<Int>().apply { value = ContextCompat.getColor(app, R.color.fontDefaultInverted) }
    val description =
        MutableLiveData<String>().apply { value = app.getString(R.string.consensus_detail_no_description) }
    val followingIcon =
        MutableLiveData<Drawable>().apply { value = ContextCompat.getDrawable(app, R.drawable.ic_follow) }
    val statusBackground =
        MutableLiveData<Drawable>().apply { value = ContextCompat.getDrawable(app, R.drawable.bg_rounded_unknown) }

    private val voteClickListener = { suggestion: SuggestionResponse ->
        currentConsensus?.let {
            when {
                it.finished || it.votingStartDate > System.currentTimeMillis() -> handle(
                    SuggestionInfoAsk(
                        it,
                        suggestion
                    )
                )
                else -> handle(SuggestionVoteAsk(suggestion))
            }
        }
        Unit
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
                        adapter.value?.insert(
                            app,
                            it.list.sortedBy { list -> list.overallAcceptance },
                            currentConsensus?.finished ?: false,
                            currentConsensus?.votingStartDate ?: 0
                        )

                        if (it.list.isEmpty()) {
                            blankVisibility.postValue(View.VISIBLE)
                        } else {
                            blankVisibility.postValue(View.GONE)
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
                    // for example, when coming from a deep link, we do no want to add this to the whole list, but still
                    // be able to show the details
                    if (it.item != null) {
                        updateDetails(it.item)
                    } else {
                        it.list.find { consensus -> consensus.id == currentId }?.let(::updateDetails)
                    }
                }
            )
            .addTo(compositeDisposable)

        repository.profileManager.observableLoginLogoutProfile
            .with(AndroidSchedulerProvider())
            .subscribeBy(onNext = { refreshDetails() })
            .addTo(compositeDisposable)
    }

    fun onTitle() {
        showSnack(currentConsensus?.title ?: "", SnackType.DEFAULT)
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
            voteClickListener = voteClickListener,
            toolsClickListener = ::askForSuggestionTools
        ))

        // resets all current saved details
        currentConsensus = null

        title.postValue("")
        status.postValue("")
        creator.postValue("")
        endDate.postValue("")
        voterCount.postValue("0")
        description.postValue("")
        creationDate.postValue("")
        votingStartDate.postValue("")
        controlsEnabled.postValue(true)
        blankVisibility.postValue(View.GONE)
        adminVisibility.postValue(View.GONE)
        addMoreVisibility.postValue(View.GONE)
        bar.postValue(AppToolbar.AppToolbarState.NO_ACTION)
        unlockState.value = LockView.LockedViewState.HIDDEN
        description.postValue(app.getString(R.string.consensus_detail_no_description))
        creatorColor.postValue(ContextCompat.getColor(app, R.color.fontDefaultInverted))
        votedColor.postValue(ContextCompat.getColor(app.applicationContext, R.color.colorAccent))
        followingColor.postValue(ContextCompat.getColor(app.applicationContext, R.color.colorAccent))
        statusBackground.postValue(ContextCompat.getDrawable(app, R.drawable.bg_rounded_unknown))
        followingIcon.postValue(ContextCompat.getDrawable(app, R.drawable.ic_follow))

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
                .subscribeRepoCompletion { result ->
                    hideLoading()
                    showDeletion(result)
                }
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
                .subscribeRepoCompletion { hideLoading() }
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
                .subscribeRepoCompletion { hideLoading() }
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
            .subscribeRepoCompletion { hideLoading() }
            .start(compositeDisposable, ::showLoading)
    }

    /**
     * Called on a click on follow.
     */
    fun onFollowClick() {
        currentConsensus?.let {
            if (controlsEnabled.value != null && controlsEnabled.value == false) {
                return
            }
            repository.consensusManager.postFollowConsensus(it.id, FollowBody(!it.following))
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion { hideLoading() }
                .start(compositeDisposable, ::showLoading)
        }
    }

    /**
     * Called when clicked on add more suggestions.
     */
    fun onAddMoreClick() {
        navigateTo(
            R.id.action_consensusDetailFragment_to_newSuggestionFragment,
            false,
            Bundle().apply { putString(NewEditSuggestionFragment.CONS_ID_KEY, currentId.toString()) },
            animType = AnimType.MODAL
        )
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
        votingStartDate.postValue(it.votingStartDate.toDateTime())

        if (it.votingStartDate >= System.currentTimeMillis()) {
            addMoreVisibility.postValue(View.VISIBLE)
        } else {
            addMoreVisibility.postValue(View.GONE)
        }

        if (it.title.length > 25) {
            bar.postValue(AppToolbar.AppToolbarState.ACTION_VISIBLE)
        } else {
            bar.postValue(AppToolbar.AppToolbarState.NO_ACTION)
        }

        if (it.description.isNullOrBlank()) {
            description.postValue(app.getString(R.string.consensus_detail_no_description))
        } else {
            description.postValue(it.description)
        }

        if (it.admin) {
            adminVisibility.postValue(View.VISIBLE)
        } else {
            adminVisibility.postValue(View.GONE)
        }

        if (it.following) {
            followingColor.postValue(ContextCompat.getColor(app, R.color.colorHighlight))
            followingIcon.postValue(ContextCompat.getDrawable(app, R.drawable.ic_unfollow))
        } else {
            followingColor.postValue(ContextCompat.getColor(app, R.color.colorAccent))
            followingIcon.postValue(ContextCompat.getDrawable(app, R.drawable.ic_follow))
        }

        if (it.voters.contains(repository.profileManager.currentProfile.username)) {
            votedColor.postValue(ContextCompat.getColor(app, R.color.colorHighlight))
        } else {
            votedColor.postValue(ContextCompat.getColor(app, R.color.colorAccent))
        }

        var lockState = LockView.LockedViewState.HIDE
        if (!it.hasAccess) {
            lockState = LockView.LockedViewState.SHOW
        }

        if (it.creator == repository.profileManager.currentProfile.username) {
            creatorColor.postValue(ContextCompat.getColor(app, R.color.colorHighlight))
        } else {
            creatorColor.postValue(ContextCompat.getColor(app, R.color.fontDefaultInverted))
        }

        var statusText = app.getString(R.string.consensus_detail_status_public)
        if (!it.public) {
            statusText = app.getString(R.string.consensus_detail_status_private)
        }

        if (it.finished) {
            statusBackground.postValue(ContextCompat.getDrawable(app, R.drawable.bg_rounded_finished))
            statusText = "$statusText, ${app.getString(R.string.consensus_detail_status_finished)}"
            lockState = LockView.LockedViewState.HIDE
        } else {
            statusBackground.postValue(ContextCompat.getDrawable(app, R.drawable.bg_rounded_open))
        }

        status.postValue(statusText)

        unlockState.postValue(lockState)
    }

    private fun onDetailsLoaded(result: RepoData<ConsensusResponse?>, fromLock: Boolean) {
        hideLoading()

        result.data?.let {
            loadSuggestions()

            if (fromLock && !it.hasAccess) {
                unlockState.postValue(LockView.LockedViewState.ERROR)
                showSnack(app.getString(R.string.error_input_wrong_password), SnackType.ERROR)
            }
        }

        if (result.data == null && fromLock) {
            unlockState.postValue(LockView.LockedViewState.ERROR)
            showSnack(app.getString(R.string.error_input_wrong_password), SnackType.ERROR)
        }
    }

    private fun showLockLoading() {
        unlockState.postValue(LockView.LockedViewState.LOAD)
    }

    private fun showLoading() {
        controlsEnabled.postValue(false)
        refresh.postValue(true)
    }

    private fun hideLoading() {
        controlsEnabled.postValue(true)
        refresh.postValue(false)
    }

    fun itemAnimator() = SlideInDownAnimator()

    fun layoutManager() = LinearLayoutManager(app.applicationContext)

    /**
     * Asks for the tools of a consensus for manipulator.
     *
     * @param view the view asking
     * @param data the consensus
     */
    class ConsensusToolsAsk(val view: View, val data: ConsensusResponse?)

    /**
     * Asks for the tools of a suggestion for manipulation.
     *
     * @param view the view asking
     * @param data the suggestion
     */
    class SuggestionToolsAsk(val view: View, val data: SuggestionResponse)

    /**
     *  Asks for the suggestions info dialog.
     *
     *  @param consensus the consensus
     *  @param suggestion the suggestion
     */
    class SuggestionInfoAsk(val consensus: ConsensusResponse, val suggestion: SuggestionResponse)

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

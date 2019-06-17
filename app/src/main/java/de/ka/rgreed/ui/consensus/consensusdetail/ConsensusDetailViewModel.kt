package de.ka.rgreed.ui.consensus.consensusdetail

import android.app.Application
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.content.ContextCompat

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.ka.rgreed.R
import de.ka.rgreed.base.BaseViewModel
import de.ka.rgreed.base.events.AnimType
import de.ka.rgreed.repo.RepoData
import de.ka.rgreed.repo.api.models.*
import de.ka.rgreed.repo.subscribeRepoCompletion

import de.ka.rgreed.ui.consensus.consensusdetail.suggestionlist.SuggestionsAdapter
import de.ka.rgreed.ui.consensus.consensusdetail.neweditsuggestion.NewEditSuggestionFragment
import de.ka.rgreed.ui.consensus.consensuslist.ConsensusItemDecoration
import de.ka.rgreed.utils.*
import de.ka.rgreed.utils.NavigationUtils.BACK


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

    val refresherHandler = Handler()
    val hasTransparentActionButton = true
    val actionDrawableRes = R.drawable.ic_more_horiz
    val unlockListener: LockView.UnlockListener = this
    val adapter = MutableLiveData<SuggestionsAdapter>()
    val votingStartTime =
        MutableLiveData<TimeAwareUpdate>().apply {
            value = TimeAwareUpdate(R.string.consensus_detail_votingstartdate_placeholder, 0, true)
        }
    val title = MutableLiveData<String>().apply { value = "" }
    val creator = MutableLiveData<String>().apply { value = "" }
    val endTime = MutableLiveData<TimeAwareUpdate>().apply {
        value = TimeAwareUpdate(R.string.consensus_detail_enddate_placeholder, 0, true)
    }
    val voterCount = MutableLiveData<String>().apply { value = "0" }
    val refresh = MutableLiveData<Boolean>().apply { value = false }
    val creationDate = MutableLiveData<String>().apply { value = "" }
    val adminVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val addMoreVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val refresherToggle = MutableLiveData<Boolean>().apply { value = false }
    val swipeToRefreshListener = SwipeRefreshLayout.OnRefreshListener { refreshDetails() }
    val itemDecoration = ConsensusItemDecoration(
        app.resources.getDimensionPixelSize(R.dimen.default_8),
        app.resources.getDimensionPixelSize(R.dimen.default_8)
    )
    val statusImage = MutableLiveData<Drawable>().apply { value = ContextCompat.getDrawable(app, R.drawable.ic_follow) }
    val statusColor = MutableLiveData<Int>().apply { value = ContextCompat.getColor(app, R.color.colorStatusUnknown) }
    val votedColor = MutableLiveData<Int>().apply { value = ContextCompat.getColor(app, R.color.colorAccent) }
    val followingColor = MutableLiveData<Int>().apply { value = ContextCompat.getColor(app, R.color.colorAccent) }
    val bar = MutableLiveData<AppToolbar.AppToolbarState>().apply { value = AppToolbar.AppToolbarState.NO_ACTION }
    val unlockState = MutableLiveData<LockView.LockedViewState>().apply { value = LockView.LockedViewState.HIDDEN }
    val description =
        MutableLiveData<String>().apply { value = app.getString(R.string.consensus_detail_no_description) }
    val followingIcon =
        MutableLiveData<Drawable>().apply { value = ContextCompat.getDrawable(app, R.drawable.ic_follow) }
    val statusBackground =
        MutableLiveData<Drawable>().apply { value = ContextCompat.getDrawable(app, R.drawable.bg_rounded_unknown) }

    private val controlsEnabled = MutableLiveData<Boolean>().apply { value = true }
    private val voteClickListener = { suggestion: SuggestionResponse, placement: Int ->
        currentConsensus?.let {
            when {
                it.finished || it.votingStartDate > System.currentTimeMillis() -> handle(
                    SuggestionInfoAsk(
                        it,
                        suggestion,
                        placement
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
                onError = ::handleGeneralError,
                onNext = { result ->
                    if (result.invalidate) {
                        refreshDetails()
                    } else {
                        adapter.value?.let {
                            val isFinished = currentConsensus?.finished ?: false
                            val list = if (isFinished) {
                                result.list.sortedBy { list -> list.overallAcceptance }
                            } else {
                                result.list.sortedByDescending { list -> list.id }
                            }
                            it.removeAddOrUpdate(
                                app,
                                list,
                                isFinished,
                                currentConsensus?.votingStartDate ?: 0,
                                result.remove,
                                result.update,
                                result.addToTop
                            )

                        }
                    }
                }
            )
            .addTo(compositeDisposable)

        repository.consensusManager.observableConsensuses
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onError = ::handleGeneralError,
                onNext = { it.list.find { consensus -> consensus.id == currentId }?.let(::updateDetails) }
            )
            .addTo(compositeDisposable)

        repository.profileManager.observableLoginLogoutProfile
            .with(AndroidSchedulerProvider())
            .subscribeBy(onNext = { refreshDetails() }, onError = {})
            .addTo(compositeDisposable)
    }

    fun onTitle() {
        handle(TitleAsk(currentConsensus?.title))
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

        // resets all current saved details, should be fairly impossible to get here without a deep link / wrong id
        currentConsensus = null

        title.postValue("")
        creator.postValue("")
        endTime.postValue(TimeAwareUpdate(R.string.consensus_detail_enddate_placeholder, 0, true))
        voterCount.postValue("0")
        description.postValue("")
        creationDate.postValue("")
        votingStartTime.postValue(TimeAwareUpdate(R.string.consensus_detail_votingstartdate_placeholder, 0, true))
        controlsEnabled.postValue(true)
        adminVisibility.value = View.GONE
        addMoreVisibility.postValue(View.GONE)
        bar.postValue(AppToolbar.AppToolbarState.NO_ACTION)
        unlockState.value = LockView.LockedViewState.HIDDEN
        description.postValue(app.getString(R.string.consensus_detail_no_description))
        statusColor.postValue(ContextCompat.getColor(app, R.color.colorStatusUnknown))
        statusImage.postValue(ContextCompat.getDrawable(app, R.drawable.ic_follow))
        votedColor.postValue(ContextCompat.getColor(app.applicationContext, R.color.colorAccent))
        followingColor.postValue(ContextCompat.getColor(app.applicationContext, R.color.colorAccent))
        statusBackground.value = ContextCompat.getDrawable(app, R.drawable.bg_rounded_unknown)
        followingIcon.postValue(ContextCompat.getDrawable(app, R.drawable.ic_follow))

        refreshDetails()
    }

    /**
     * Handles the logic of showing or hiding a refresher. This is a useful button which let's the user choose if he
     * wants to update the view (on a button click).
     *
     * The refresher should only be shown if necessary meaning it will only show if the consensus is not finished,
     * not locked for the user and at least the voting start date or finish date has just been passed.
     *
     * @param alreadyShowing set to true if the refresher should be aware that the current consensus is already shown
     * this is important because maybe the refresher is aleady shown for that consensus in which case we should present
     * the refresher, defaults to false
     * @param restart set to true to restart the logic, looking for when to show the button, false to just hide the
     * button, defaults to false
     */
    private fun handleRefresher(alreadyShowing: Boolean = false, restart: Boolean = false) {
        if (restart) {
            currentConsensus?.let {
                val refreshForVoteMillis = Math.max(0, it.votingStartDate - System.currentTimeMillis())
                val refreshForEndMillis = Math.max(0, it.endDate - System.currentTimeMillis())

                if (it.finished || !it.hasAccess || refreshForVoteMillis == 0L && refreshForEndMillis == 0L) {
                    return
                }

                var delay = refreshForVoteMillis
                if (refreshForVoteMillis == 0L) {
                    delay = refreshForEndMillis
                }

                hideRefresherIfNeeded(alreadyShowing)
                refresherHandler.postDelayed(
                    { refresherToggle.postValue(true) },
                    delay
                )
            }
        } else {
            hideRefresherIfNeeded(alreadyShowing)
        }
    }

    private fun hideRefresherIfNeeded(alreadyShowing: Boolean = false) {
        refresherHandler.removeCallbacksAndMessages(null)
        refresherToggle.postValue(alreadyShowing && refresherToggle.value!!)
    }

    fun refreshDetails() {
        handleRefresher()

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

    override fun onCloseRequested() {
        onBack()
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
        val alreadyShowing = currentConsensus?.id == it.id

        if (alreadyShowing) {
            val wasFollowing = currentConsensus?.following ?: false
            if (wasFollowing && !it.following) {
                messageManager.publishMessage(
                    String.format(
                        app.getString(R.string.consensus_detail_message_stop_following),
                        it.title
                    )
                )
            } else if (!wasFollowing && it.following) {
                messageManager.publishMessage(
                    String.format(
                        app.getString(R.string.consensus_detail_message_following),
                        it.title
                    )
                )
            }
        }

        currentConsensus = it

        title.postValue(it.title)
        description.postValue(it.description)
        creator.postValue(it.creator)
        creationDate.postValue(it.creationDate.toDateTime())
        endTime.postValue(TimeAwareUpdate(R.string.consensus_detail_enddate_placeholder, it.endDate))
        voterCount.postValue(it.voters.size.toString())
        votingStartTime.postValue(
            TimeAwareUpdate(
                R.string.consensus_detail_votingstartdate_placeholder,
                it.votingStartDate
            )
        )

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

        var statusIcon = ContextCompat.getDrawable(app, R.drawable.ic_public)
        var lockState = LockView.LockedViewState.HIDE

        if (!it.public) {
            if (it.hasAccess) {
                statusIcon = ContextCompat.getDrawable(app, R.drawable.ic_unlocked)
            } else {
                lockState = LockView.LockedViewState.SHOW
                statusIcon = ContextCompat.getDrawable(app, R.drawable.ic_locked)
            }
        }

        if (it.finished) {
            statusColor.value = ContextCompat.getColor(app, R.color.colorStatusFinished)
            statusBackground.value = ContextCompat.getDrawable(app, R.drawable.bg_rounded_finished)
            lockState = LockView.LockedViewState.HIDE
            statusIcon = ContextCompat.getDrawable(app, R.drawable.ic_finished)
        } else {
            statusColor.value = ContextCompat.getColor(app, R.color.colorStatusOpen)
            statusBackground.value = ContextCompat.getDrawable(app, R.drawable.bg_rounded_open)
        }

        statusImage.value = statusIcon
        unlockState.postValue(lockState)

        handleRefresher(alreadyShowing, true)
    }

    private fun onDetailsLoaded(result: RepoData<ConsensusResponse?>, fromLock: Boolean) {
        hideLoading()

        result.data?.let {
            loadSuggestions()

            if (fromLock && !it.hasAccess) {
                unlockState.postValue(LockView.LockedViewState.ERROR)
            }
        }

        if (result.data == null && fromLock) {
            unlockState.postValue(LockView.LockedViewState.ERROR)
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
     * Asks for the full title.
     */
    class TitleAsk(val title: String?)

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
     *  @param placement the placement, defaults to 0, only meaningful if consensus is finished
     */
    class SuggestionInfoAsk(val consensus: ConsensusResponse, val suggestion: SuggestionResponse, val placement: Int)

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

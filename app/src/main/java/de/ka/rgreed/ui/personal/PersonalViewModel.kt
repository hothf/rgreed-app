package de.ka.rgreed.ui.personal

import android.app.Application
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.ka.rgreed.R
import de.ka.rgreed.base.BaseViewModel
import de.ka.rgreed.base.events.AnimType
import de.ka.rgreed.repo.RepoData
import de.ka.rgreed.repo.api.models.ConsensusResponse
import de.ka.rgreed.repo.subscribeRepoCompletion
import de.ka.rgreed.ui.home.HomeViewModel
import de.ka.rgreed.ui.consensus.consensusdetail.ConsensusDetailFragment
import de.ka.rgreed.ui.consensus.consensuslist.ConsensusItemDecoration
import de.ka.rgreed.ui.consensus.consensuslist.ConsensusAdapter
import de.ka.rgreed.ui.consensus.consensuslist.ConsensusItemViewModel
import de.ka.rgreed.utils.AndroidSchedulerProvider
import de.ka.rgreed.utils.start
import de.ka.rgreed.utils.with
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

/**
 * Responsible for displaying the list of all [ConsensusResponse]s of the user. This may include all consensuses where
 * he interacted or has created data in. There is a switch to toggle finished and unfinished consensuses.
 */
class PersonalViewModel(app: Application) : BaseViewModel(app) {

    private enum class Shown { OPEN, FINISHED, ADMIN }

    private var currentlyShown = 0
    private var lastReceivedCount = 0
    private var isLoading: Boolean = false
    private var shown: Shown = Shown.OPEN

    val adapter = MutableLiveData<ConsensusAdapter>()
    val refresh = MutableLiveData<Boolean>().apply { value = false }
    val blankVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val noConsensusesText =
        MutableLiveData<String>().apply { value = app.getString(R.string.personal_consensus_no_consensus_open) }
    val itemDecoration = ConsensusItemDecoration(app.resources.getDimensionPixelSize(R.dimen.default_16))
    val openTextColor = MutableLiveData<Int>().apply {
        value = ContextCompat.getColor(app.applicationContext, R.color.fontDefaultInverted)
    }
    val openButtonBackground = MutableLiveData<Drawable>().apply {
        value = ContextCompat.getDrawable(app.applicationContext, R.drawable.rounded_button_left_selector_active)
    }
    val finishedTextColor = MutableLiveData<Int>().apply {
        value = ContextCompat.getColor(app.applicationContext, R.color.colorAccent)
    }
    val finishedButtonBackground = MutableLiveData<Drawable>().apply {
        value = ContextCompat.getDrawable(app.applicationContext, R.drawable.rounded_button_middle_selector)
    }
    val adminTextColor = MutableLiveData<Int>().apply {
        value = ContextCompat.getColor(app.applicationContext, R.color.colorAccent)
    }
    val adminButtonBackground = MutableLiveData<Drawable>().apply {
        value = ContextCompat.getDrawable(app.applicationContext, R.drawable.rounded_button_right_selector)
    }
    val swipeToRefreshListener = SwipeRefreshLayout.OnRefreshListener { loadPersonalConsensuses(true) }

    private val itemClickListener = { vm: ConsensusItemViewModel, view: View ->
        navigateTo(
            R.id.action_personalFragment_to_consensusDetailFragment,
            false,
            Bundle().apply { putString(ConsensusDetailFragment.CONS_ID_KEY, vm.item.id.toString()) },
            null,
            FragmentNavigatorExtras(view to view.transitionName)
        )
    }

    init {
        startObserving()
    }

    fun onSettingsClick() {
        navigateTo(R.id.settingsFragment, animType = AnimType.MODAL)
    }

    private fun startObserving() {
        repository.profileManager.observableLoginLogoutProfile
            .with(AndroidSchedulerProvider())
            .subscribeBy(onError = {}, onNext = { loadPersonalConsensuses(true) })
            .addTo(compositeDisposable)

        repository.consensusManager.observableAdminConsensuses
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onError = ::handleGeneralError,
                onNext = {
                    if (shown == Shown.ADMIN) {
                        if (it.invalidate) {
                            loadPersonalConsensuses(true)
                            return@subscribeBy
                        }
                        adapter.value?.insert(it.list, itemClickListener)

                        if (it.list.isEmpty()) {

                            if (shown == Shown.ADMIN) {
                                noConsensusesText.postValue(app.getString(R.string.personal_consensus_no_consensus_admin))
                            }

                            blankVisibility.postValue(View.VISIBLE)
                        } else {
                            blankVisibility.postValue(View.GONE)
                        }
                    }
                })
            .addTo(compositeDisposable)

        repository.consensusManager.observableFollowingConsensuses
            .with(AndroidSchedulerProvider())
            .subscribeBy(
                onError = ::handleGeneralError,
                onNext = {
                if (shown != Shown.ADMIN) {
                    if (it.invalidate) {
                        loadPersonalConsensuses(true)
                        return@subscribeBy
                    }
                    adapter.value?.insert(it.list, itemClickListener)
                    if (it.list.isEmpty()) {

                        if (shown == Shown.OPEN) {
                            noConsensusesText.postValue(app.getString(R.string.personal_consensus_no_consensus_open))
                        } else if (shown == Shown.FINISHED) {
                            noConsensusesText.postValue(app.getString(R.string.personal_consensus_no_consensus_finished))
                        }

                        blankVisibility.postValue(View.VISIBLE)
                    } else {
                        blankVisibility.postValue(View.GONE)
                    }
                }
            })
            .addTo(compositeDisposable)
    }

    fun layoutManager() = LinearLayoutManager(app.applicationContext)

    /**
     * Sets up the view, if not already done.
     *
     * @param owner the lifecycle owner to keep the data in sync with the lifecycle
     */
    fun setupAdapterAndLoad(owner: LifecycleOwner) {
        if (adapter.value == null) {
            adapter.postValue(ConsensusAdapter(owner))
            loadPersonalConsensuses(true)
        }
    }

    /**
     * Called on a finish toggle click, to show finished personal consensuses.
     */
    fun onFinishedClick() {
        shown = Shown.FINISHED
        openTextColor.postValue(ContextCompat.getColor(app.applicationContext, R.color.colorAccent))
        openButtonBackground.postValue(
            ContextCompat.getDrawable(
                app.applicationContext,
                R.drawable.rounded_button_left_selector
            )
        )
        finishedTextColor.postValue(ContextCompat.getColor(app.applicationContext, R.color.fontDefaultInverted))
        finishedButtonBackground.postValue(
            ContextCompat.getDrawable(
                app.applicationContext,
                R.drawable.rounded_button_middle_selector_active
            )
        )
        adminTextColor.postValue(ContextCompat.getColor(app.applicationContext, R.color.colorAccent))
        adminButtonBackground.postValue(
            ContextCompat.getDrawable(
                app.applicationContext,
                R.drawable.rounded_button_right_selector
            )
        )
        loadPersonalConsensuses(true)
    }

    /**
     * Called on a open toggle click, to show not finished personal consensuses.
     */
    fun onOpenedClick() {
        shown = Shown.OPEN
        openTextColor.postValue(ContextCompat.getColor(app.applicationContext, R.color.fontDefaultInverted))
        openButtonBackground.postValue(
            ContextCompat.getDrawable(
                app.applicationContext,
                R.drawable.rounded_button_left_selector_active
            )
        )
        finishedTextColor.postValue(ContextCompat.getColor(app.applicationContext, R.color.colorAccent))
        finishedButtonBackground.postValue(
            ContextCompat.getDrawable(
                app.applicationContext,
                R.drawable.rounded_button_middle_selector
            )
        )
        adminTextColor.postValue(ContextCompat.getColor(app.applicationContext, R.color.colorAccent))
        adminButtonBackground.postValue(
            ContextCompat.getDrawable(
                app.applicationContext,
                R.drawable.rounded_button_right_selector
            )
        )
        loadPersonalConsensuses(true)
    }

    /**
     * Called on a click on admin
     */
    fun onAdminClick() {
        shown = Shown.ADMIN
        openTextColor.postValue(ContextCompat.getColor(app.applicationContext, R.color.colorAccent))
        openButtonBackground.postValue(
            ContextCompat.getDrawable(
                app.applicationContext,
                R.drawable.rounded_button_left_selector
            )
        )
        finishedTextColor.postValue(ContextCompat.getColor(app.applicationContext, R.color.colorAccent))
        finishedButtonBackground.postValue(
            ContextCompat.getDrawable(
                app.applicationContext,
                R.drawable.rounded_button_middle_selector
            )
        )
        adminTextColor.postValue(ContextCompat.getColor(app.applicationContext, R.color.fontDefaultInverted))
        adminButtonBackground.postValue(
            ContextCompat.getDrawable(
                app.applicationContext,
                R.drawable.rounded_button_right_selector_active
            )
        )
        loadPersonalConsensuses(true)

    }

    fun itemAnimator() = SlideInUpAnimator()

    /**
     * Retrieves an on scroll listener for charging history loading.
     *
     * @return the scroll listener
     */
    fun getOnScrollListener(): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollVertically(1) && lastReceivedCount >= HomeViewModel.ITEMS_PER_LOAD) {
                    loadPersonalConsensuses(false)
                }
            }
        }
    }

    private fun loadPersonalConsensuses(reset: Boolean) {
        if (reset) {
            currentlyShown = 0
            isLoading = false
            compositeDisposable.clear()
            startObserving()
        }

        if (isLoading) {
            return
        }

        if (shown == Shown.ADMIN) {
            repository.consensusManager.getAdminConsensuses(reset, ITEMS_PER_LOAD, currentlyShown)
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion(::handleListResult)
                .start(compositeDisposable, ::showLoading)
        } else {
            repository.consensusManager.getFollowingConsensuses(
                reset,
                ITEMS_PER_LOAD,
                currentlyShown,
                shown == Shown.FINISHED
            )
                .with(AndroidSchedulerProvider())
                .subscribeRepoCompletion(::handleListResult)
                .start(compositeDisposable, ::showLoading)
        }
    }

    private fun handleListResult(result: RepoData<List<ConsensusResponse>?>) {
        refresh.postValue(false)
        isLoading = false

        result.data?.let {
            currentlyShown += it.size
            lastReceivedCount = it.size
        }
    }

    private fun showLoading() {
        isLoading = true
        refresh.postValue(true)
    }

    companion object {
        const val ITEMS_PER_LOAD = 10
    }
}
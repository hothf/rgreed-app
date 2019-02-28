package de.ka.skyfallapp.ui.personal

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.ConsensusResponse
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.ui.home.consensusdetail.ConsensusDetailFragment
import de.ka.skyfallapp.ui.personal.consensuslist.PersonalAdapter
import de.ka.skyfallapp.ui.personal.consensuslist.PersonalItemViewModel
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.start
import de.ka.skyfallapp.utils.with
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber

class PersonalViewModel(app: Application) : BaseViewModel(app) {

    val adapter = MutableLiveData<PersonalAdapter>()
    val scrollTo = MutableLiveData<Int>().apply { postValue(0) }
    val refresh = MutableLiveData<Boolean>().apply { postValue(false) }
    val blankVisibility = MutableLiveData<Int>().apply { postValue(View.GONE) }
    val swipeToRefreshListener = SwipeRefreshLayout.OnRefreshListener { loadPersonalConsensuses(true) }

    init {
        repository.profileManager.observableProfile
            .with(AndroidSchedulerProvider())
            .subscribeBy(onNext = { loadPersonalConsensuses(true) })
            .addTo(compositeDisposable)

        repository.consensusManager.observablePersonalConsensuses
            .with(AndroidSchedulerProvider())
            .subscribeBy(onNext = {
                if (it.isEmpty()) {
                    blankVisibility.postValue(View.VISIBLE)
                } else {
                    blankVisibility.postValue(View.GONE)
                }
                adapter.value?.insert(it, itemClickListener)
            })
            .addTo(compositeDisposable)
    }

    private val itemClickListener = { vm: PersonalItemViewModel, view: View ->
        navigateTo(
            R.id.action_personalFragment_to_consensusDetailFragment,
            false,
            Bundle().apply { putString(ConsensusDetailFragment.CONS_ID_KEY, vm.item.id.toString()) },
            null,
            FragmentNavigatorExtras(view to view.transitionName)
        )
    }

    fun layoutManager() = LinearLayoutManager(app.applicationContext)

    fun setupAdapterAndLoad(owner: LifecycleOwner) {
        if (adapter.value == null) {
            adapter.postValue(PersonalAdapter(owner))
            loadPersonalConsensuses(true)
        }
    }

    private fun loadPersonalConsensuses(reset: Boolean) {
        repository.consensusManager.getPersonalConsensuses(reset)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion(::handleListResult)
            .start(compositeDisposable, ::showLoading)
    }

    private fun handleListResult(result: RepoData<List<ConsensusResponse>?>) {
        refresh.postValue(false)

        if (result.data == null) {
            showSnack(result.info.throwable?.message.toString())
        }
    }

    private fun showLoading() {
        refresh.postValue(true)
    }

    companion object {
        const val PERSONAL_DATA = "PersonalViewModelData"
    }
}
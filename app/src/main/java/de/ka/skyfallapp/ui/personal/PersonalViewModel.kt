package de.ka.skyfallapp.ui.personal

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.ConsensusResponse
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.ui.home.consensus.ConsensusDetailFragment
import de.ka.skyfallapp.ui.personal.list.PersonalAdapter
import de.ka.skyfallapp.ui.personal.list.PersonalItemViewModel
import de.ka.skyfallapp.utils.AndroidSchedulerProvider
import de.ka.skyfallapp.utils.start
import de.ka.skyfallapp.utils.with

class PersonalViewModel(app: Application) : BaseViewModel(app) {

    val swipeToRefreshListener = SwipeRefreshLayout.OnRefreshListener { loadConsensus() }
    val blankVisibility = MutableLiveData<Int>().apply { postValue(View.GONE) }
    val refresh = MutableLiveData<Boolean>().apply { postValue(false) }
    val scrollTo = MutableLiveData<Int>().apply { postValue(0) }
    val adapter = MutableLiveData<PersonalAdapter>()

    private val itemClickListener = { vm: PersonalItemViewModel ->
        navigateTo(
            R.id.action_personalFragment_to_consensusDetailFragment,
            false,
            Bundle().apply { putString(ConsensusDetailFragment.CONS_ID_KEY, vm.item.id.toString()) }
        )
    }

    fun layoutManager() = LinearLayoutManager(app.applicationContext)

    fun setupAdapterAndLoad(owner: LifecycleOwner) {
        if (adapter.value == null) {
            adapter.postValue(PersonalAdapter(owner))
            loadConsensus()
        }
    }

    fun loadConsensus() {
        repository.getConsensus()
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion(::showResult)
            .start(compositeDisposable, ::showLoading)
    }

    private fun showResult(result: RepoData<List<ConsensusResponse>?>) {
        refresh.postValue(false)

        result.data?.let {

            val personalList = it.filter { consensusResponse ->
                consensusResponse.admin
            }

            if (personalList.isEmpty()) {
                blankVisibility.postValue(View.VISIBLE)
            } else {
                blankVisibility.postValue(View.GONE)
            }

            adapter.value?.insert(personalList, itemClickListener)

            scrollTo.postValue(0)

            return
        }

        if (result.info.code == 401) {
            adapter.value?.insert(listOf(), itemClickListener)

            // TODO show a go to profile

        } else {
            showSnack(result.info.throwable?.message.toString())
        }
    }

    private fun showLoading() {
        refresh.postValue(true)
    }
}
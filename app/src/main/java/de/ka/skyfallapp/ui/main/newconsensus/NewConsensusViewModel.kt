package de.ka.skyfallapp.ui.main.newconsensus

import android.app.Application
import android.os.Bundle
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.Consensus
import de.ka.skyfallapp.repo.api.ConsensusDetail
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.ui.home.HomeFragment
import de.ka.skyfallapp.ui.personal.PersonalFragment
import de.ka.skyfallapp.utils.*
import java.util.*

class NewConsensusViewModel(app: Application) : BaseViewModel(app) {


    fun onAddClicked() {

        val consensus =
            ConsensusDetail("4t324" + Random().nextInt(), "adda", Calendar.getInstance().time.time, listOf(), listOf())

        repository.sendConsensus(consensus)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion(::handleResult)
            .start(compositeDisposable, ::showLoading)
    }

    private fun handleResult(result: RepoData<ConsensusDetail?>) {

        result.data?.let {
            dirtyDataWatcher.markDirty(HomeFragment.HOME_DIRTY)
            dirtyDataWatcher.markDirty(PersonalFragment.PERSONAL_DIRTY)

            navigateTo(R.id.action_newConsensusFragment_to_personalFragment)
        }

        defaultErrorHandling(result) { unhandled ->
            unhandled.info.throwable?.let {
                showSnack(it.message.toString())
            }
        }
    }

    private fun showLoading() {

    }

}
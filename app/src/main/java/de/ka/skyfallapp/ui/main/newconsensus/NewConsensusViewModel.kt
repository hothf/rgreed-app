package de.ka.skyfallapp.ui.main.newconsensus

import android.app.Application
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.ConsensusBody
import de.ka.skyfallapp.repo.api.ConsensusResponse
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.ui.home.HomeFragment
import de.ka.skyfallapp.ui.personal.PersonalFragment
import de.ka.skyfallapp.utils.*

class NewConsensusViewModel(app: Application) : BaseViewModel(app) {

    fun onAddClicked() {

        val consensus = ConsensusBody("adadsfwfwfw")

        repository.sendConsensus(consensus)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion(::handleResult)
            .start(compositeDisposable, ::showLoading)
    }

    private fun handleResult(result: RepoData<ConsensusResponse?>) {

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
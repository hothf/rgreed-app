package de.ka.skyfallapp.ui.newconsensus

import android.app.Application
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.ConsensusBody
import de.ka.skyfallapp.repo.api.ConsensusResponse
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.ui.home.HomeFragment
import de.ka.skyfallapp.ui.home.HomeViewModel
import de.ka.skyfallapp.ui.personal.PersonalFragment
import de.ka.skyfallapp.ui.personal.PersonalViewModel
import de.ka.skyfallapp.utils.*
import kotlin.random.Random

class NewConsensusViewModel(app: Application) : BaseViewModel(app) {

    fun onAddClicked() {

        val consensus = ConsensusBody(
            "Random App Consensus",
            "Random description",
            true,
            System.currentTimeMillis() + 1_000_000_000
        )

        repository.sendConsensus(consensus)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion(::handleResult)
            .start(compositeDisposable, ::showLoading)
    }

    private fun handleResult(result: RepoData<ConsensusResponse?>) {

        result.data?.let {
            dirtyDataWatcher.markDirty(HomeViewModel.HOME_DATA)
            dirtyDataWatcher.markDirty(PersonalViewModel.PERSONAL_DATA)

            navigateTo(R.id.action_newConsensusFragment_to_personalFragment)
        }

        apiErrorHandler.handle(result) { unhandled ->
            unhandled.info.throwable?.let {
                showSnack(it.message.toString())
            }
        }
    }

    private fun showLoading() {

    }

}
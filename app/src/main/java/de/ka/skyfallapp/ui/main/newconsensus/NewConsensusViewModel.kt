package de.ka.skyfallapp.ui.main.newconsensus

import android.app.Application
import android.os.Bundle
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseViewModel
import de.ka.skyfallapp.repo.RepoData
import de.ka.skyfallapp.repo.api.Consensus
import de.ka.skyfallapp.repo.subscribeRepoCompletion
import de.ka.skyfallapp.ui.home.HomeFragment
import de.ka.skyfallapp.utils.*
import java.util.*

class NewConsensusViewModel(app: Application) : BaseViewModel(app) {


    fun onAddClicked() {

        val consensus =
            Consensus("4t324" + Random().nextInt(), "adda", Calendar.getInstance().time.time, listOf(), listOf())

        repository.sendConsensus(consensus)
            .with(AndroidSchedulerProvider())
            .subscribeRepoCompletion(::handleResult)
            .start(compositeDisposable, ::showLoading)
    }

    private fun handleResult(result: RepoData<Consensus?>) {

        result.data?.let {
            navigateTo(
                R.id.action_newConsensusFragment_to_personalFragment,
                args = Bundle().apply { putBoolean(HomeFragment.KEY_UPDATE, true) }
            )
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
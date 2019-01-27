package de.ka.skyfallapp.ui.home.consensus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseFragment
import de.ka.skyfallapp.databinding.FragmentConsensusDetailBinding
import de.ka.skyfallapp.repo.api.Consensus
import de.ka.skyfallapp.repo.api.Suggestion

class ConsensusDetailFragment :
    BaseFragment<FragmentConsensusDetailBinding, ConsensusDetailViewModel>(
        ConsensusDetailViewModel::class
    ) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val consensusId = arguments?.getString(CONS_ID_KEY)
        if (consensusId != null) {
            viewModel.setupAdapterAndLoad(viewLifecycleOwner, consensusId)
        }
        arguments?.clear()

        dirtyDataWatcher.handleDirty(CONS_DETAIL_DIRTY) {
            viewModel.refreshDetails()
        }

        return view
    }

    override var bindingLayoutId = R.layout.fragment_consensus_detail

    companion object {
        const val CONS_ID_KEY = "cons_id_key"
        const val CONS_DETAIL_DIRTY = "cons_detail_dirty"
    }

}

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

        val consensus = arguments?.getSerializable(CONSENSUS_KEY) as Consensus?

        if (consensus != null) {
            viewModel.populateConsensusDetails(viewLifecycleOwner, consensus)
        }

        val update = arguments?.getBoolean(UPDATE_KEY)

        if (update != null && update) {
            viewModel.refreshDetails()
        }

        arguments?.clear()

        return view
    }

    override var bindingLayoutId = R.layout.fragment_consensus_detail

    companion object {
        const val CONSENSUS_KEY = "consensus_key"
        const val UPDATE_KEY = "update_key"
    }

}

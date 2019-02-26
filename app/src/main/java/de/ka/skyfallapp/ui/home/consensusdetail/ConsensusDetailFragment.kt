package de.ka.skyfallapp.ui.home.consensusdetail

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.transition.TransitionInflater
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseFragment
import de.ka.skyfallapp.databinding.FragmentConsensusDetailBinding

class ConsensusDetailFragment :
    BaseFragment<FragmentConsensusDetailBinding, ConsensusDetailViewModel>(
        ConsensusDetailViewModel::class
    ) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val consensusId = arguments?.getString(CONS_ID_KEY)
        if (consensusId != null) {
            viewModel.setupAdapterAndLoad(viewLifecycleOwner, consensusId.toInt())
        }
        arguments?.clear()

        getBinding()?.main?.let { ViewCompat.setTransitionName(it, consensusId) }
        sharedElementEnterTransition =
            TransitionInflater.from(requireContext()).inflateTransition(R.transition.shared_element_cons_transition)

        return view
    }

    override fun handle(element: Any?) {
        if (element is ConsensusDetailViewModel.ConsensusDeletionAsk) {

            with(AlertDialog.Builder(requireActivity())) {
                setPositiveButton(android.R.string.ok) { _, _ ->
                    viewModel.deleteConsensus()
                }
                setNegativeButton(android.R.string.cancel) { _, _ ->
                    // do nothing
                }
                setTitle("Wirklich löschen ... TBD")

                create()
            }.show()
        }

    }


    override var bindingLayoutId = R.layout.fragment_consensus_detail

    companion object {
        const val CONS_ID_KEY = "cons_id_key"
    }

}

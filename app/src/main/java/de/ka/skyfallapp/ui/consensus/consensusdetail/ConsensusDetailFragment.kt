package de.ka.skyfallapp.ui.consensus.consensusdetail

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
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

        getBinding()?.topCard?.let { ViewCompat.setTransitionName(it, consensusId) }
        sharedElementEnterTransition =
            TransitionInflater.from(requireContext()).inflateTransition(R.transition.shared_element_cons_transition)

        return view
    }

    override fun handle(element: Any?) {

        when (element) {
            is ConsensusDetailViewModel.ConsensusDeletionAsk -> {
                askForDeletion()
            }
            is ConsensusDetailViewModel.SuggestionToolsAsk -> {
                PopupMenu(requireContext(), element.view).apply {
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.suggestion_action_edit -> {
                                true
                            }
                            R.id.suggestion_action_delete -> {
                                askForDeletion(element.id)
                                true
                            }
                            else -> {
                                false
                            }
                        }
                    }
                    inflate(R.menu.suggestion_actions)
                    show()
                }
            }

        }
    }

    private fun askForDeletion(suggestionId: Int? = null) {
        with(AlertDialog.Builder(requireActivity())) {
            setPositiveButton(android.R.string.ok) { _, _ ->
                if (suggestionId == null) {
                    viewModel.deleteConsensus()
                } else {
                    viewModel.deleteSuggestion(suggestionId)
                }
            }
            setNegativeButton(android.R.string.cancel) { _, _ ->
                // do nothing
            }
            setTitle("Wirklich löschen ... TBD") //TODO set values

            create()
        }.show()
    }


    override var bindingLayoutId = R.layout.fragment_consensus_detail

    companion object {
        const val CONS_ID_KEY = "cons_id_key"
    }

}

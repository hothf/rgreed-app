package de.ka.rgreed.ui.consensus.consensusdetail

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.transition.TransitionInflater
import de.ka.rgreed.R
import de.ka.rgreed.base.BaseFragment
import de.ka.rgreed.base.events.NavigateTo
import de.ka.rgreed.databinding.FragmentConsensusDetailBinding
import de.ka.rgreed.repo.api.models.SuggestionResponse
import de.ka.rgreed.ui.consensus.consensusdetail.neweditsuggestion.NewEditSuggestionFragment
import de.ka.rgreed.ui.consensus.consensusdetail.suggestionlist.vote.SuggestionVoteFragment
import de.ka.rgreed.ui.consensus.consensusdetail.suggestionlist.vote.Voteable
import de.ka.rgreed.ui.neweditconsensus.NewEditConsensusFragment
import android.widget.ArrayAdapter
import de.ka.rgreed.base.events.AnimType
import de.ka.rgreed.repo.api.models.ConsensusResponse
import de.ka.rgreed.ui.consensus.consensusdetail.suggestionlist.SuggestionInfoFragment
import de.ka.rgreed.utils.ShareUtils


/**
 * The detail fragment of a consensus. Pass a consensus id as argument to get started.
 *
 * To open different dialogs and pickers, this fragment handles custom opening and ask events.
 */
class ConsensusDetailFragment :
    Voteable,
    BaseFragment<FragmentConsensusDetailBinding, ConsensusDetailViewModel>(
        ConsensusDetailViewModel::class
    ) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val consensusId = arguments?.getString(CONS_ID_KEY)
        if (consensusId != null) {
            viewModel.setupAdapterAndLoad(viewLifecycleOwner, consensusId.toInt())
        }

        animate(listOf(getBinding()?.favButton, getBinding()?.voters, getBinding()?.addSuggButton))

        getBinding()?.topCard?.let { ViewCompat.setTransitionName(it, consensusId) }
        sharedElementEnterTransition =
            TransitionInflater.from(requireContext()).inflateTransition(R.transition.shared_element_cons_transition)

        return view
    }

    private fun animate(views: List<View?>) {
        views.forEach { view ->
            view?.apply {
                scaleX = 0.0f
                scaleY = 0.0f
                animate().scaleX(1.0f).scaleY(1.0f)
            }
        }
    }

    override fun handle(element: Any?) {
        when (element) {
            is ConsensusDetailViewModel.ConsensusToolsAsk -> {
                PopupMenu(requireContext(), element.view).apply {
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.consensus_action_edit -> {
                                navigateToConsensusEdit(element.data)
                                true
                            }
                            R.id.consensus_action_delete -> {
                                askForDeletion()
                                true
                            }
                            R.id.consensus_action_share -> {
                                ShareUtils.showConsensusShare(requireActivity(), element.data?.id.toString())
                                true
                            }
                            else -> {
                                false
                            }
                        }
                    }
                    inflate(R.menu.consensus_actions)

                    menu.findItem(R.id.consensus_action_edit).isVisible =
                        if (element.data != null) element.data.admin && !element.data.finished else false
                    menu.findItem(R.id.consensus_action_delete).isVisible = element.data?.admin ?: false

                    show()
                }
            }
            is ConsensusDetailViewModel.SuggestionToolsAsk -> {
                PopupMenu(requireContext(), element.view).apply {
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.suggestion_action_edit -> {
                                navigateToSuggestionEdit(element.data)
                                true
                            }
                            R.id.suggestion_action_delete -> {
                                askForDeletion(element.data.id)
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
            is ConsensusDetailViewModel.SuggestionInfoAsk -> {
                SuggestionInfoFragment.newInstance(element.consensus, element.suggestion, this@ConsensusDetailFragment)
                    .show(fragmentManager, "info")
            }
            is ConsensusDetailViewModel.SuggestionVoteAsk -> {
                SuggestionVoteFragment.newInstance(element.suggestion, this@ConsensusDetailFragment)
                    .show(fragmentManager, "vote")
            }
            is ConsensusDetailViewModel.VoterDialogAsk -> {
                askForVoters(element.voters)
            }
            is ConsensusDetailViewModel.TitleAsk -> {
                if (element.title == null) {
                    return
                }
                with(AlertDialog.Builder(requireActivity())) {
                    setPositiveButton(android.R.string.ok) { _, _ -> /* do nothing */ }
                    setTitle(getString(R.string.consensus_detail_title))
                    setMessage(element.title)
                    create()
                }.show()
            }

        }
    }

    override fun onVoteSet(suggestion: SuggestionResponse, amount: Float) {
        viewModel.voteOnSuggestion(suggestion, amount)
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
            setNegativeButton(android.R.string.cancel) { _, _ -> /* do nothing */ }
            setTitle(getString(R.string.consensus_detail_delete_title))

            create()
        }.show()
    }

    private fun askForVoters(voters: List<String>) {
        with(AlertDialog.Builder(requireActivity())) {
            setPositiveButton(android.R.string.ok) { _, _ -> /* do nothing */ }
            setTitle(getString(R.string.consensus_detail_voters))

            val arrayAdapter =
                ArrayAdapter<String>(requireActivity(), R.layout.item_simple_list).apply {
                    addAll(voters)
                }

            setAdapter(arrayAdapter) { _, _ -> /* do nothing */ }
            create()
        }.show()
    }

    private fun navigateToConsensusEdit(consensusResponse: ConsensusResponse?) {
        navigateTo(
            NavigateTo(
                R.id.action_consensusDetailFragment_to_newConsensusFragment,
                false,
                Bundle().apply { putSerializable(NewEditConsensusFragment.CONSENSUS_KEY, consensusResponse) },
                animType = AnimType.MODAL
            )
        )
    }

    private fun navigateToSuggestionEdit(suggestionResponse: SuggestionResponse) {
        navigateTo(
            NavigateTo(
                R.id.action_consensusDetailFragment_to_newSuggestionFragment,
                false,
                Bundle().apply { putSerializable(NewEditSuggestionFragment.SUGGESTION_KEY, suggestionResponse) },
                animType = AnimType.MODAL
            )
        )
    }

    override var bindingLayoutId = R.layout.fragment_consensus_detail

    companion object {
        const val CONS_ID_KEY = "cons_id_key"
    }

}

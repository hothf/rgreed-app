package de.ka.rgreed.ui.consensus.consensusdetail.suggestionlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import de.ka.rgreed.R
import de.ka.rgreed.repo.api.models.SuggestionResponse

import de.ka.rgreed.repo.api.models.ConsensusResponse

/**
 * A suggestions info fragment. This should not contain states as
 * this has no view model.
 */
class SuggestionInfoFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.layout_suggestion_info, container)

        val suggestion = arguments?.getSerializable(SUGGESTION_KEY) as? SuggestionResponse
        val consensus = arguments?.getSerializable(CONS_KEY) as? ConsensusResponse
        val placement = arguments?.getInt(PLACEMENT_KEY, 0) ?: 2

        val winnerContainer = view.findViewById<LinearLayout>(R.id.winnerContainer)
        val objectionsContainer = view.findViewById<LinearLayout>(R.id.objectionsContainer)
        val acceptanceContainer = view.findViewById<LinearLayout>(R.id.acceptanceContainer)

        if (suggestion != null && consensus != null) {
            if (consensus.finished) {
                if (placement < 2 && suggestion.overallAcceptance != null) {
                    winnerContainer.visibility = View.VISIBLE
                } else {
                    winnerContainer.visibility = View.GONE
                }

                view.findViewById<TextView>(R.id.infoText).text = String.format(
                    getString(R.string.consensus_detail_cannot_vote_finished), suggestion.title
                )

                if (suggestion.heavyObjectionsCount != null && suggestion.heavyObjectionsCount > 0) {
                    objectionsContainer.visibility = View.VISIBLE
                    view.findViewById<TextView>(R.id.highObjections).apply {
                        text = String.format(
                            getString(R.string.suggestions_info_objectionscount),
                            suggestion.heavyObjectionsCount
                        )
                    }
                } else {
                    objectionsContainer.visibility = View.GONE
                }

                view.findViewById<TextView>(R.id.acceptance).apply {
                    text = if (suggestion.overallAcceptance == null) {
                        getString(R.string.suggestions_info_not_voted)
                    } else {
                        String.format(
                            getString(R.string.suggestions_info_acceptance),
                            suggestion.overallAcceptance
                        )
                    }
                }

                acceptanceContainer.visibility = View.VISIBLE
            } else {
                winnerContainer.visibility = View.GONE
                objectionsContainer.visibility = View.GONE
                acceptanceContainer.visibility = View.GONE
                view.findViewById<TextView>(R.id.infoText).text =
                    String.format(getString(R.string.consensus_detail_cannot_vote), suggestion.title)
            }
        }

        view.findViewById<Button>(R.id.closeButton).setOnClickListener {
            dismiss()
        }

        return view
    }

    companion object {
        const val SUGGESTION_KEY = "sugi_key"
        const val PLACEMENT_KEY = "place_key"
        const val CONS_KEY = "cons_key"

        fun newInstance(
            consensus: ConsensusResponse,
            suggestion: SuggestionResponse,
            placement: Int,
            targetFragment: Fragment
        ) =
            SuggestionInfoFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(SUGGESTION_KEY, suggestion)
                    putSerializable(CONS_KEY, consensus)
                    putInt(PLACEMENT_KEY, placement)
                }
                setTargetFragment(targetFragment, 0)
            }
    }
}
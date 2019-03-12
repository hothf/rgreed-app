package de.ka.skyfallapp.ui.consensus.consensusdetail.suggestionlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import de.ka.skyfallapp.R
import de.ka.skyfallapp.repo.api.models.SuggestionResponse

class SuggestionVoteFragment<T : Voteable> : BottomSheetDialogFragment() {

    private var voteable: T? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.layout_suggestion_vote, container)

        val suggestion = arguments?.getSerializable(SUGGESTION_KEY) as? SuggestionResponse

        voteable = targetFragment as? T

        if (suggestion != null) {

            //TODO better visuals, better picker ;)

            view.findViewById<Button>(R.id.voteButton).setOnClickListener {
                voteable?.onVoteSet(suggestion, 10.0f)
                dismiss()
            }
        }

        return view
    }


    companion object {
        const val SUGGESTION_KEY = "sugg_key"

        fun <T : Voteable> newInstance(suggestion: SuggestionResponse, targetFragment: T) =
            SuggestionVoteFragment<T>().apply {
                arguments = Bundle().apply { putSerializable(SUGGESTION_KEY, suggestion) }
                setTargetFragment(targetFragment as Fragment, 0)
            }
    }
}

/**
 * Listens for voting events.
 */
interface Voteable {

    /**
     * Called on setting a vote.
     */
    fun onVoteSet(suggestion: SuggestionResponse, amount: Float)
}
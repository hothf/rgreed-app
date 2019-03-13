package de.ka.skyfallapp.ui.consensus.consensusdetail.suggestionlist.vote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import de.ka.skyfallapp.R
import de.ka.skyfallapp.repo.api.models.SuggestionResponse


import de.ka.skyfallapp.utils.HorizontalPickerManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

/**
 * A suggestions vote fragment, mainly a bottom sheet with a horizontal picker. This should not contain states as
 * this has no view model.
 */
class SuggestionVoteFragment<T : Voteable> : BottomSheetDialogFragment() {

    private var voteable: T? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.layout_suggestion_vote, container)

        val suggestion = arguments?.getSerializable(SUGGESTION_KEY) as? SuggestionResponse

        voteable = targetFragment as? T

        val recyclerView: RecyclerView = view.findViewById(R.id.voteRecycler)

        if (suggestion != null) {

            var currentVoting: Float? = suggestion.ownAcceptance

            val adapter = SuggestionVoteAdapter { position ->
                Timber.e("Weird $position")
                recyclerView.smoothScrollToPosition(position)
            }
            recyclerView.layoutManager =
                HorizontalPickerManager(requireContext(), LinearLayoutManager.HORIZONTAL, false).apply {
                    itemSizeResId = R.dimen.default_90
                    isChangeAlpha = true
                    scaleDownBy = 0.5f
                    scaleDownDistance = 0.4f
                    onSelected = { selected ->
                        currentVoting = adapter.getDataAt(selected)?.first?.toFloat() ?: currentVoting
                    }
                }
            LinearSnapHelper().attachToRecyclerView(recyclerView)
            recyclerView.adapter = adapter

            val startValue = suggestion.ownAcceptance ?: 5.0f

            adapter.getPositionForValue(startValue)?.let {
                recyclerView.smoothScrollToPosition(it)
            }

            // actual voting
            view.findViewById<Button>(R.id.voteButton).setOnClickListener {
                currentVoting?.let {
                    voteable?.onVoteSet(suggestion, it)
                }
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
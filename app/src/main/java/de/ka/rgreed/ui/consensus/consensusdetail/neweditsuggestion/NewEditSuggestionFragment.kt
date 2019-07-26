package de.ka.rgreed.ui.consensus.consensusdetail.neweditsuggestion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.ka.rgreed.R
import de.ka.rgreed.base.BaseFragment
import de.ka.rgreed.databinding.FragmentNeweditsuggestionBinding
import de.ka.rgreed.repo.api.models.SuggestionResponse
import de.ka.rgreed.utils.*

/**
 * This fragment aims at creating or editing a suggestion. Different behaviours can be triggered by delivering
 * different arguments.
 */
class NewEditSuggestionFragment :
    BaseFragment<FragmentNeweditsuggestionBinding, NewEditSuggestionViewModel>(
        NewEditSuggestionViewModel::class
    ) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val consensusId = arguments?.getString(CONS_ID_KEY)
        if (consensusId != null) {
            viewModel.setupNew(consensusId.toInt())
        } else {
            val suggestion = arguments?.getSerializable(SUGGESTION_KEY) as? SuggestionResponse
            suggestion?.let {
                viewModel.setupEdit(it)
            }
        }
        getBinding()?.suggInput?.apply {
            requestFocus()
            showAttachedKeyboard()
        }

        return view
    }

    override var bindingLayoutId = R.layout.fragment_neweditsuggestion

    companion object {
        const val CONS_ID_KEY = "cons_id_key"
        const val SUGGESTION_KEY = "suggestion_key"
    }
}
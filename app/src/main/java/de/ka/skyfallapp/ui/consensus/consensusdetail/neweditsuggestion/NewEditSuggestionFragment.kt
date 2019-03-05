package de.ka.skyfallapp.ui.consensus.consensusdetail.neweditsuggestion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseFragment
import de.ka.skyfallapp.databinding.FragmentNewsuggestionBinding
import de.ka.skyfallapp.repo.api.SuggestionResponse

class NewEditSuggestionFragment :
    BaseFragment<FragmentNewsuggestionBinding, NewEditSuggestionViewModel>(
        NewEditSuggestionViewModel::class
    ) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val consensusId = arguments?.getInt(CONS_ID_KEY)
        consensusId?.let {
            viewModel.setupNew(it)
        }

        val suggestion = arguments?.getSerializable(SUGGESTION_KEY) as? SuggestionResponse
        suggestion?.let {
            viewModel.setupEdit(it)
        }

        return view
    }

    override var bindingLayoutId = R.layout.fragment_newsuggestion

    companion object {
        const val CONS_ID_KEY = "cons_id_key"
        const val SUGGESTION_KEY = "suggestion_key"
    }
}
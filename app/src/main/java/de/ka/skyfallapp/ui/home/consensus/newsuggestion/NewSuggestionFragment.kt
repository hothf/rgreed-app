package de.ka.skyfallapp.ui.home.consensus.newsuggestion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseFragment
import de.ka.skyfallapp.databinding.FragmentNewsuggestionBinding

class NewSuggestionFragment :
    BaseFragment<FragmentNewsuggestionBinding, NewSuggestionViewModel>(
        NewSuggestionViewModel::class
    ) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val consensusId = arguments?.getInt(CONS_ID_KEY)
        if (consensusId != null) {
            viewModel.setup(consensusId)
        }
        arguments?.clear()

        return view
    }

    override var bindingLayoutId = R.layout.fragment_newsuggestion

    companion object {
        const val CONS_ID_KEY = "cons_id_key"
    }
}
package de.ka.skyfallapp.ui.home.consensus.newsuggestion

import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseFragment
import de.ka.skyfallapp.databinding.FragmentNewsuggestionBinding

class NewSuggestionFragment :
    BaseFragment<FragmentNewsuggestionBinding, NewSuggestionViewModel>(
        NewSuggestionViewModel::class
    ) {

    override var bindingLayoutId = R.layout.fragment_newsuggestion
}
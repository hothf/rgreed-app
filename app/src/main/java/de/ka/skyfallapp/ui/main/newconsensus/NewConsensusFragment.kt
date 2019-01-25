package de.ka.skyfallapp.ui.main.newconsensus

import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseFragment
import de.ka.skyfallapp.databinding.FragmentNewconsensusBinding
import de.ka.skyfallapp.databinding.FragmentNewsuggestionBinding

class NewConsensusFragment :
    BaseFragment<FragmentNewconsensusBinding, NewConsensusViewModel>(
        NewConsensusViewModel::class
    ) {

    override var bindingLayoutId = R.layout.fragment_newconsensus
}
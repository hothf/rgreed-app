package de.ka.skyfallapp.ui.newconsensus

import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseFragment
import de.ka.skyfallapp.databinding.FragmentNewconsensusBinding

class NewConsensusFragment :
    BaseFragment<FragmentNewconsensusBinding, NewConsensusViewModel>(
        NewConsensusViewModel::class
    ) {

    override var bindingLayoutId = R.layout.fragment_newconsensus
}
package de.ka.skyfallapp.ui.personal

import android.os.Bundle
import android.view.View
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseFragment
import de.ka.skyfallapp.databinding.FragmentPersonalBinding

/**
 * The personal fragment displays a list to discover all consensuses of the user - either ones where he interacts or
 * he has created date in.
 */
class PersonalFragment : BaseFragment<FragmentPersonalBinding, PersonalViewModel>(PersonalViewModel::class) {

    override var bindingLayoutId = R.layout.fragment_personal

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.setupAdapterAndLoad(viewLifecycleOwner)

        return super.onViewCreated(view, savedInstanceState)
    }
}

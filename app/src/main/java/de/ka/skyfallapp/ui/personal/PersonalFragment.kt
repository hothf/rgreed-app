package de.ka.skyfallapp.ui.personal

import android.os.Bundle
import android.view.View
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseFragment
import de.ka.skyfallapp.databinding.FragmentPersonalBinding

class PersonalFragment : BaseFragment<FragmentPersonalBinding, PersonalViewModel>(PersonalViewModel::class) {

    override var bindingLayoutId = R.layout.fragment_personal

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.setupAdapterAndLoad(viewLifecycleOwner)

        val update = arguments?.getBoolean(PersonalFragment.KEY_UPDATE)
        if (update != null && update) {
            viewModel.loadConsensus()
        }
        arguments?.clear()

        return super.onViewCreated(view, savedInstanceState)
    }


    companion object {
        const val KEY_UPDATE = "update_key"
    }
}

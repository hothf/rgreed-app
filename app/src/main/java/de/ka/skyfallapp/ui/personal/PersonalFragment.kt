package de.ka.skyfallapp.ui.personal

import android.os.Bundle
import android.view.View
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseFragment
import de.ka.skyfallapp.databinding.FragmentPersonalBinding
import de.ka.skyfallapp.utils.ShareUtils

class PersonalFragment : BaseFragment<FragmentPersonalBinding, PersonalViewModel>(PersonalViewModel::class) {

    override var bindingLayoutId = R.layout.fragment_personal

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.setupAdapterAndLoad(viewLifecycleOwner)
        
        return super.onViewCreated(view, savedInstanceState)
    }

    override fun handle(element: Any?) {
        if (element is PersonalViewModel.SharePersonalConsensus){
            ShareUtils.showConsensusShare(requireActivity(), element.id)
        }
    }
}

package de.ka.skyfallapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseFragment
import de.ka.skyfallapp.databinding.FragmentHomeBinding
import de.ka.skyfallapp.utils.ShareUtils

/**
 * The home fragment displays a list to discover all consensuses this app has to offer.
 */
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>(HomeViewModel::class) {

    override var bindingLayoutId = R.layout.fragment_home

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel.setupAdapterAndLoad(viewLifecycleOwner)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun handle(element: Any?) {
        if (element is HomeViewModel.ShareConsensus) {
            ShareUtils.showConsensusShare(requireActivity(), element.id)
        }
    }
}

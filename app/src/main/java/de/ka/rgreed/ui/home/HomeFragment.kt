package de.ka.rgreed.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.ka.rgreed.R
import de.ka.rgreed.base.BaseFragment
import de.ka.rgreed.databinding.FragmentHomeBinding

/**
 * The home fragment displays a list to discover all consensuses this app has to offer.
 */
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>(HomeViewModel::class) {

    override var bindingLayoutId = R.layout.fragment_home

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel.setupAdapterAndLoad(viewLifecycleOwner)

        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

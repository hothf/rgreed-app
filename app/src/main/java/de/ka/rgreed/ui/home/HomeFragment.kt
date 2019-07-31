package de.ka.rgreed.ui.home

import de.ka.rgreed.R
import de.ka.rgreed.base.BaseFragment
import de.ka.rgreed.databinding.FragmentHomeBinding


/**
 * The home fragment displays a list to discover all consensuses this app has to offer.
 */
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>(HomeViewModel::class) {

    override var bindingLayoutId = R.layout.fragment_home
}

package de.ka.skyfallapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseFragment
import de.ka.skyfallapp.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>(HomeViewModel::class) {

    override var bindingLayoutId = R.layout.fragment_home

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        viewModel.setupAdapterAndLoad(viewLifecycleOwner)

        dirtyDataWatcher.handleDirty(HOME_DIRTY) {
            viewModel.loadConsensus()
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    companion object {
        const val HOME_DIRTY = "home_dirty"
    }
}

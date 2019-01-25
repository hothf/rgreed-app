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

        dirtyDataWatcher.handleDirty(PERSONAL_DIRTY) {
            viewModel.loadConsensus()
        }

        return super.onViewCreated(view, savedInstanceState)
    }


    companion object {
        const val PERSONAL_DIRTY = "personal_dirty"
    }
}

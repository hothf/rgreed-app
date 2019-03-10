package de.ka.skyfallapp.ui.profile

import android.os.Bundle
import android.view.View
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseFragment

import de.ka.skyfallapp.databinding.FragmentProfileBinding

class ProfileFragment : BaseFragment<FragmentProfileBinding, ProfileViewModel>(ProfileViewModel::class) {

    override var bindingLayoutId = R.layout.fragment_profile

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val root = super.onViewCreated(view, savedInstanceState)

        val new = arguments?.getBoolean(NEW_KEY, false) ?: false
        if (new) {
            viewModel.setupNew()
        }
        arguments?.clear()

        return root
    }

    companion object {
        const val NEW_KEY = "new_key"
    }


}

package de.ka.skyfallapp.ui.profile

import android.os.Bundle
import android.view.View
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseFragment

import de.ka.skyfallapp.databinding.FragmentProfileBinding

/**
 * The profile fragment allows users to log out, if logged in and to login, if logged out. Also offers access to the
 * route to the registration.
 */
class ProfileFragment : BaseFragment<FragmentProfileBinding, ProfileViewModel>(ProfileViewModel::class) {

    override var bindingLayoutId = R.layout.fragment_profile

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val root = super.onViewCreated(view, savedInstanceState)

        val new = arguments?.getBoolean(NEW_KEY) ?: false
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

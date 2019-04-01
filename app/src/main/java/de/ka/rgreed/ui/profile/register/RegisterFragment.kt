package de.ka.rgreed.ui.profile.register

import android.os.Bundle
import android.view.View
import de.ka.rgreed.R
import de.ka.rgreed.base.BaseFragment

import de.ka.rgreed.databinding.FragmentRegisterBinding

/**
 * The register fragment offers edit text fields to register a new user.
 */
class RegisterFragment : BaseFragment<FragmentRegisterBinding, RegisterViewModel>(RegisterViewModel::class) {

    override var bindingLayoutId = R.layout.fragment_register

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
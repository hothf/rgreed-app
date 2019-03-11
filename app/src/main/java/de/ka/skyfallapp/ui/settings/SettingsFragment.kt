package de.ka.skyfallapp.ui.settings

import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseFragment
import de.ka.skyfallapp.databinding.FragmentSettingsBinding

/**
 * A settings fragment for app specific settings adjustment.
 */
class SettingsFragment : BaseFragment<FragmentSettingsBinding, SettingsViewModel>(SettingsViewModel::class) {

    override var bindingLayoutId = R.layout.fragment_settings

}



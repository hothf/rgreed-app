package de.ka.rgreed.ui.settings

import de.ka.rgreed.R
import de.ka.rgreed.base.BaseFragment
import de.ka.rgreed.databinding.FragmentSettingsBinding

/**
 * A settings fragment for app specific settings adjustment.
 */
class SettingsFragment : BaseFragment<FragmentSettingsBinding, SettingsViewModel>(SettingsViewModel::class) {

    override var bindingLayoutId = R.layout.fragment_settings

}



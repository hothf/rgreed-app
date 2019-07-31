package de.ka.rgreed.ui.personal

import android.os.Bundle
import android.view.View
import de.ka.rgreed.R
import de.ka.rgreed.base.BaseFragment
import de.ka.rgreed.databinding.FragmentPersonalBinding

/**
 * The personal fragment displays a list to discover all consensuses of the user - either ones where he interacts or
 * he has created date in.
 */
class PersonalFragment : BaseFragment<FragmentPersonalBinding, PersonalViewModel>(PersonalViewModel::class) {

    override var bindingLayoutId = R.layout.fragment_personal

}

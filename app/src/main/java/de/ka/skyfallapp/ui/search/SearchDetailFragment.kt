package de.ka.skyfallapp.ui.search

import android.os.Bundle
import android.view.View
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseFragment
import de.ka.skyfallapp.databinding.FragmentSearchDetailBinding
import de.ka.skyfallapp.ui.personal.PersonalViewModel
import de.ka.skyfallapp.utils.ShareUtils
import de.ka.skyfallapp.utils.showAttachedKeyboard

/**
 * A search fragment for searching for detailed content.
 */
class SearchDetailFragment :
    BaseFragment<FragmentSearchDetailBinding, SearchDetailViewModel>(SearchDetailViewModel::class) {

    override var bindingLayoutId = R.layout.fragment_search_detail

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.setup(viewLifecycleOwner)

        getBinding()?.searchField?.apply {
            requestFocus()
            showAttachedKeyboard()
        }

        return super.onViewCreated(view, savedInstanceState)
    }

    override fun handle(element: Any?) {
        if (element is PersonalViewModel.SharePersonalConsensus) {
            ShareUtils.showConsensusShare(requireActivity(), element.id)
        }
    }

}



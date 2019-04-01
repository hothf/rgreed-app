package de.ka.rgreed.ui.search

import android.os.Bundle
import android.view.View
import de.ka.rgreed.R
import de.ka.rgreed.base.BaseFragment
import de.ka.rgreed.databinding.FragmentSearchBinding

/**
 * A search fragment for searching for content.
 */
class SearchFragment : BaseFragment<FragmentSearchBinding, SearchViewModel>(SearchViewModel::class) {

    override var bindingLayoutId = R.layout.fragment_search

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.setup(viewLifecycleOwner)

        return super.onViewCreated(view, savedInstanceState)
    }
}



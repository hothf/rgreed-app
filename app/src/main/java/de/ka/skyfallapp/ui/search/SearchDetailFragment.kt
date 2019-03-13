package de.ka.skyfallapp.ui.search

import android.os.Bundle
import android.view.View
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseFragment
import de.ka.skyfallapp.databinding.FragmentSearchDetailBinding

/**
 * A search fragment for searching for detailed content.
 */
class SearchDetailFragment :
    BaseFragment<FragmentSearchDetailBinding, SearchDetailViewModel>(SearchDetailViewModel::class) {

    override var bindingLayoutId = R.layout.fragment_search_detail

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.setup(viewLifecycleOwner)

        return super.onViewCreated(view, savedInstanceState)
    }

}



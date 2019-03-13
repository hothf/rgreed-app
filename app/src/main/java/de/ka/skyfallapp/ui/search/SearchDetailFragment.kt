package de.ka.skyfallapp.ui.search

import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseFragment
import de.ka.skyfallapp.databinding.FragmentSearchDetailBinding

/**
 * A search fragment for searching for detailed content.
 */
class SearchDetailFragment : BaseFragment<FragmentSearchDetailBinding, SearchDetailViewModel>(SearchDetailViewModel::class) {

    override var bindingLayoutId = R.layout.fragment_search_detail

}



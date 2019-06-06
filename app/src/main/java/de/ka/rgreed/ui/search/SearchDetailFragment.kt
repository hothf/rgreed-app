package de.ka.rgreed.ui.search

import android.os.Bundle
import android.view.View
import de.ka.rgreed.R
import de.ka.rgreed.base.BaseFragment
import de.ka.rgreed.databinding.FragmentSearchDetailBinding
import de.ka.rgreed.utils.showAttachedKeyboard

/**
 * A search fragment for searching for detailed content.
 */
class SearchDetailFragment :
    BaseFragment<FragmentSearchDetailBinding, SearchDetailViewModel>(SearchDetailViewModel::class) {

    override var bindingLayoutId = R.layout.fragment_search_detail

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val search = arguments?.getString(KEY_SEARCH)
        val new = arguments?.getBoolean(KEY_NEW)
        arguments?.clear()

        if (new != null && new) {
            getBinding()?.searchField?.apply {
                requestFocus()
                showAttachedKeyboard()
            }
        }

        viewModel.setup(viewLifecycleOwner, search, new)

        return super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        const val KEY_SEARCH = "search_key"
        const val KEY_NEW = "key_new"
    }
}
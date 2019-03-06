package de.ka.skyfallapp.ui.consensus.consensusdetail.neweditsuggestion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseFragment
import de.ka.skyfallapp.databinding.FragmentNeweditsuggestionBinding
import de.ka.skyfallapp.repo.api.SuggestionResponse
import de.ka.skyfallapp.utils.NewEditSuggestionsDatePicker
import de.ka.skyfallapp.utils.NewEditSuggestionsTimePicker


class NewEditSuggestionFragment :
    BaseFragment<FragmentNeweditsuggestionBinding, NewEditSuggestionViewModel>(
        NewEditSuggestionViewModel::class
    ) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val consensusId = arguments?.getInt(CONS_ID_KEY)
        consensusId?.let {
            viewModel.setupNew(it)
        }

        val suggestion = arguments?.getSerializable(SUGGESTION_KEY) as? SuggestionResponse
        suggestion?.let {
            viewModel.setupEdit(it)
        }

        return view
    }

    fun onTimeSet(hourOfDay: Int, minute: Int) {
        viewModel.updateVoteStartTime(hourOfDay, minute)
    }

    fun onDateSet(year: Int, month: Int, day: Int) {
        viewModel.updateVoteStartDate(year, month, day)
    }

    override fun handle(element: Any?) {
        if (element is NewEditSuggestionViewModel.OpenPickerEvent) {
            if (element.date) {
                NewEditSuggestionsDatePicker().apply {
                    arguments = Bundle().apply { putLong(NewEditSuggestionsDatePicker.DATE, element.data) }
                    setTargetFragment(this@NewEditSuggestionFragment, 0)
                }.show(fragmentManager, "nesfDialog")
            } else {
                NewEditSuggestionsTimePicker().apply {
                    arguments = Bundle().apply { putLong(NewEditSuggestionsTimePicker.TIME, element.data) }
                    setTargetFragment(this@NewEditSuggestionFragment, 0)
                }.show(fragmentManager, "nestDialog")
            }
        }
    }

    override var bindingLayoutId = R.layout.fragment_neweditsuggestion

    companion object {
        const val CONS_ID_KEY = "cons_id_key"
        const val SUGGESTION_KEY = "suggestion_key"
    }
}
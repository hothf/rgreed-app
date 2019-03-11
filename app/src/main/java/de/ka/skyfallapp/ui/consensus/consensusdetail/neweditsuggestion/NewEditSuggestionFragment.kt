package de.ka.skyfallapp.ui.consensus.consensusdetail.neweditsuggestion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseFragment
import de.ka.skyfallapp.databinding.FragmentNeweditsuggestionBinding
import de.ka.skyfallapp.repo.api.SuggestionResponse
import de.ka.skyfallapp.utils.DatePickeable
import de.ka.skyfallapp.utils.DatePicker
import de.ka.skyfallapp.utils.TimePickeable
import de.ka.skyfallapp.utils.TimePicker

/**
 * This fragment aims at creating or editing a suggestion. Different behaviours can be triggered by delivering
 * different arguments.
 */
class NewEditSuggestionFragment : TimePickeable, DatePickeable,
    BaseFragment<FragmentNeweditsuggestionBinding, NewEditSuggestionViewModel>(
        NewEditSuggestionViewModel::class
    ) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val consensusId = arguments?.getString(CONS_ID_KEY)
        if (consensusId != null) {
            viewModel.setupNew(consensusId.toInt())
        } else {
            val suggestion = arguments?.getSerializable(SUGGESTION_KEY) as? SuggestionResponse
            suggestion?.let {
                viewModel.setupEdit(it)
            }
        }
        arguments?.clear()

        return view
    }

    override fun onTimeSet(hourOfDay: Int, minute: Int) {
        viewModel.updateVoteStartTime(hourOfDay, minute)
    }

    override fun onDateSet(year: Int, month: Int, day: Int) {
        viewModel.updateVoteStartDate(year, month, day)
    }

    override fun handle(element: Any?) {
        if (element is NewEditSuggestionViewModel.OpenPickerEvent) {
            if (element.date) {
                DatePicker
                    .newInstance(element.data, this@NewEditSuggestionFragment)
                    .show(fragmentManager, "nesdDlg")
            } else {
                TimePicker
                    .newInstance(element.data, this@NewEditSuggestionFragment)
                    .show(fragmentManager, "nestDlg")
            }
        }
    }

    override var bindingLayoutId = R.layout.fragment_neweditsuggestion

    companion object {
        const val CONS_ID_KEY = "cons_id_key"
        const val SUGGESTION_KEY = "suggestion_key"
    }
}
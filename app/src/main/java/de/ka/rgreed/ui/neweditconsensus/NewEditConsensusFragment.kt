package de.ka.rgreed.ui.neweditconsensus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.ka.rgreed.R
import de.ka.rgreed.base.BaseFragment
import de.ka.rgreed.databinding.FragmentNeweditconsensusBinding
import de.ka.rgreed.repo.api.models.ConsensusResponse
import de.ka.rgreed.utils.DatePickeable
import de.ka.rgreed.utils.DatePicker
import de.ka.rgreed.utils.TimePickeable
import de.ka.rgreed.utils.TimePicker

/**
 * Offers the edition or creation of a new suggestion, depending on the supplied arguments of this fragment.
 */
class NewEditConsensusFragment : TimePickeable, DatePickeable,
    BaseFragment<FragmentNeweditconsensusBinding, NewEditConsensusViewModel>(
        NewEditConsensusViewModel::class
    ) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val consensus = arguments?.getSerializable(CONSENSUS_KEY) as? ConsensusResponse
        if (consensus != null) {
            viewModel.setupEdit(consensus)
        } else {
            val new = arguments?.getBoolean(NEW_KEY, false) ?: false
            if (new) {
                viewModel.setupNew()
            }
        }

        return view
    }

    override fun onTimeSet(hourOfDay: Int, minute: Int, callerId: Int) {
        if (callerId == CALLER_FINISH){
            viewModel.updateFinishTime(hourOfDay, minute)
        } else {
            viewModel.updateVoteStartTime(hourOfDay, minute)
        }
    }

    override fun onDateSet(year: Int, month: Int, day: Int, callerId: Int) {
        if (callerId == CALLER_FINISH) {
            viewModel.updateFinishDate(year, month, day)
        } else {
            viewModel.updateVoteStartDate(year, month, day)
        }
    }

    override fun handle(element: Any?) {
        if (element is NewEditConsensusViewModel.OpenPickerEvent) {
            if (element.date) {
                DatePicker
                    .newInstance(element.data, this@NewEditConsensusFragment, element.caller)
                    .show(fragmentManager, "necdDlg")
            } else {
                TimePicker
                    .newInstance(element.data, this@NewEditConsensusFragment, element.caller)
                    .show(fragmentManager, "nectDlg")
            }
        }
    }

    override var bindingLayoutId = R.layout.fragment_neweditconsensus

    companion object {
        const val CONSENSUS_KEY = "consensus_key"
        const val NEW_KEY = "new_key"
        const val CALLER_FINISH = 0
        const val CALLER_VOTING = 1
    }
}
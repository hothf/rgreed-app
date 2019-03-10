package de.ka.skyfallapp.ui.neweditconsensus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseFragment
import de.ka.skyfallapp.databinding.FragmentNeweditconsensusBinding
import de.ka.skyfallapp.repo.api.ConsensusResponse
import de.ka.skyfallapp.utils.DatePickeable
import de.ka.skyfallapp.utils.DatePicker
import de.ka.skyfallapp.utils.TimePickeable
import de.ka.skyfallapp.utils.TimePicker

class NewEditConsensusFragment : TimePickeable, DatePickeable,
    BaseFragment<FragmentNeweditconsensusBinding, NewEditConsensusViewModel>(
        NewEditConsensusViewModel::class
    ) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        if (savedInstanceState == null) {
            val consensus = arguments?.getSerializable(CONSENSUS_KEY) as? ConsensusResponse
            if (consensus != null) {
                viewModel.setupEdit(consensus)
            } else {
                viewModel.setupNew()
            }
        } else {
            viewModel.restore()
        }

        return view
    }


    //TODO check time pickings and description !!!
    override fun onTimeSet(hourOfDay: Int, minute: Int) {
        viewModel.updateFinishTime(hourOfDay, minute)
    }

    override fun onDateSet(year: Int, month: Int, day: Int) {
        viewModel.updateFinishDate(year, month, day)
    }

    override fun handle(element: Any?) {
        if (element is NewEditConsensusViewModel.OpenFinishPickerEvent) {
            if (element.date) {
                DatePicker
                    .newInstance(element.data, this@NewEditConsensusFragment)
                    .show(fragmentManager, "necdDlg")
            } else {
                TimePicker
                    .newInstance(element.data, this@NewEditConsensusFragment)
                    .show(fragmentManager, "nectDlg")
            }
        }
    }

    override var bindingLayoutId = R.layout.fragment_neweditconsensus

    companion object {
        const val CONSENSUS_KEY = "consensus_key"
    }
}
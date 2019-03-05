package de.ka.skyfallapp.utils

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import de.ka.skyfallapp.ui.consensus.consensusdetail.neweditsuggestion.NewEditSuggestionFragment

import java.util.*

/**
 * A time picker dialog with extra accessors.
 */
class NewEditSuggestionsTimePicker : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()

        val time = arguments?.getLong(TIME)

        if (time != null) {
            calendar.time = Date(time)
        }

        return TimePickerDialog(
            requireContext(),
            this,
            calendar.get(Calendar.HOUR),
            calendar.get(Calendar.MINUTE),
            DateFormat.is24HourFormat(activity)
        )
    }

    override fun onTimeSet(picker: TimePicker?, hourOfDay: Int, minute: Int) {
        (targetFragment as? NewEditSuggestionFragment)?.onTimeSet(hourOfDay, minute)
    }

    companion object {
        const val TIME = "time_key"
    }
}
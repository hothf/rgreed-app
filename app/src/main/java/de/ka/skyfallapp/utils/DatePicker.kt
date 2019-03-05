package de.ka.skyfallapp.utils

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import de.ka.skyfallapp.ui.consensus.consensusdetail.neweditsuggestion.NewEditSuggestionFragment
import java.util.*

/**
 * A date picker dialog with extra accessors.
 */
class NewEditSuggestionsDatePicker : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()

        val time = arguments?.getLong(DATE)

        if (time != null) {
            calendar.time = Date(time)
        }

        return DatePickerDialog(
            requireContext(),
            this,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        (targetFragment as? NewEditSuggestionFragment)?.onDateSet(year, month, day)
    }

    companion object {
        const val DATE = "date_key"
    }
}
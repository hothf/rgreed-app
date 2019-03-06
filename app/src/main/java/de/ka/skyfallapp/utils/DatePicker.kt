package de.ka.skyfallapp.utils

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import java.util.*

/**
 * A date picker dialog with extra accessors.
 */
class DatePicker<T : DatePickeable> : DialogFragment(), DatePickerDialog.OnDateSetListener {

    private var datePickeable: T? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()

        val time = arguments?.getLong(DATE)

        datePickeable = targetFragment as? T

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
        datePickeable?.onDateSet(year, month, day)
    }

    companion object {
        const val DATE = "date_key"

        fun <T : DatePickeable> newInstance(date: Long, targetFragment: T) = DatePicker<T>().apply {
            arguments = Bundle().apply { putLong(DATE, date) }
            setTargetFragment(targetFragment as Fragment, 0)
        }
    }
}

/**
 * Listens for date picking events.
 */
interface DatePickeable {

    /**
     * Called on setting a date.
     */
    fun onDateSet(year: Int, month: Int, day: Int)
}
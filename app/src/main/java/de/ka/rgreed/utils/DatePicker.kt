package de.ka.rgreed.utils

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
    private var caller = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()

        val time = arguments?.getLong(DATE)
        caller = arguments?.getInt(CALLER, 0) ?: 0

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
        datePickeable?.onDateSet(year, month, day, caller)
    }

    companion object {
        const val DATE = "date_key"
        const val CALLER = "caller_key"

        fun <T : DatePickeable> newInstance(date: Long, targetFragment: T, callerId: Int = 0) = DatePicker<T>().apply {
            arguments = Bundle().apply {
                putLong(DATE, date)
                putInt(CALLER, callerId)
            }
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
     *
     * @param callerId optional id to identify the original caller.
     */
    fun onDateSet(year: Int, month: Int, day: Int, callerId: Int = 0)
}
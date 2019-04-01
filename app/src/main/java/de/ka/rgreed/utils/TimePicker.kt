package de.ka.rgreed.utils

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment

import java.util.*


/**
 * A time picker dialog with extra accessors.
 */
class TimePicker<T : TimePickeable> : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    private var timePickeable: T? = null
    private var caller = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()

        val time = arguments?.getLong(TIME)
        caller = arguments?.getInt(DatePicker.CALLER, 0) ?: 0

        timePickeable = targetFragment as? T

        if (time != null) {
            calendar.time = Date(time)
        }

        return TimePickerDialog(
            requireContext(),
            this,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            DateFormat.is24HourFormat(activity)
        )
    }

    override fun onTimeSet(picker: TimePicker?, hourOfDay: Int, minute: Int) {
        timePickeable?.onTimeSet(hourOfDay, minute, caller)
    }

    companion object {
        const val TIME = "time_key"
        const val CALLER = "caller_key"

        fun <T : TimePickeable> newInstance(time: Long, targetFragment: T, callerId: Int = 0) = TimePicker<T>().apply {
            arguments = Bundle().apply {
                putLong(TIME, time)
                putInt(CALLER, callerId)
            }
            setTargetFragment(targetFragment as Fragment, 0)
        }
    }
}

/**
 * Listens for time pick events.
 */
interface TimePickeable {

    /**
     * Called on setting the time.
     *
     * @param callerId optional id to identify the original caller.
     */
    fun onTimeSet(hourOfDay: Int, minute: Int, callerId: Int = 0)
}
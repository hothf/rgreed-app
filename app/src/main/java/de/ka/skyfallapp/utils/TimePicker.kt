package de.ka.skyfallapp.utils

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()

        val time = arguments?.getLong(TIME)

        timePickeable = targetFragment as? T

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
        timePickeable?.onTimeSet(hourOfDay, minute)
    }

    companion object {
        const val TIME = "time_key"

        fun <T : TimePickeable> newInstance(time: Long, targetFragment: T) = TimePicker<T>().apply {
            arguments = Bundle().apply { putLong(TIME, time) }
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
     */
    fun onTimeSet(hourOfDay: Int, minute: Int)
}
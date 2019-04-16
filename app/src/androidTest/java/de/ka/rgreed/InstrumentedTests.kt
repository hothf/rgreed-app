package de.ka.rgreed

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.ka.rgreed.ui.MainActivity
import de.ka.rgreed.utils.TimeAwareTextView
import de.ka.rgreed.utils.TimeAwareUpdate

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import androidx.test.core.app.ActivityScenario
import timber.log.Timber


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 *
 * Uses android x compatibility libraries.
 */
@RunWith(AndroidJUnit4::class)
class InstrumentedTests {

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("de.ka.rgreed.debug", appContext.packageName)
    }

    @Test
    fun showDateAwareText() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val textView = TimeAwareTextView(activity)

                Timber.e("---Displaying time aware texts start---")

                // seconds
                displayTimeAwareText(textView, 1000)
                displayTimeAwareText(textView, 2000)
                displayTimeAwareText(textView, 60_000)
                displayTimeAwareText(textView, 61_000)
                displayTimeAwareText(textView, 120_000)

                // minutes
                displayTimeAwareText(textView, 121_000)
                displayTimeAwareText(textView, 15 * 60 * 1000)
                displayTimeAwareText(textView, 25 * 60 * 1000)
                displayTimeAwareText(textView, 60 * 60 * 1000 - 1000)

                // hours
                displayTimeAwareText(textView, 60 * 60 * 1000)
                displayTimeAwareText(textView, 60 * 60 * 1000 + 5 * 60 * 1000)
                displayTimeAwareText(textView, 60 * 60 * 1000 * 2)

                // too long / too short / inalid
                displayTimeAwareText(textView, 2000, true)
                displayTimeAwareText(textView, 0, true)
                displayTimeAwareText(textView, 60 * 60 * 1000 * 2 + 1000)
                displayTimeAwareText(textView, 60 * 60 * 1000 * 3)
                displayTimeAwareText(textView, -1000)
                displayTimeAwareText(textView, 0)

                Timber.e("---Displaying time aware texts end---")

                assertNotNull(textView.text)
            }
        }
    }

    private fun displayTimeAwareText(textView: TimeAwareTextView, timeOffset: Long, isInvalid: Boolean = false) {
        textView.setTimeAwareText(
            TimeAwareUpdate(
                R.string.consensus_until,
                System.currentTimeMillis() + timeOffset,
                isInvalid
            )
        )
        Timber.e("Displaying time aware texts:" + textView.text)
    }

}

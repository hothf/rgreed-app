package de.ka.skyfallapp.base.events

import android.os.Bundle
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.google.android.material.snackbar.Snackbar
import de.ka.skyfallapp.R
import kotlin.reflect.KClass

/**
 * A collection of commonly used events.
 */
sealed class Event

enum class SnackType(@ColorRes val textColorRes: Int, @ColorRes val backgroundColorRes: Int) {
    DEFAULT(R.color.snackDefaultTextColor, R.color.snackDefaultBackground),
    WARNING(R.color.snackWarningTextColor, R.color.snackWarningBackground),
    ERROR(R.color.snackErrorTextColor, R.color.snackErrorBackground)
}

data class ShowSnack(
    val message: String,
    val type: SnackType = SnackType.DEFAULT,
    val length: Int = Snackbar.LENGTH_LONG
) : Event()


/**
 * Handles navigation to fragments.
 *
 * @param navigationTargetId the target to navigate to. May reference a destination or action
 * @param clearBackStack clears the backstack. Note that you can not use [navigationPopupToId] when this is set to true
 * @param args optional arguments to pass for the target fragment
 * @param navOptions optional navigator options for setting the default animations and behaviour
 * @param extras options for the transaction, like shared views fro transitions
 * @param navigationPopupToId id for target to popup to. This will only work, if [clearBackStack] is set to false
 */
data class NavigateTo(
    @IdRes val navigationTargetId: Int,
    val clearBackStack: Boolean = false,
    val args: Bundle? = null,
    val navOptions: NavOptions? = null,
    val extras: Navigator.Extras? = null,
    @IdRes val navigationPopupToId: Int? = null
) : Event()

data class Open(
    val url: String? = null,
    val clazz: KClass<*>? = null,
    val args: Bundle? = null
) : Event()

data class Handle<T>(
    val element: T
) : Event()


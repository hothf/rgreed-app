package de.ka.skyfallapp.base.events

import android.os.Bundle
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.google.android.material.snackbar.Snackbar
import de.ka.skyfallapp.R
import kotlin.reflect.KClass

/**
 * A collection of commonly used events.
 */


sealed class Event

data class ShowSnack(
        val message: String,
        @ColorRes val colorRes: Int = R.color.colorAccent,
        val length: Int = Snackbar.LENGTH_LONG
) : Event()

data class NavigateTo(
        @IdRes val navigationTargetId: Int,
        val clearBackStack: Boolean = false,
        val args: Bundle? = null,
        val navOptions: NavOptions? = null,
        val extras: Navigator.Extras? = null
) : Event()

data class Open(
        val url: String? = null,
        val clazz: KClass<*>? = null,
        val args: Bundle? = null
) : Event()

data class Handle<T>(
        val element: T
) : Event()


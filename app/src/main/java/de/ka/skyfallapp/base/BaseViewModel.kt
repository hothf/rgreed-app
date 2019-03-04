package de.ka.skyfallapp.base

import android.app.Application
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.events.*
import de.ka.skyfallapp.repo.Repository
import de.ka.skyfallapp.utils.ApiErrorHandler
import io.reactivex.disposables.CompositeDisposable
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import kotlin.reflect.KClass

/**
 * The base view model.
 */
abstract class BaseViewModel(val app: Application) : AndroidViewModel(app), KoinComponent {

    val events = QueueLiveEvent<Event>()

    val repository: Repository by inject()
    val apiErrorHandler: ApiErrorHandler by inject()

    val compositeDisposable = CompositeDisposable()

    private fun queueEvent(event: Event) = events.queueValue(event)

    /**
     * Navigates to the given resource id. Pass -1 as id to simply pop the back stack.
     */
    fun navigateTo(
        @IdRes navigationTargetId: Int,
        clearBackStack: Boolean = false,
        args: Bundle? = null,
        navOptions: NavOptions? = null,
        extras: Navigator.Extras? = null,
        navDirections: NavDirections? = null
    ) = queueEvent(
        NavigateTo(
            navigationTargetId = navigationTargetId,
            clearBackStack = clearBackStack,
            args = args,
            navOptions = navOptions,
            extras = extras,
            directions = navDirections
        )
    )

    fun showSnack(message: String) = queueEvent(
        ShowSnack(
            message = message,
            colorRes = R.color.colorAccent
        )
    )

    fun open(
        url: String? = null,
        clazz: KClass<*>? = null,
        args: Bundle? = null
    ) = queueEvent(
        Open(
            url = url,
            clazz = clazz,
            args = args
        )
    )

    fun <T> handle(element: T) = queueEvent(
        Handle(
            element = element
        )
    )

    override fun onCleared() {
        super.onCleared()

        compositeDisposable.clear()
    }
}
package de.ka.rgreed.base

import android.app.Application
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import de.ka.rgreed.base.events.*
import de.ka.rgreed.repo.Repository
import de.ka.rgreed.utils.ApiErrorManager
import de.ka.rgreed.utils.BackPressEventListener
import de.ka.rgreed.utils.GlobalMessageManager
import de.ka.rgreed.utils.NavigationUtils.BACK
import de.ka.rgreed.utils.Snacker
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
    val apiErrorHandler: ApiErrorManager by inject()
    val messageManager: GlobalMessageManager by inject()
    val backPressListener: BackPressEventListener by inject()

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
        animType: AnimType = AnimType.DEFAULT,
        @IdRes popupToId: Int? = null
    ) {
        if (navigationTargetId == BACK) {
            backPressListener.onBack()
            return
        }

        queueEvent(
            NavigateTo(
                navigationTargetId = navigationTargetId,
                clearBackStack = clearBackStack,
                args = args,
                navOptions = navOptions,
                extras = extras,
                animType = animType,
                navigationPopupToId = popupToId
            )
        )
    }

    fun handleBack() {
        queueEvent(Back(true))
    }

    fun showSnack(message: String, snackType: Snacker.SnackType = Snacker.SnackType.DEFAULT) = queueEvent(
        ShowSnack(message = message, type = snackType)
    )

    fun open(url: String? = null, clazz: KClass<*>? = null, args: Bundle? = null) = queueEvent(
        Open(url = url, clazz = clazz, args = args)
    )

    fun <T> handle(element: T) = queueEvent(Handle(element = element))

    /**
     * Handles a general error.
     */
    fun handleGeneralError(throwable: Throwable) {
        apiErrorHandler.handle(null, throwable)
    }

    override fun onCleared() {
        super.onCleared()

        compositeDisposable.clear()
    }
}
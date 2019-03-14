package de.ka.skyfallapp.utils

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.events.NavigateTo

object NavigationUtils {

    //TODO to investigate> navigate BACK on fragment openend through deeplink not working

    /**
     * Indicate to simply go back and popup the last fragment.
     */
    const val BACK = -1

    /**
     * Indicate to popup to the given [NavigateTo.navigationTargetId]. Has to be supplied!
     */
    const val POPUPTO = -2

    /**
     * Navigates to the destination described in the [NavigateTo] event with the given [NavController].
     *
     * @param navController the nav controller to use for the navigation
     * @param navigateToEvent the event to use for directions and actions
     */
    fun navigateTo(navController: NavController, navigateToEvent: NavigateTo) {

        if (navigateToEvent.navigationTargetId == BACK) {
            navController.popBackStack()
            return
        } else if (navigateToEvent.navigationTargetId == POPUPTO) {
            navController.popBackStack(navigateToEvent.navigationPopupToId!!, true)
            return
        }

        val navOptions = setupOptions(
            navigateToEvent.clearBackStack,
            navController,
            navigateToEvent.navOptions,
            navigateToEvent.navigationPopupToId
        )

        navController.navigate(
            navigateToEvent.navigationTargetId,
            navigateToEvent.args,
            navOptions,
            navigateToEvent.extras
        )
    }

    private fun setupOptions(
        clearBackStack: Boolean,
        navController: NavController,
        navOptions: NavOptions?,
        popupToId: Int?
    ): NavOptions {
        return if (navOptions == null) {
            NavOptions.Builder().apply {
                setEnterAnim(R.anim.nav_default_enter_anim)
                setExitAnim(R.anim.nav_default_exit_anim)
                setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
                setPopExitAnim(R.anim.nav_default_pop_exit_anim)

                if (clearBackStack) {
                    setPopUpTo(navController.graph.id, true)
                } else {
                    popupToId?.let { setPopUpTo(it, true) }
                }
            }.build()
        } else {
            NavOptions.Builder().apply {
                setEnterAnim(navOptions.enterAnim)
                setExitAnim(navOptions.exitAnim)
                setPopEnterAnim(navOptions.popEnterAnim)
                setPopExitAnim(navOptions.popExitAnim)
                setLaunchSingleTop(navOptions.shouldLaunchSingleTop())

                if (clearBackStack) {
                    setPopUpTo(navController.graph.id, true)
                } else {
                    popupToId?.let { setPopUpTo(it, true) }
                }
            }.build()

        }
    }
}
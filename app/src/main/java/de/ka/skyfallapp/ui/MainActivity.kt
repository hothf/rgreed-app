package de.ka.skyfallapp.ui

import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.snackbar.Snackbar
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.BaseActivity
import de.ka.skyfallapp.base.events.NavigateTo
import de.ka.skyfallapp.base.events.ShowSnack
import de.ka.skyfallapp.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(MainViewModel::class) {

    override var bindingLayoutId = R.layout.activity_main

    override fun onSupportNavigateUp() = findNavController(this, R.id.main_nav_host_fragment).navigateUp()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getBinding()?.bottomNavigation?.let {
            NavigationUI.setupWithNavController(
                it,
                Navigation.findNavController(this, R.id.main_nav_host_fragment)
            )
        }

        Navigation.findNavController(this, R.id.main_nav_host_fragment)
            .addOnDestinationChangedListener { _, dest: NavDestination, _ ->

                if (dest.id == R.id.homeFragment
                    || dest.id == R.id.personalFragment
                    || dest.id == R.id.settingsFragment
                ) {
                    animateBottomBar(up = true)
                } else {
                    animateBottomBar(up = false)
                }

                if (dest.id == R.id.homeFragment
                    || dest.id == R.id.personalFragment
                ) {
                    animateButton(true)
                } else {
                    animateButton(false)
                }

                //TODO this can be simplified, e.g. using a map: just observing how the complexity
                // gets before refactoring this.
            }
    }


    private fun animateBottomBar(up: Boolean) {
        getBinding()?.bottomNavigation?.let {
            if (!up) {
                it.animate()
                    .translationY(it.height.toFloat())
                    .setInterpolator(DecelerateInterpolator())
                    .setDuration(200)

            } else {
                it.animate().translationY(0f).setInterpolator(AccelerateInterpolator()).setDuration(200)
            }
        }
    }

    private fun animateButton(show: Boolean) {
        getBinding()?.addButton?.let {
            if (show) {
                it.animate().rotation(0.0f).scaleX(1.0f).scaleY(1.0f).setInterpolator(AccelerateInterpolator())
                    .setDuration(200)
            } else {
                it.animate().rotation(90.0f).scaleX(0.0f).scaleY(0.0f).setInterpolator(DecelerateInterpolator())
                    .setDuration(200)
            }
        }
    }

    override fun onShowSnack(view: View, showSnack: ShowSnack) {
        Snackbar.make(view, showSnack.message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onNavigateTo(navigateTo: NavigateTo) {
        val navController = Navigation.findNavController(this, R.id.main_nav_host_fragment)

        navController.navigate(
            navigateTo.navigationTargetId
        )
    }
}

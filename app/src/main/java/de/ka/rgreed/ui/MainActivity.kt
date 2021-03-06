package de.ka.rgreed.ui

import android.animation.Animator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.NavigationUI
import de.ka.rgreed.R
import de.ka.rgreed.base.BaseActivity
import de.ka.rgreed.base.events.NavigateTo
import de.ka.rgreed.base.events.ShowSnack
import de.ka.rgreed.databinding.ActivityMainBinding
import de.ka.rgreed.utils.NavigationUtils

/**
 * The main entry point of this app.
 *
 * **This is a single-activity app.**
 *
 * The activity is responsible to handle global events and ui elements which have to be visible throughout the app.
 */
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(MainViewModel::class) {

    override var bindingLayoutId = R.layout.activity_main

    override fun onSupportNavigateUp() = findNavController(this, R.id.main_nav_host_fragment).navigateUp()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.let {
            getBinding()?.addButton?.visibility = it.getInt(STATE_BUTTON_KEY)
            getBinding()?.bottomNavigation?.translationY = it.getFloat(STATE_BOTTOM_BAR_KEY)
        }

        getBinding()?.bottomNavigation?.let {
            NavigationUI.setupWithNavController(
                it,
                Navigation.findNavController(this, R.id.main_nav_host_fragment)
            )
        }

        Navigation.findNavController(this, R.id.main_nav_host_fragment)
            .addOnDestinationChangedListener { _, dest: NavDestination, _ ->
                if (dest.id == R.id.homeFragment || dest.id == R.id.personalFragment || dest.id == R.id.searchFragment) {
                    animateBottomBar(up = true)
                } else {
                    animateBottomBar(up = false)
                }

                if (dest.id == R.id.homeFragment || dest.id == R.id.personalFragment) {
                    animateButton(true)
                } else {
                    animateButton(false)
                }
            }

        if (savedInstanceState == null) {
            viewModel.registerForPush()
        }
    }

    private fun animateBottomBar(up: Boolean) {
        getBinding()?.bottomNavigation?.let {
            if (!up) {
                if (it.translationY > 0) {
                    return
                }
                it.post {
                    it.animate()
                        .translationY(it.height.toFloat())
                        .setInterpolator(DecelerateInterpolator())
                        .setDuration(DURATION_ANIM_MS)
                }

            } else {
                it.post {
                    it.animate().translationY(0f).setInterpolator(AccelerateInterpolator()).setDuration(200)
                }
            }
        }
    }

    private fun animateButton(show: Boolean) {
        getBinding()?.addButton?.let {
            if (show) {
                it.visibility = View.VISIBLE
                it.animate()
                    .rotation(0.0f)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setInterpolator(AccelerateInterpolator())
                    .setDuration(DURATION_ANIM_MS)
                    .setListener(null)
                it.isEnabled = true
            } else {
                it.animate()
                    .rotation(90.0f)
                    .scaleX(0.0f)
                    .scaleY(0.0f)
                    .setInterpolator(DecelerateInterpolator())
                    .setDuration(DURATION_ANIM_MS)
                    .setListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(p0: Animator?) {
                            // do nothing
                        }

                        override fun onAnimationEnd(p0: Animator?) {
                            it.visibility = View.GONE
                        }

                        override fun onAnimationCancel(p0: Animator?) {
                            // do nothing
                        }

                        override fun onAnimationStart(p0: Animator?) {
                            // do nothing
                        }

                    })
                it.isEnabled = false
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        getBinding()?.addButton?.visibility?.let {
            outState?.putInt(STATE_BUTTON_KEY, it)
        }

        getBinding()?.bottomNavigation?.translationY?.let {
            outState?.putFloat(STATE_BOTTOM_BAR_KEY, it)
        }

        super.onSaveInstanceState(outState)
    }

    override fun onShowSnack(view: View, showSnack: ShowSnack) {
        getBinding()?.mainSnacker?.reveal(showSnack)
    }

    override fun onNavigateTo(navigateTo: NavigateTo) {
        val navController = Navigation.findNavController(this, R.id.main_nav_host_fragment)

        NavigationUtils.navigateTo(navController, navigateTo)
    }

    companion object {
        const val DURATION_ANIM_MS = 200L
        const val STATE_BUTTON_KEY = "bt_state_key"
        const val STATE_BOTTOM_BAR_KEY = "bottom_state_key"
    }
}

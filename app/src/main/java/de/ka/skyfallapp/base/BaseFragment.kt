package de.ka.skyfallapp.base

import android.content.Intent
import android.net.Uri

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import de.ka.skyfallapp.BR
import de.ka.skyfallapp.R
import de.ka.skyfallapp.base.events.Handle
import de.ka.skyfallapp.base.events.NavigateTo
import de.ka.skyfallapp.base.events.Open
import de.ka.skyfallapp.base.events.ShowSnack
import de.ka.skyfallapp.utils.BackPressInterceptor
import de.ka.skyfallapp.utils.DirtyDataWatcher
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModelByClass
import timber.log.Timber
import kotlin.reflect.KClass

/**
 * Represents a base fragment. Extending fragments should always be combined with a viewModel,
 * that's why offering the fragment layout resource id and viewModel is mandatory.
 * The viewModel updates the ui with Databinding.
 * To use the binding of the ui after inflating, use [getBinding].
 *
 * Created by Thomas Hofmann
 */
abstract class BaseFragment<out T : ViewDataBinding, E : BaseViewModel>(clazz: KClass<E>) : Fragment() {

    abstract var bindingLayoutId: Int

    private lateinit var binding: ViewDataBinding

    val viewModel: E by lazy {
        getViewModelByClass(
            clazz,
            from = { activity })
    }

    val backPressInterceptor: BackPressInterceptor by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = DataBindingUtil.inflate(layoutInflater, bindingLayoutId, null, true)

        binding.apply {
            setVariable(BR.viewModel, viewModel)
            setLifecycleOwner(viewLifecycleOwner)
            executePendingBindings()
        }

        return binding.root
    }

    /**
     * Retrieves the view binding of the fragment. May only be useful after [onCreateView].
     */
    @Suppress("UNCHECKED_CAST")
    fun getBinding() = binding as? T

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.events.observe(
            viewLifecycleOwner,
            Observer {
                Timber.i("Event observed: $it")

                when (it) {
                    is ShowSnack -> snack(activity?.currentFocus ?: binding.root, it)
                    is NavigateTo -> navigateTo(it)
                    is Open -> open(it)
                    is Handle<*> -> handle(it.element)
                }
            }
        )
    }

    /**
     * Called when a generic element should be handled.
     */
    open fun handle(element: Any?) {
        // implemented by children
    }

    private fun open(openEvent: Open) {
        if (openEvent.clazz != null) {

            val intent = Intent(activity, openEvent.clazz.java)

            openEvent.args?.let {
                intent.putExtras(it)
            }

            startActivity(intent)
        } else {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(openEvent.url)))
        }
    }

    private fun snack(view: View, showSnackbarEvent: ShowSnack): Snackbar = with(showSnackbarEvent) {
        val whiteSpan = ForegroundColorSpan(ContextCompat.getColor(view.context, R.color.colorPrimary))
        val snackbarText = SpannableStringBuilder(message)
        snackbarText.setSpan(whiteSpan, 0, snackbarText.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        val snackbar = Snackbar.make(view, snackbarText, length)
        snackbar.view.setBackgroundColor(ContextCompat.getColor(view.context, colorRes))

        snackbar.show()

        return snackbar
    }


    private fun navigateTo(navigateToEvent: NavigateTo) {

        val navController = view?.findNavController()

        if (navController == null) {
            Timber.e("Could not find nav controller!")
            return
        }

        if (navigateToEvent.navigationTargetId == -1) {
            navController.popBackStack()
            return
        }

        var navOptions = navigateToEvent.navOptions

        if (navigateToEvent.clearBackStack) {
            navOptions = setClearBackStack(navController, navOptions)
        }

        navController.navigate(
            navigateToEvent.navigationTargetId,
            navigateToEvent.args,
            navOptions,
            navigateToEvent.extras
        )
    }

    private fun setClearBackStack(navController: NavController, navOptions: NavOptions?): NavOptions {
        return if (navOptions == null) {
            NavOptions.Builder()
                .setPopUpTo(navController.graph.id, true)
                .build()
        } else {
            NavOptions.Builder()
                .setEnterAnim(navOptions.enterAnim)
                .setPopUpTo(navController.graph.id, true)
                .setExitAnim(navOptions.exitAnim)
                .setPopEnterAnim(navOptions.popEnterAnim)
                .setPopExitAnim(navOptions.popExitAnim)
                .setLaunchSingleTop(navOptions.shouldLaunchSingleTop())
                .build()

        }

    }


}


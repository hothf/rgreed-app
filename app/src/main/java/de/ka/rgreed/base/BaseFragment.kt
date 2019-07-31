package de.ka.rgreed.base

import android.content.Intent
import android.net.Uri

import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import de.ka.rgreed.BR
import de.ka.rgreed.base.events.*
import de.ka.rgreed.utils.NavigationUtils
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

    val viewModel: E by lazy { getViewModelByClass(clazz)}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = DataBindingUtil.inflate(layoutInflater, bindingLayoutId, null, true)

        binding.apply {
            setVariable(BR.viewModel, viewModel)
            lifecycleOwner = viewLifecycleOwner
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

    fun navigateTo(navigateToEvent: NavigateTo) {

        val navController = view?.findNavController()

        if (navController == null) {
            Timber.e("Could not find nav controller!")
            return
        }

        NavigationUtils.navigateTo(navController, navigateToEvent)
    }
}


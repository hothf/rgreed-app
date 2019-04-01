package de.ka.rgreed.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import de.ka.rgreed.BR
import de.ka.rgreed.base.events.*
import org.koin.androidx.viewmodel.ext.android.viewModelByClass
import timber.log.Timber
import kotlin.reflect.KClass

/**
 * Represents a base activity. Extending activities should always be combined with a viewModel,
 * that's why offering the activity layout resource id and viewModel is mandatory.
 * The viewModel updates the ui with Databinding.
 * To use the binding of the ui after inflating, use [getBinding].
 *
 * Created by Thomas Hofmann
 */
abstract class BaseActivity<out T : ViewDataBinding, E : BaseViewModel>(clazz: KClass<E>) : AppCompatActivity() {

    abstract var bindingLayoutId: Int

    private lateinit var binding: ViewDataBinding

    val viewModel: E by viewModelByClass(clazz)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.inflate(layoutInflater, bindingLayoutId, null, true)

        binding.apply {
            setVariable(BR.viewModel, viewModel)
            setLifecycleOwner(this@BaseActivity)
            executePendingBindings()
        }

        setContentView(binding.root)

        viewModel.events.observe(
            this,
            Observer {
                Timber.i("Event observed: $it")

                when (it) {
                    is ShowSnack -> onShowSnack(this.currentFocus ?: binding.root, it)
                    is NavigateTo -> onNavigateTo(it)
                    is Open -> onOpen(it)
                    is Handle<*> -> onHandle(it.element)
                    is Back -> if (it.fired) onBackPressed()
                }
            }
        )
    }

    open fun onShowSnack(view: View, showSnack: ShowSnack) {}

    open fun onNavigateTo(navigateTo: NavigateTo) {}

    open fun onOpen(open: Open) {}

    open fun onHandle(element: Any?) {}

    /**
     * Retrieves the view binding of the activity. May only be useful after [onCreate].
     */
    @Suppress("UNCHECKED_CAST")
    fun getBinding() = binding as? T
}

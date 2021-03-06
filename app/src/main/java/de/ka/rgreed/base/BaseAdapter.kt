package de.ka.rgreed.base

import android.app.Application
import android.content.Context

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import androidx.databinding.BaseObservable
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import de.ka.rgreed.BR
import de.ka.rgreed.repo.Repository
import io.reactivex.disposables.CompositeDisposable
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

abstract class BaseAdapter<E : BaseItemViewModel>(
    private val items: ArrayList<E> = arrayListOf(),
    diffCallback: DiffUtil.ItemCallback<E>? = null
) : RecyclerView.Adapter<BaseViewHolder<*>>(), KoinComponent {

    val app: Application by inject()
    val layoutInflater: LayoutInflater = LayoutInflater.from(app.applicationContext)

    private var differ: AsyncListDiffer<E>? = null

    init {
        if (diffCallback != null) {
            @Suppress("LeakingThis")
            differ = AsyncListDiffer(this, diffCallback)
        }
    }

    open var isEmpty: Boolean = items.isEmpty()

    fun getItems(): List<E> {
        return if (differ != null) {
            differ!!.currentList
        } else {
            items
        }
    }

    open fun clear() {
        if (differ != null) {
            differ?.submitList(listOf())
        } else {
            items.clear()
            notifyDataSetChanged()
        }
        isEmpty = true
    }

    open fun addItem(index: Int = 0, item: E) {
        if (differ != null) {
            differ!!.submitList(differ!!.currentList.toMutableList().apply { add(index, item) })
        } else {
            items.add(item)

            notifyDataSetChanged()
        }
        isEmpty = false
    }

    open fun setItems(newItems: List<E>) {
        if (differ != null) {
            differ!!.submitList(newItems)
        } else {
            items.clear()

            items.addAll(newItems)

            notifyDataSetChanged()
        }
        isEmpty = newItems.isEmpty()
    }

    open fun addItems(newItems: List<E>) {
        if (differ != null) {
            val items = ArrayList(differ!!.currentList)
            items.addAll(newItems)

            differ!!.submitList(items)
        } else {
            items.addAll(newItems)
            notifyDataSetChanged()
        }
        isEmpty = newItems.isEmpty()
    }

    override fun getItemCount(): Int {
        if (differ != null) {
            return differ!!.currentList.size
        }
        return items.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        if (differ != null) {
            holder.bind(differ!!.currentList[holder.adapterPosition])
        } else {
            holder.bind(items[holder.adapterPosition])
        }

        if (holder.adapterPosition in 0 until itemCount) {
            if (differ != null) {
                differ!!.currentList[holder.adapterPosition].onAttached()
            } else {
                items[holder.adapterPosition].onAttached()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (differ != null) {
            differ!!.currentList[position].type
        } else {
            items[position].type
        }
    }

    override fun onViewRecycled(holder: BaseViewHolder<*>) {
        if (holder.adapterPosition in 0 until itemCount) {
            if (differ != null) {
                differ!!.currentList[holder.adapterPosition].onCleared()
            } else {
                items[holder.adapterPosition].onCleared()
            }
        }
        super.onViewRecycled(holder)
    }
}

/**
 * These viewModels are not created through the android viewmodel framework;  may be used
 * with observable fields.
 */
abstract class BaseItemViewModel(val type: Int = 0) : BaseObservable(), KoinComponent {

    val appContext: Context by inject()
    val repository: Repository by inject()

    var compositeDisposable: CompositeDisposable? = null

    fun onAttached() {
        compositeDisposable = CompositeDisposable()
    }

    fun onCleared() {
        compositeDisposable?.clear()
    }
}

class BaseViewHolder<T : ViewDataBinding>(private val binding: T) : RecyclerView.ViewHolder(binding.root) {

    fun bind(viewModel: BaseItemViewModel) {
        binding.setVariable(BR.viewModel, viewModel)
        binding.executePendingBindings()
    }
}




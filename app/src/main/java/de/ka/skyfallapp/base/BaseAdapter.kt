package de.ka.skyfallapp.base

import android.app.Application

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import de.ka.skyfallapp.BR
import io.reactivex.disposables.CompositeDisposable
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

abstract class BaseAdapter<E : BaseItemViewModel>(
    private val owner: LifecycleOwner,
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

    fun getItems(): List<E> {
        return if (differ != null) {
            differ!!.currentList
        } else {
            items
        }
    }

    open fun addItem(index: Int = 0, item: E) {
        if (differ != null) {
            differ!!.submitList(differ!!.currentList.toMutableList().apply { add(index, item) })
        } else {
            items.add(item)

            notifyDataSetChanged()
        }
    }

    open fun addItems(newItems: List<E>) {
        if (differ != null) {
            differ!!.submitList(newItems)
        } else {
            items.clear()

            items.addAll(newItems)

            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        if (differ != null) {
            return differ!!.currentList.size
        }
        return items.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        if (differ != null) {
            holder.bind(owner, differ!!.currentList[holder.adapterPosition])
        } else {
            holder.bind(owner, items[holder.adapterPosition])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (differ != null) {
            differ!!.currentList[position].type
        } else {
            items[position].type
        }
    }

    override fun onViewAttachedToWindow(holder: BaseViewHolder<*>) {
        super.onViewAttachedToWindow(holder)

        if (holder.adapterPosition in 0..(itemCount - 1)) {
            if (differ != null) {
                differ!!.currentList[holder.adapterPosition].onAttached()

            } else {
                items[holder.adapterPosition].onAttached()
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: BaseViewHolder<*>) {

        if (holder.adapterPosition in 0..(itemCount - 1)) {
            if (differ != null) {
                differ!!.currentList[holder.adapterPosition].onCleared()

            } else {
                items[holder.adapterPosition].onCleared()
            }

        }

        super.onViewDetachedFromWindow(holder)
    }
}

/**
 * These viewModels are not created through the android viewmodel framework but still may be used
 * with [MutableLiveData<T>].
 */
abstract class BaseItemViewModel(val type: Int = 0) : KoinComponent {

    var compositeDisposable: CompositeDisposable? = null

    fun onAttached() {
        compositeDisposable = CompositeDisposable()
    }

    fun onCleared() {
        compositeDisposable?.clear()
    }
}

class BaseViewHolder<T : ViewDataBinding>(private val binding: T) : RecyclerView.ViewHolder(binding.root) {

    fun bind(owner: LifecycleOwner, viewModel: BaseItemViewModel) {
        binding.setVariable(BR.viewModel, viewModel)
        binding.setLifecycleOwner(owner)
        binding.executePendingBindings()
    }
}




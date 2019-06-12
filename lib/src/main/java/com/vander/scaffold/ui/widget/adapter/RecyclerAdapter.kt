package com.vander.scaffold.ui.widget.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import io.reactivex.processors.PublishProcessor

/**
 * Observable recycler adapter, type parameters specify recycler item object and event object
 *
 * @author marian on 13.3.2017.
 */

class RecyclerAdapter<T : AdapterModel, R>(
    private val source: DataSource<T>,
    private val viewHolderFactory: (Int, View) -> ViewHolder<T, R>)
  : RecyclerView.Adapter<ViewHolder<T, R>>(), ListUpdateCallback, AdapterItemEventObservable<R> {

  override val itemEventSource: PublishProcessor<R> = PublishProcessor.create<R>()

  init {
    source.listUpdateCallback = this
  }

  override fun onCreateViewHolder(parent: ViewGroup, layoutRes: Int): ViewHolder<T, R> {
    val inflater = LayoutInflater.from(parent.context)
    val root = inflater.inflate(layoutRes, parent, false)
    return viewHolderFactory.invoke(layoutRes, root)
  }

  override fun onBindViewHolder(holder: ViewHolder<T, R>, position: Int) {
    holder.bind(source[position])
  }

  override fun onViewAttachedToWindow(holder: ViewHolder<T, R>) {
    super.onViewAttachedToWindow(holder)
    holder.attach()
    holder.disposable.addAll(holder.itemEvent.subscribe { itemEventSource.onNext(it) })
  }

  override fun onViewDetachedFromWindow(holder: ViewHolder<T, R>) {
    holder.disposable.clear()
    super.onViewDetachedFromWindow(holder)
  }

  override fun getItemCount(): Int = source.itemCount

  override fun getItemViewType(position: Int): Int = source.getLayoutRes(position)

  override fun getItemId(position: Int): Long = source.getItemId(position)

  override fun onInserted(position: Int, count: Int) = notifyItemRangeInserted(position, count)

  override fun onRemoved(position: Int, count: Int) = notifyItemRangeRemoved(position, count)

  override fun onMoved(fromPosition: Int, toPosition: Int) = notifyItemMoved(fromPosition, toPosition)

  override fun onChanged(position: Int, count: Int, payload: Any?) = notifyItemRangeChanged(position, count, payload)

}

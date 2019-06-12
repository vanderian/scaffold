package com.vander.scaffold.ui.widget.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.*
import io.reactivex.processors.PublishProcessor

/**
 * Observable paged recycler adapter, type parameters specify recycler item object and event object
 */

class PagedRecyclerAdapter<T : AdapterModel, R>(
    diffItemCallback: DiffUtil.ItemCallback<T> = DefaultDiffCallback(),
    private val viewHolderFactory: (Int, View) -> ViewHolder<T, R>)
  : PagedListAdapter<T, ViewHolder<T, R>>(diffItemCallback), AdapterItemEventObservable<R> {

  override val itemEventSource: PublishProcessor<R> = PublishProcessor.create<R>()

  override fun onCreateViewHolder(parent: ViewGroup, layoutRes: Int): ViewHolder<T, R> =
      viewHolderFactory.invoke(layoutRes, LayoutInflater.from(parent.context).inflate(layoutRes, parent, false))

  override fun onBindViewHolder(holder: ViewHolder<T, R>, position: Int) =
      holder.bind(getItem(position))


  override fun onViewAttachedToWindow(holder: ViewHolder<T, R>) {
    super.onViewAttachedToWindow(holder)
    holder.attach()
    holder.disposable.addAll(holder.itemEvent.subscribe { itemEventSource.onNext(it) })
  }

  override fun onViewDetachedFromWindow(holder: ViewHolder<T, R>) {
    holder.disposable.clear()
    super.onViewDetachedFromWindow(holder)
  }

  override fun getItemViewType(position: Int): Int = getItem(position)?.layoutRes ?: super.getItemViewType(position)

  override fun getItemId(position: Int): Long = getItem(position)?.id ?: super.getItemId(position)

}

package com.vander.scaffold.ui.widget.adapter

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor

abstract class ViewHolder<in T : AdapterModel, R>(root: View) : RecyclerView.ViewHolder(root) {
  val disposable = CompositeDisposable()
  val itemEvent: PublishProcessor<R> = PublishProcessor.create<R>()
  val context: Context
    get() = itemView.context

  abstract fun bind(item: T)

  abstract fun attach()
}
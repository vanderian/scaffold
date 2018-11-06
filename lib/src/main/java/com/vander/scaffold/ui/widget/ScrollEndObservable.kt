package com.vander.scaffold.ui.widget

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.vander.scaffold.ui.checkMainThread
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

internal class ScrollEndObservable(private val view: RecyclerView, private val threshold: Int) : Observable<Unit>() {

  override fun subscribeActual(observer: Observer<in Unit>) {
    if (!checkMainThread(observer)) {
      return
    }
    val listener = Listener(view, observer, threshold)
    observer.onSubscribe(listener)
    view.addOnScrollListener(listener.listener)
  }

  internal class Listener(
      private val recyclerView: RecyclerView,
      private val observer: Observer<in Unit>,
      private val threshold: Int
  ) : MainThreadDisposable() {

    val listener = object : RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (!isDisposed) {
          val last = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
          if (recyclerView.layoutManager!!.itemCount - threshold < last) recyclerView.post { observer.onNext(Unit) }
        }
      }
    }

    override fun onDispose() {
      recyclerView.clearOnScrollListeners()
    }
  }
}

fun RecyclerView.onScrollEnd(threshold: Int = 10): Observable<Unit> = ScrollEndObservable(this, threshold)

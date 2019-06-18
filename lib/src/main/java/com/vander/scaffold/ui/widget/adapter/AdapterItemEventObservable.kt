package com.vander.scaffold.ui.widget.adapter

import io.reactivex.processors.PublishProcessor

interface AdapterItemEventObservable<R> {
  val itemEventSource: PublishProcessor<R>
}
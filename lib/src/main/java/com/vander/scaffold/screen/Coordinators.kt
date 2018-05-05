package com.vander.scaffold.screen

import android.view.View
import com.vander.scaffold.R

object Coordinators {

  fun getCoordinator(view: View): Coordinator? {
    return view.getTag(R.id.coordinator) as? Coordinator
  }

  fun add(view: View, provider: CoordinatorProvider) {
    provider.provideCoordinator(view)?.let {
      it.attach(view)
      view.setTag(R.id.coordinator, it)
    }
  }

  fun remove(view: View) {
    if (view.getTag(R.id.coordinator) != null) view.setTag(R.id.coordinator, null)
  }
}

fun View.getCoordinator() = Coordinators.getCoordinator(this)
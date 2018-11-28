package com.vander.scaffold.debugyzer.log

import timber.log.Timber

/**
 * @author marian on 24.1.2017.
 */

class ReleaseTree(private val priority: () -> Int) : Timber.DebugTree() {
  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    if (priority >= priority()) {
      super.log(priority, tag, message, t)
    }
  }
}

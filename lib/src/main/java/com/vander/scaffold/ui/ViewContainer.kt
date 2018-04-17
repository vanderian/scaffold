package com.vander.scaffold.ui

import android.app.Activity
import android.view.ViewGroup

/**
 * An indirection which allows controlling the root container used for each activity.
 */
interface ViewContainer {
  /**
   * The root [ViewGroup] into which the activity should place its contents.
   */
  operator fun get(activity: Activity): ViewGroup

  companion object {

    /**
     * An [ViewContainer] which returns the normal activity content view.
     */
    val DEFAULT = object : ViewContainer {
      override fun get(activity: Activity): ViewGroup = activity.findViewById<ViewGroup>(android.R.id.content)
    }
  }
}

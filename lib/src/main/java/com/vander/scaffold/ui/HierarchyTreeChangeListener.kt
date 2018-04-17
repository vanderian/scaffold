package com.vander.scaffold.ui

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

/**
 * A [hierarchy change listener][ViewGroup.OnHierarchyChangeListener] which recursively
 * monitors an entire tree of views.
 */
class HierarchyTreeChangeListener private constructor(
    private val delegate: ViewGroup.OnHierarchyChangeListener
) : ViewGroup.OnHierarchyChangeListener {

  override fun onChildViewAdded(parent: View, child: View) {
    delegate.onChildViewAdded(parent, child)

    if (child is ViewGroup && child !is RecyclerView) {
      val childGroup = child
      childGroup.setOnHierarchyChangeListener(this)
      for (i in 0..childGroup.childCount - 1) {
        onChildViewAdded(childGroup, childGroup.getChildAt(i))
      }
    }
  }

  override fun onChildViewRemoved(parent: View, child: View) {
    if (child is ViewGroup) {
      val childGroup = child
      for (i in 0..childGroup.childCount - 1) {
        onChildViewRemoved(childGroup, childGroup.getChildAt(i))
      }
      childGroup.setOnHierarchyChangeListener(null)
    }

    delegate.onChildViewRemoved(parent, child)
  }

  companion object {
    /**
     * Wrap a regular [hierarchy change listener][ViewGroup.OnHierarchyChangeListener] with one
     * that monitors an entire tree of views.
     */
    fun wrap(delegate: ViewGroup.OnHierarchyChangeListener): HierarchyTreeChangeListener =
        HierarchyTreeChangeListener(delegate)
  }
}

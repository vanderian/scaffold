package com.vander.scaffold.ui

import android.support.transition.Transition
import flow.TraversalCallback

/**
 * @author marian on 5.9.2017.
 */
class TransitionCompletedListener(private val callback: TraversalCallback) : Transition.TransitionListener {
  override fun onTransitionEnd(transition: Transition) = callback.onTraversalCompleted()
  override fun onTransitionResume(transition: Transition) {}
  override fun onTransitionPause(transition: Transition) {}
  override fun onTransitionCancel(transition: Transition) = callback.onTraversalCompleted()
  override fun onTransitionStart(transition: Transition) {}
}
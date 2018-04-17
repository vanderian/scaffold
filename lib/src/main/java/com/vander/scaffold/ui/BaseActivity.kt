package com.vander.scaffold.ui

import android.content.Context
import android.os.Bundle
import android.support.transition.AutoTransition
import android.support.transition.Scene
import android.support.transition.TransitionManager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.coordinators.CoordinatorProvider
import com.squareup.coordinators.Coordinators
import flow.*
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {

  @Inject lateinit var viewContainer: ViewContainer
  @Inject lateinit var coordinatorProvider: CoordinatorProvider
  private lateinit var container: ViewGroup

  protected abstract fun defaultKey(): LayoutKey

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // calls setContentView()
    container = viewContainer[this]
    container.setOnHierarchyChangeListener(HierarchyTreeChangeListener.wrap(object : ViewGroup.OnHierarchyChangeListener {
      override fun onChildViewAdded(parent: View, child: View) {
        Coordinators.bind(child, coordinatorProvider)
      }

      override fun onChildViewRemoved(parent: View, child: View) {}
    }))
  }

  public override fun attachBaseContext(newBase: Context) =
      super.attachBaseContext(
          Flow.configure(newBase, this)
              .dispatcher(KeyDispatcher.configure(this, Changer()).build())
              .keyParceler(KeyParceler())
              .defaultKey(defaultKey())
              .install()
      )

  override fun onBackPressed() {
    if (Flow.get(this).goBack()) {
      return
    }
    super.onBackPressed()
  }

  private inner class Changer : flow.KeyChanger {
    override fun changeKey(
        outgoingState: State?, incomingState: State,
        direction: Direction,
        incomingContexts: MutableMap<Any, Context>,
        callback: TraversalCallback
    ) {

      val currentView = container.getChildAt(0)
      outgoingState?.apply { save(currentView) }

      val key = incomingState.getKey<LayoutKey>()
      val ctx = incomingContexts[key]

      // avoid bootstrap on resume since coordinator is already attached
      if (currentView != null && Flow.getKey<LayoutKey>(currentView) == key) {
        callback.onTraversalCompleted()
        return
      }

      val view = LayoutInflater.from(ctx).inflate(key.layout(), container, false)
      incomingState.restore(view)
      val scene = Scene(container, view)
      val transition = AutoTransition()
      transition.addListener(TransitionCompletedListener(callback))
      TransitionManager.go(scene, transition)
    }
  }
}
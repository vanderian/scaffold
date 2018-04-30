package com.vander.scaffold.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.squareup.coordinators.CoordinatorProvider
import com.squareup.coordinators.Coordinators
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import com.vander.scaffold.Injectable
import com.vander.scaffold.R
import javax.inject.Inject

abstract class FragmentActivity : AppCompatActivity(), HasSupportFragmentInjector, Injectable {

  @Inject lateinit var viewContainer: ViewContainer
  @Inject lateinit var coordinatorProvider: CoordinatorProvider
  @Inject lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
  private lateinit var container: ViewGroup

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // calls setContentView()
    container = viewContainer[this]
    container.setOnHierarchyChangeListener(HierarchyTreeChangeListener.wrap(object : ViewGroup.OnHierarchyChangeListener {
      override fun onChildViewAdded(parent: View, child: View) {
        Coordinators.bind(child, coordinatorProvider)

        coordinatorProvider.provideCoordinator(child)?.let {
          it.attach(child)
          child.setTag(com.squareup.coordinators.R.id.coordinator, it)
        }
      }

      override fun onChildViewRemoved(parent: View, child: View) {}
    }))
    val frame = FrameLayout(this)
    frame.id = R.id.container_id
    container.addView(frame)
  }

  override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentInjector
}
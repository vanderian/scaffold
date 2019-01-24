package com.vander.scaffold.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.vander.scaffold.Injectable
import com.vander.scaffold.R
import com.vander.scaffold.screen.CoordinatorProvider
import com.vander.scaffold.screen.Coordinators
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
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
        Coordinators.add(child, coordinatorProvider)
      }

      override fun onChildViewRemoved(parent: View, child: View) {
        Coordinators.remove(child)
      }
    }))
    container.addView(FrameLayout(this).apply { id = R.id.container_id })
  }

  override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentInjector

  override fun onBackPressed() {
    if (BackSupport.handlesBack(supportFragmentManager.findFragmentById(R.id.container_id)).not()) {
      super.onBackPressed()
    }
  }
}
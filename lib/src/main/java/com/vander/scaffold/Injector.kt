package com.vander.scaffold

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import com.vander.scaffold.ui.ActivityHierarchyServer

/**
 * @author marian on 21.9.2017.
 */
object Injector : ActivityHierarchyServer.Empty() {
  override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    handleActivity(activity)
  }

  private fun handleActivity(activity: Activity) {
    if (activity is Injectable) {
      AndroidInjection.inject(activity)
    }
    (activity as? FragmentActivity)?.supportFragmentManager?.registerFragmentLifecycleCallbacks(
        object : FragmentManager.FragmentLifecycleCallbacks() {
          override fun onFragmentPreCreated(fm: FragmentManager?, f: Fragment?, savedInstanceState: Bundle?) {
            if (f is Injectable) AndroidSupportInjection.inject(f)
          }
        }, true)
  }

}
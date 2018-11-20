package com.vander.scaffold.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import timber.log.Timber
import java.util.*

/**
 * A "view server" adaptation which automatically hooks itself up to all activities.
 */
interface ActivityHierarchyServer : Application.ActivityLifecycleCallbacks {

  class Proxy : ActivityHierarchyServer {
    private val servers = ArrayList<ActivityHierarchyServer>()

    fun addServer(server: ActivityHierarchyServer) = servers.add(server)

    fun removeServer(server: ActivityHierarchyServer) = servers.remove(server)

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) =
        servers.forEach { it.onActivityCreated(activity, savedInstanceState) }

    override fun onActivityStarted(activity: Activity) =
        servers.forEach { it.onActivityStarted(activity) }

    override fun onActivityResumed(activity: Activity) =
        servers.forEach { it.onActivityResumed(activity) }

    override fun onActivityPaused(activity: Activity) =
        servers.forEach { it.onActivityPaused(activity) }

    override fun onActivityStopped(activity: Activity) =
        servers.forEach { it.onActivityStopped(activity) }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) =
        servers.forEach { it.onActivitySaveInstanceState(activity, outState) }

    override fun onActivityDestroyed(activity: Activity) =
        servers.forEach { it.onActivityDestroyed(activity) }
  }

  open class Empty : ActivityHierarchyServer {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}
    override fun onActivityDestroyed(activity: Activity) {}
  }

  open class Debug : ActivityHierarchyServer {
    override fun onActivityPaused(p0: Activity) = Timber.d("onActivityPaused $p0")
    override fun onActivityResumed(p0: Activity) = Timber.d("onActivityResumed $p0")
    override fun onActivityStarted(p0: Activity) = Timber.d("onActivityStarted $p0")
    override fun onActivityDestroyed(p0: Activity) = Timber.d("onActivityDestroyed $p0")
    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle?) = Timber.d("onActivitySaveInstanceState $p0 $p1")
    override fun onActivityStopped(p0: Activity) = Timber.d("onActivityStopped $p0")
    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
      Timber.d("onActivityCreated $p0 $p1")
      (p0 as? AppCompatActivity)?.supportFragmentManager
          ?.registerFragmentLifecycleCallbacks(FragmentLifeCycleDebug(), true)
    }
  }

  class FragmentLifeCycleDebug : FragmentManager.FragmentLifecycleCallbacks() {
    override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
      Timber.d("onFragmentViewCreated $f $v $savedInstanceState")
    }

    override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
      Timber.d("onFragmentStopped $f")
    }

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
      Timber.d("onFragmentCreated $f $savedInstanceState")
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
      Timber.d("onFragmentResumed $f")
    }

    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
      Timber.d("onFragmentAttached $f $context")
    }

    override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) {
      Timber.d("onFragmentPreAttached $f $context")
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
      Timber.d("onFragmentDestroyed $f")
    }

    override fun onFragmentSaveInstanceState(fm: FragmentManager, f: Fragment, outState: Bundle) {
      Timber.d("onFragmentSaveInstanceState $f")
    }

    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
      Timber.d("onFragmentStarted $f")
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
      Timber.d("onFragmentViewDestroyed $f")
    }

    override fun onFragmentPreCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
      Timber.d("onFragmentPreCreated $f")
    }

    override fun onFragmentActivityCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
      Timber.d("onFragmentActivityCreated $f")
    }

    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
      Timber.d("onFragmentPaused $f")
    }

    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
      Timber.d("onFragmentDetached $f")
    }
  }

  companion object {
    /**
     * An [ActivityHierarchyServer] which does nothing.
     */
    val NONE: ActivityHierarchyServer = Empty()
  }
}

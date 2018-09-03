package com.vander.scaffold

import android.os.Looper
import com.vander.scaffold.ui.ActivityHierarchyServer
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import javax.inject.Inject

abstract class BaseApp : DaggerApplication() {
  @Inject lateinit var activityHierarchyServer: Set<@JvmSuppressWildcards ActivityHierarchyServer>

  protected abstract fun buildComponentAndInject()

  override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
    return AndroidInjector { buildComponentAndInject() }
  }

  override fun onCreate() {
    super.onCreate()

    registerActivityLifecycleCallbacks(ActivityHierarchyServer.Proxy().apply {
      activityHierarchyServer.forEach { addServer(it) }
      addServer(Injector)
    })
  }

  fun setupRx() {
    RxJavaPlugins.setErrorHandler { Timber.e(it, "Uncaught RxJava error") }
    RxAndroidPlugins.initMainThreadScheduler { AndroidSchedulers.from(Looper.getMainLooper(), true) }
  }
}

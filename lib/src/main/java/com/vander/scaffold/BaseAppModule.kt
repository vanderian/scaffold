package com.vander.scaffold

import android.app.Application
import android.content.Context
import android.content.res.Resources
import com.vander.scaffold.annotations.ApplicationScope
import com.vander.scaffold.screen.ScreenModelModule
import com.vander.scaffold.ui.ActivityHierarchyServer
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjectionModule
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.AndroidSupportInjectionModule
import dagger.multibindings.ElementsIntoSet

@Module(
    includes = [
      AndroidInjectionModule::class,
      AndroidSupportInjectionModule::class,
      ScreenModelModule::class
    ]
)
class BaseAppModule(private val app: Application) {

  @Provides @ApplicationScope @ElementsIntoSet
  fun providesEmptyActivityHierarchyServers(): Set<ActivityHierarchyServer> = emptySet()

  @Provides @ApplicationScope fun provideApplication(): Application = app

  @Provides @ApplicationScope fun provideApplicationContext(): Context = app.applicationContext

  @Provides @ApplicationScope fun provideResources(): Resources = app.resources



}

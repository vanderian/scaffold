package com.vander.scaffold.screen

import com.vander.scaffold.annotations.ActivityScope
import com.squareup.coordinators.Coordinator
import com.squareup.coordinators.CoordinatorProvider
import com.squareup.coordinators.R
import dagger.Module
import dagger.Provides
import dagger.multibindings.Multibinds
import javax.inject.Provider


@Module(includes = [(CoordinatorModule.MapModule::class)])
object CoordinatorModule {

  @JvmStatic @Provides @ActivityScope fun providesCoordinatorProvider(
      coordinatorMap: Map<Class<out Coordinator>, @JvmSuppressWildcards Provider<Coordinator>>
  ): CoordinatorProvider = CoordinatorProvider { view ->
    (view.tag as? String)?.apply {
      try {
        val clazz = Class.forName(this)
        if (clazz in coordinatorMap) {
          return@CoordinatorProvider coordinatorMap[clazz]!!.get()
        }
      } catch (e: ClassNotFoundException) {
        throw IllegalStateException("No coordinator class available for " + this)
      }
      throw IllegalStateException("Dagger multibinds map contains no coordinator for " + view)
    }
    null
  }

  @Module
  abstract class MapModule {
    @Multibinds abstract fun provideMap(): Map<Class<out Coordinator>, Coordinator>
  }
}

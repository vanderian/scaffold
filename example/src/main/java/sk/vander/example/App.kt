package sk.vander.example

import android.arch.lifecycle.ViewModel
import autodagger.AutoComponent
import autodagger.AutoInjector
import com.vander.scaffold.BaseApp
import com.vander.scaffold.BaseAppModule
import com.vander.scaffold.annotations.ActivityScope
import com.vander.scaffold.annotations.ApplicationScope
import com.vander.scaffold.annotations.ScreenScope
import com.vander.scaffold.annotations.ViewModelKey
import com.vander.scaffold.screen.CoordinatorModule
import com.vander.scaffold.ui.ViewContainer
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import timber.log.Timber

@AutoComponent(
    modules = [(AppModule::class), (AppModule.Ui::class)]
)
@AutoInjector
@ApplicationScope
class App : BaseApp() {
  override fun onCreate() {
    super.onCreate()
    Timber.plant(Timber.DebugTree())
  }

  override fun buildComponentAndInject() =
      DaggerAppComponent.builder().baseAppModule(BaseAppModule(this)).build().inject(this)
}

@Module(includes = [BaseAppModule::class])
object AppModule {

  @Module
  abstract class Ui {
    @ActivityScope @ContributesAndroidInjector(modules = [CoordinatorModule::class, Screens::class])
    abstract fun contributeMainActivity(): MainActivity

    @Binds @IntoMap @ViewModelKey(FooModel::class)
    abstract fun provideFooModel(viewModel: FooModel): ViewModel
  }

  @Module
  abstract class Screens {
    @ScreenScope @ContributesAndroidInjector()
    abstract fun contributeFooScreen(): FooScreen
  }

  @JvmStatic @Provides @ApplicationScope
  fun providesViewContainer(): ViewContainer = ViewContainer.DEFAULT
}

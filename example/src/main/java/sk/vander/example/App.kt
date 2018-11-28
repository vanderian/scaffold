package sk.vander.example

import android.arch.lifecycle.ViewModel
import android.content.Context
import autodagger.AutoComponent
import autodagger.AutoInjector
import com.vander.scaffold.BaseApp
import com.vander.scaffold.BaseAppModule
import com.vander.scaffold.annotations.*
import com.vander.scaffold.debugyzer.bugreport.BugReportContainer
import com.vander.scaffold.debugyzer.bugreport.ReportData
import com.vander.scaffold.screen.Coordinator
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
    setupRx()
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

    @Binds @IntoMap @ClassKeyCoordinator(FooCoordinator::class)
    abstract fun provideFooCoordinator(coordinator: FooCoordinator): Coordinator
  }

  @Module
  abstract class Screens {
    @ScreenScope @ContributesAndroidInjector()
    abstract fun contributeFooScreen(): FooScreen
  }

  @JvmStatic @Provides @ApplicationScope
  fun providesViewContainer(ctx: Context): ViewContainer = BugReportContainer(ctx, ReportData("a@a.a", "v1", 10), { true })
}

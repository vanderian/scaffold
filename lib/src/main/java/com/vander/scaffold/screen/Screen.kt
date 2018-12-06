package com.vander.scaffold.screen

import android.app.Activity
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import com.vander.scaffold.*
import com.vander.scaffold.debug.log
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * @author marian on 20.9.2017.
 */
abstract class Screen<U : Screen.State, out V : Screen.Intents>(
    private val clazz: KClass<out ScreenModel<U, V>>? = null
) : Fragment(), Injectable {

  interface State
  interface Intents {
    fun events(): List<Observable<*>> = emptyList()
  }

  private var result = BehaviorSubject.create<Result>()
  private lateinit var model: ScreenModel<U, V>
  private val onEvent: PublishSubject<Event> = PublishSubject.create()
  private val disposable = CompositeDisposable()
  @Inject lateinit var modelFactory: ViewModelProvider.Factory

  protected open val hasNavController = true

  val state: U
    get() = model.stateValue

  @LayoutRes abstract fun layout(): Int
  abstract fun intents(): V
  abstract fun render(state: U)

  protected open fun interceptEvent(it: Event): Event = it

  private fun checkStartFinish(intent: Intent, finish: Boolean = false, code: Int = 0, withResult: Boolean = false) {
    if (intent.resolveActivity(context!!.packageManager) != null) {
      if (withResult) startActivityForResult(intent, code) else startActivity(intent)
    } else {
      Toast.makeText(context, context!!.getString(R.string.no_app_error, intent), Toast.LENGTH_SHORT).show()
      result.onNext(Result(code))
    }
    if (finish) activity?.finish()
  }
  private fun navHost(id: Int?) = id?.let { childFragmentManager.findFragmentById(it) ?: fragmentManager?.findFragmentById(it) }

  private fun navController(id: Int? = null) = (navHost(id)?.findNavController() ?: findNavController())

  private fun navigate(navigation: NavEvent) {
    when (navigation) {
      GoBack -> activity!!.onBackPressed()
      is GoUp -> navController(navigation.childNavHostId).navigateUp()
      is PopStack -> navController(navigation.childNavHostId).popBackStack()
      is PopWithResult -> navController().run {
        val id = currentDestination!!.id
        popBackStack()
        currentDestination?.addResult(Result(id, navigation.success, navigation.extras))
      }
      is NextActivity -> checkStartFinish(navigation.intent, navigation.finish)
      is NextActivityExplicit -> checkStartFinish(Intent(context, navigation.clazz.java).apply(navigation.intentBuilder), navigation.finish)
      is WithResult -> checkStartFinish(navigation.intent, code = navigation.requestCode, withResult = true)
      is WithResultExplicit -> checkStartFinish(Intent(context, navigation.clazz.java).apply(navigation.intentBuilder), code = navigation.requestCode, withResult = true)
      is NavDirection -> navController(navigation.childNavHostId).navigate(navigation.action, navigation.args, navigation.navOptions, navigation.extras)
    }
  }

  fun <T : Event> event(clazz: KClass<T>): Observable<T> = onEvent.ofType(clazz.java)

  fun NavDestination.addResult(result: Result) = addDefaultArguments(result.bundle(RESULT))

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val c = clazz?.java ?: Class.forName(javaClass.name.replace("Screen", "Model")) as Class<ScreenModel<U, V>>
    model = ViewModelProviders.of(this, modelFactory)[c]
    model.args = arguments ?: Bundle.EMPTY
    if (hasNavController) {
      model.args.putInt(ACTION_ID, findNavController().currentDestination!!.id)
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
      inflater.inflate(layout(), container, false)

  override fun onStart() {
    super.onStart()
    disposable.addAll(
        model.state.log("screen state").switchToMainIfOther().subscribe { render(it) },
        model.event.log("screen event").switchToMainIfOther()
            .map { interceptEvent(it) }
            .subscribe {
              when (it) {
                is NavEvent -> navigate(it)
                is ToastEvent -> Toast.makeText(context, if (it.msgRes == -1) it.msg else context!!.getString(it.msgRes), it.length).show()
                else -> onEvent.onNext(it)
              }
            },
        model.collect(intents(), result)
    )
    if (hasNavController) {
      findNavController().currentDestination?.run {
        defaultArguments.unbundleOptional<Result>(RESULT)?.run { result(this) }
        defaultArguments.remove(RESULT)
      }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    result.onNext(Result(requestCode, resultCode == Activity.RESULT_OK, data?.extras))
  }

  override fun onStop() {
    disposable.clear()
    result = BehaviorSubject.create()
    super.onStop()
  }

  fun result(result: Result) {
    this.result.onNext(result)
  }

  companion object {
    const val ACTION_ID = "arg_action_id"
    const val RESULT = "extra_result"

    fun actionId(args: Bundle) = args.getInt(ACTION_ID)
  }
}

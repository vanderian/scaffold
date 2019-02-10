package com.vander.scaffold.screen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import com.vander.scaffold.Injectable
import com.vander.scaffold.R
import com.vander.scaffold.debug.log
import com.vander.scaffold.navArgs
import com.vander.scaffold.switchToMainIfOther
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * @author marian on 20.9.2017.
 */
abstract class Screen<U : Screen.State, V : Screen.Intents>(
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

  /**
   * should be called after onActivityCreated
   */
  val destinationId: Int
    get() = destinationId(model.args)

  @LayoutRes abstract fun layout(): Int
  abstract fun intents(): V
  abstract fun render(state: U)

  private fun checkStartFinish(intent: Intent, finish: Boolean = false, code: Int = 0, withResult: Boolean = false) {
    if (intent.resolveActivity(context!!.packageManager) != null) {
      if (withResult) startActivityForResult(intent, code) else startActivity(intent)
    } else {
      Toast.makeText(context, context!!.getString(R.string.no_app_error, intent), Toast.LENGTH_SHORT).show()
      result.onNext(Result(code))
    }
    if (finish) activity?.finish()
  }

  private fun navController() = findNavController()

  private fun navigate(navigation: NavEvent) {
    when (navigation) {
      is GoBack -> activity!!.onBackPressed()
      is GoUp -> navController().navigateUp()
      is PopStack -> navController().popBackStack()
      is PopStackTo -> navController().popBackStack(navigation.destination, navigation.inclusive)
      is PopWithResult -> navController().run {
        val id = currentDestination!!.id
        popBackStack()
        currentDestination?.addResult(Result(id, navigation.success, navigation.extras))
      }
      is NextActivity -> checkStartFinish(navigation.intent, navigation.finish)
      is NextActivityExplicit -> checkStartFinish(Intent(context, navigation.clazz.java).apply(navigation.intentBuilder), navigation.finish)
      is WithResult -> checkStartFinish(navigation.intent, code = navigation.requestCode, withResult = true)
      is WithResultExplicit -> checkStartFinish(Intent(context, navigation.clazz.java).apply(navigation.intentBuilder), code = navigation.requestCode, withResult = true)
      is NavDirection -> navController().navigate(navigation.action, navigation.args, navigation.navOptions, navigation.extras)
    }
  }

  fun <T : Event> event(clazz: KClass<T>): Observable<T> = onEvent.ofType(clazz.java)

  fun NavDestination.addResult(result: Result) = addArgument(RESULT, result.navArgs())

  @Suppress("UNCHECKED_CAST")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val c = clazz?.java ?: Class.forName(javaClass.name.replace("Screen", "Model")) as Class<ScreenModel<U, V>>
    model = ViewModelProviders.of(this, modelFactory)[c]
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    model.args = arguments ?: Bundle()
    if (hasNavController && !model.args.containsKey(DEST_ID)) {
      model.args.putInt(DEST_ID, findNavController().currentDestination!!.id)
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
      inflater.inflate(layout(), container, false)

  override fun onStart() {
    super.onStart()
    disposable.addAll(
        model.state.log("${this.javaClass.simpleName} state").switchToMainIfOther().subscribe { render(it) },
        model.event.log("${this.javaClass.simpleName} event").switchToMainIfOther().subscribe {
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
        (arguments[RESULT]?.defaultValue as? Result)?.run { result(this) }
        arguments -= RESULT
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
    const val DEST_ID = "arg_destination_id"
    const val RESULT = "extra_result"

    fun destinationId(args: Bundle) = args.getInt(DEST_ID)
  }
}

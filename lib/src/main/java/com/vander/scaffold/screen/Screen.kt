package com.vander.scaffold.screen

import android.app.Activity
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import butterknife.ButterKnife
import butterknife.Unbinder
import com.vander.scaffold.Injectable
import com.vander.scaffold.R
import com.vander.scaffold.debug.log
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.AsyncSubject
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
  private lateinit var unbind: Unbinder
  private lateinit var model: ScreenModel<U, V>
  private val onEvent: PublishSubject<Event> = PublishSubject.create()
  private val disposable = CompositeDisposable()
  @Inject lateinit var modelFactory: ViewModelProvider.Factory

  val state: U
    get() = model.state.value

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

  private fun navigate(navigation: Navigation) {
    when (navigation) {
      GoBack -> activity!!.onBackPressed()
      is NextScreen -> (if (navigation.fragmentsManager) fragmentManager else activity?.supportFragmentManager)!!.beginTransaction()
          .replace(navigation.id, navigation.screen)
          .addToBackStack("")
          .commit()
      is NextScreenResult -> {
        navigation.screen.setTargetFragment(this, navigation.requestCode)
        (if (navigation.fragmentsManager) fragmentManager else activity?.supportFragmentManager)!!.beginTransaction()
            .replace(navigation.id, navigation.screen)
            .addToBackStack("")
            .commit()
      }
      is NextChildScreen -> {
        childFragmentManager.beginTransaction()
            .replace(navigation.id, navigation.screen)
            .addToBackStack("")
            .commit()
      }
      is NextActivity -> checkStartFinish(navigation.intent, navigation.finish)
      is NextActivityExplicit -> checkStartFinish(Intent(context, navigation.clazz.java).apply(navigation.intentBuilder), navigation.finish)
      is WithResult -> checkStartFinish(navigation.intent, code = navigation.requestCode, withResult = true)
      is WithResultExplicit -> checkStartFinish(Intent(context, navigation.clazz.java).apply(navigation.intentBuilder), code = navigation.requestCode, withResult = true)
    }
  }

  protected fun <T : Parcelable> setArgument(obj: T) {
    arguments = Bundle().apply { putParcelable(ARG_OBJ, obj) }
  }

  protected fun setId(id: Long) {
    arguments = Bundle().apply { putLong(ARG_ID, id) }
  }

  fun <T : Event> event(clazz: KClass<T>): Observable<T> = onEvent.ofType(clazz.java)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val c = clazz?.java ?: Class.forName(javaClass.name.replace("Screen", "Model")) as Class<ScreenModel<U, V>>
    model = ViewModelProviders.of(this, modelFactory)[c]
    model.args = arguments ?: Bundle.EMPTY
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
      inflater.inflate(layout(), container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    unbind = ButterKnife.bind(this, view)
  }

  override fun onStart() {
    super.onStart()
    disposable.addAll(
        model.state.log("screen state").subscribe { render(it) },
        model.event.log("screen event").subscribe {
          when (it) {
            is Navigation -> navigate(it)
            is ToastEvent -> Toast.makeText(context, if (it.msgRes == -1) it.msg else context!!.getString(it.msgRes), it.length).show()
            else -> onEvent.onNext(it)
          }
        },
        model.collect(intents(), result)
    )
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    result.onNext(Result(requestCode, resultCode == Activity.RESULT_OK, data))
  }

  override fun onStop() {
    disposable.clear()
    result = BehaviorSubject.create()
    super.onStop()
  }

  fun result(result: Result) {
    this.result.onNext(result)
  }

  fun goBack() {
    navigate(GoBack)
  }

  companion object {
    const val ARG_OBJ = "arg_object"
    const val ARG_ID = "arg_id"
    const val JSON_EXTRA = "json_extra"

    fun <T : Parcelable> getArgument(args: Bundle): T = args.getParcelable(ARG_OBJ)
    fun getId(args: Bundle): Long = args.getLong(ARG_ID)
  }
}

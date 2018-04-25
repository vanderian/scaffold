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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * @author marian on 20.9.2017.
 */
abstract class Screen<in U : Screen.State, out V : Screen.Intents>(
    private val clazz: KClass<out ScreenModel<U, V>> = Class.forName(this::class.java.name.replace("Screen", "Model")).kotlin as KClass<ScreenModel<U,V>>
) : Fragment(), Injectable {

  interface State
  interface Intents {
    fun events(): List<Observable<*>> = emptyList()
  }

  private val result = BehaviorSubject.create<Result>()
  private lateinit var unbind: Unbinder
  private lateinit var model: ScreenModel<U, V>
  private val onEvent: PublishSubject<Event> = PublishSubject.create()
  protected val disposable = CompositeDisposable()
  @Inject lateinit var modelFactory: ViewModelProvider.Factory

  @LayoutRes abstract fun layout(): Int
  abstract fun intents(): V
  abstract fun render(state: U)

  private fun navigate(navigation: Navigation) {
    when (navigation) {
      GoBack -> activity!!.onBackPressed()
      is NextScreen -> activity!!.supportFragmentManager.beginTransaction()
          .replace(R.id.container_id, navigation.screen)
          .addToBackStack("")
          .commit()
      is NextScreenResult -> {
        navigation.screen.setTargetFragment(this, navigation.requestCode)
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.container_id, navigation.screen)
            .addToBackStack("")
            .commit()
      }
      is NextActivity -> {
        startActivity(Intent(context, navigation.clazz.java))
        if (navigation.finish) activity?.finish()
      }
      is ExtActivity -> startActivity(navigation.intent)
      is WithResult ->
        if (navigation.intent.resolveActivity(context!!.packageManager) != null) {
          startActivityForResult(navigation.intent, navigation.requestCode)
        } else {
          Toast.makeText(context, context!!.getString(R.string.no_app_error, navigation.intent), Toast.LENGTH_SHORT).show()
          result.onNext(Result(navigation.requestCode))
        }
    }
  }

  protected fun <T : Parcelable> setArgument(obj: T) {
    arguments = Bundle().apply { putParcelable(ARG_OBJ, obj) }
  }

  fun <T : Event> event(clazz: KClass<T>): Observable<T> = onEvent.ofType(clazz.java)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    model = ViewModelProviders.of(this, modelFactory)[clazz.java]
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
        model.state.log("screen state").observeOn(AndroidSchedulers.mainThread()).subscribe { render(it) },
        model.event.log("screen event").observeOn(AndroidSchedulers.mainThread()).subscribe {
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
    result.onNext(Result(requestCode, resultCode == Activity.RESULT_OK, data?.extras?.getString(JSON_EXTRA) ?: ""))
  }

  override fun onStop() {
    disposable.clear()
    super.onStop()
  }

  fun <T : Parcelable> getArgument(): T? = arguments?.getParcelable(ARG_OBJ)

  fun result(result: Result) {
    this.result.onNext(result)
  }

  fun goBack() {
    navigate(GoBack)
  }

  companion object {
    const val ARG_OBJ = "arg_object"
    const val ARG_STATE = "arg_state"
    const val JSON_EXTRA = "json_extra"
  }
}

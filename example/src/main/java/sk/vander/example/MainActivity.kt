package sk.vander.example

import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import com.jakewharton.rxbinding2.view.clicks
import com.squareup.coordinators.Coordinator
import com.squareup.coordinators.Coordinators
import com.vander.scaffold.form.Form
import com.vander.scaffold.form.FormData
import com.vander.scaffold.form.FormIntents
import com.vander.scaffold.form.FormResult
import com.vander.scaffold.form.validator.*
import com.vander.scaffold.screen.BaseCoordinator
import com.vander.scaffold.screen.Result
import com.vander.scaffold.screen.Screen
import com.vander.scaffold.screen.ScreenModel
import com.vander.scaffold.ui.FragmentActivity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class MainActivity : FragmentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
          .replace(R.id.container_id, FooScreen())
          .commit()
    }
  }
}

data class FooState(
    val text: String,
    val formData: FormData = emptyMap()
) : Screen.State

interface FooIntents : FormIntents {
  fun submit(): Observable<FormResult>
}

class FooModel @Inject constructor() : ScreenModel<FooState, FooIntents>() {
  override fun collectIntents(intents: FooIntents, result: Observable<Result>): Disposable {
    state.init(FooState("hello"))

    return CompositeDisposable(
        intents.state().subscribe { state.next { copy(formData = it) } },
        intents.submit().subscribe { }
    )
  }
}

class FooScreen : Screen<FooState, FooIntents>() {
  @BindView(R.id.text) lateinit var text: TextView
  @BindView(R.id.view_complex) lateinit var complex: View
  @BindView(R.id.input_first) lateinit var input1: TextInputLayout
  @BindView(R.id.input_second) lateinit var input2: TextInputLayout

  private lateinit var form: Form
  private val coordinator
    get() = Coordinators.getCoordinator(complex) as FooCoordinator


  override fun layout(): Int = R.layout.activity_main

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    form = Form.init(
        Validation(input1, NotEmptyRule(R.string.error_empty), EmailRule(R.string.error_email)),
        Validation(input2, NotEmptyRule(R.string.error_empty))
    )
  }

  override fun intents(): FooIntents = object : FooIntents {
    override val form: Form = this@FooScreen.form
    override fun submit(): Observable<FormResult> = complex.clicks()
        .flatMapSingle {
          form.with(Validation(input2, ValueCheckRule("bar", { getString(R.string.error_no_match, "bar", it) })))
              .validate()
        }
  }

  override fun render(state: FooState) {
    text.text = state.text
    form.restore(state.formData)
  }

}

class FooCoordinator @Inject constructor() : BaseCoordinator() {
  @BindView(R.id.submit) lateinit var submit: Button

  override fun attach(view: View) {
    super.attach(view)
    Toast.makeText(view.context, "attach", Toast.LENGTH_SHORT).show()
  }

  override fun detach(view: View) {
    Toast.makeText(view.context, "detach", Toast.LENGTH_SHORT).show()
    super.detach(view)
  }

  fun clicks(): Observable<Unit> = submit.clicks()
}
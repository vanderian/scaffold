package sk.vander.example

import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import com.jakewharton.rxbinding2.view.clicks
import com.vander.scaffold.form.Form
import com.vander.scaffold.form.FormData
import com.vander.scaffold.form.FormIntents
import com.vander.scaffold.form.FormResult
import com.vander.scaffold.form.validator.EmailRule
import com.vander.scaffold.form.validator.NotEmptyRule
import com.vander.scaffold.form.validator.ValidateRule
import com.vander.scaffold.form.validator.Validation
import com.vander.scaffold.screen.*
import com.vander.scaffold.ui.FragmentActivity
import com.vander.scaffold.ui.HandlesBack
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.Callable
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

class ValueCheckRule(val value: String, override val errorMessage: (String) -> String) : ValidateRule() {
  override fun validate(text: String?): Boolean = text == value
}

data class FooState(
    val text: String,
    val formData: FormData = emptyMap()
) : Screen.State

interface FooIntents : FormIntents {
  fun submit(): Observable<FormResult>
  fun back(): Observable<Unit>
}

class FooModel @Inject constructor() : ScreenModel<FooState, FooIntents>() {
  override fun collectIntents(intents: FooIntents, result: Observable<Result>): Disposable {
    state.init(FooState("hello"))

    return CompositeDisposable(
        intents.formState().subscribe { state.next { copy(formData = it) } },
        intents.submit().subscribe { },
        intents.back().subscribe { state.next { copy(text = "on back") } })
  }
}

class FooScreen : Screen<FooState, FooIntents>(), HandlesBack {
  @BindView(R.id.text) lateinit var text: TextView
  @BindView(R.id.view_complex) lateinit var complex: View
  @BindView(R.id.input_first) lateinit var input1: TextInputLayout
  @BindView(R.id.input_second) lateinit var input2: TextInputLayout
  private lateinit var form: Form

  private val coordinator
    get() = complex.getCoordinator() as FooCoordinator

  lateinit var onBack: Callable<Boolean>

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
    override fun submit(): Observable<FormResult> = coordinator.clicks()
        .flatMapSingle {
          form.with(Validation(input2, ValueCheckRule("bar", { getString(R.string.error_no_match, "bar", it) })))
              .validate()
        }

    override fun back(): Observable<Unit> = Observable.create { emitter -> onBack = Callable { emitter.onNext(Unit); true } }
  }

  override fun render(state: FooState) {
    text.text = state.text
    form.restore(state.formData)
  }

  override fun onBackPressed(): Boolean = if (text.text == "on back") false else onBack.call()
}

class FooCoordinator @Inject constructor() : Coordinator() {
  @BindView(R.id.submit) lateinit var submit: Button

  override fun attach(view: View) {
    super.attach(view)
    Toast.makeText(view.context, "attach", Toast.LENGTH_SHORT).show()
  }

  fun clicks(): Observable<Unit> = submit.clicks()
}
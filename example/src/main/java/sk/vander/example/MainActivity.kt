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
import com.vander.scaffold.form.FormInput
import com.vander.scaffold.form.FormIntents
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
import kotlinx.android.synthetic.main.activity_main.*
import sk.vander.example.R.id.submit
import java.util.concurrent.Callable
import javax.inject.Inject

val iter: Int = 0
  get() = field++

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
    val text: String
) : Screen.State

interface FooIntents : FormIntents {
  fun submit(): Observable<Unit>
  fun back(): Observable<Unit>
}

class FooModel @Inject constructor() : ScreenModel<FooState, FooIntents>() {
  val form = Form().withInputValidations(
      Validation(R.id.input_first, NotEmptyRule(R.string.error_empty), EmailRule(R.string.error_email)),
      Validation(R.id.input_second, NotEmptyRule(R.string.error_empty))
  )

  override fun collectIntents(intents: FooIntents, result: Observable<Result>): Disposable {
    state.init(FooState("hello"))

    val submit = intents.submit()
        .filter { form.validate(event) }

    return CompositeDisposable(
        result.subscribe { event.onNext(ToastEvent(msg = "hello result ${it.request}")) },
        submit.subscribe { event.onNext(WithResultExplicit(MainActivity::class, iter)) },
        intents.back().subscribe { state.next { copy(text = "on back") } },
        form.subscribe(intents, event)
    )
  }
}

class FooScreen : Screen<FooState, FooIntents>(), HandlesBack {
  @BindView(R.id.text) lateinit var text: TextView
  @BindView(R.id.view_complex) lateinit var complex: View
  private lateinit var form: FormInput

  private val coordinator
    get() = complex.getCoordinator() as FooCoordinator

  lateinit var onBack: Callable<Boolean>

  override fun layout(): Int = R.layout.activity_main

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    form = FormInput().withTextInputs(input_first, input_second)
  }

  override fun intents(): FooIntents = object : FooIntents {
    override val form: FormInput = this@FooScreen.form
    override fun submit(): Observable<Unit> = coordinator.clicks()
    override fun back(): Observable<Unit> = Observable.create { emitter -> onBack = Callable { emitter.onNext(Unit); true } }
    override fun events(): List<Observable<*>> = form.events(this@FooScreen)
  }

  override fun render(state: FooState) {
    text.text = state.text
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
package sk.vander.example

import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding3.view.clicks
import com.vander.scaffold.form.Form
import com.vander.scaffold.form.FormInput
import com.vander.scaffold.form.FormIntents
import com.vander.scaffold.form.validator.EmailRule
import com.vander.scaffold.form.validator.NotEmptyRule
import com.vander.scaffold.form.validator.ValidateRule
import com.vander.scaffold.form.validator.Validation
import com.vander.scaffold.screen.*
import com.vander.scaffold.ui.HandlesBack
import com.vander.scaffold.ui.NavigationActivity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_custom_view.*
import kotlinx.android.synthetic.main.layout_custom_view.view.*
import java.util.concurrent.Callable
import javax.inject.Inject

var iter: Int = 0
  get() = field++

class MinLengthRule : ValidateRule() {
  private val minLength = 3

  override val errorRes: Int
    get() = R.string.error_min_length

  override val errorMessageParams: Array<out Any>?
    get() = arrayOf(minLength)

  override fun validate(text: String): Boolean = text.length >= minLength
}

class MainActivity : NavigationActivity() {
  override val graphId: Int = R.navigation.app_graph
}

data class FooState(
    val text: String
) : Screen.State

interface FooIntents : FormIntents {
  fun submit(): Observable<Unit>
  fun back(): Observable<Unit>
}

class FooModel @Inject constructor() : ScreenModel<FooState, FooIntents>() {
  val form = Form(event).withInputValidations(
      Validation(R.id.input_first, NotEmptyRule(R.string.error_empty), EmailRule(R.string.error_email)),
      Validation(R.id.input_second, NotEmptyRule(R.string.error_empty)),
      Validation(R.id.input_third, NotEmptyRule(R.string.error_empty), MinLengthRule())
  )

  override fun collectIntents(intents: FooIntents, result: Observable<Result>): Disposable {
    state.init(FooState("hello"))

    val submit = intents.submit()
        .filter { form.validate() }

    return CompositeDisposable(
        result.subscribe { event.onNext(ToastEvent(msg = "hello result ${it.request}")) },
        submit.subscribe { event.onNext(WithResultExplicit(MainActivity::class, iter)) },
        intents.back().subscribe { state.next { copy(text = "on back") } },
        form.subscribe(intents)
    )
  }
}

class FooScreen : Screen<FooState, FooIntents>(), HandlesBack {
  private lateinit var form: FormInput

  lateinit var onBack: Callable<Boolean>

  override fun layout(): Int = R.layout.activity_main

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    form = FormInput().withTextInputs(input_first, input_second, input_third)
    form.validationEnabled(input_second, false)
  }

  override fun intents(): FooIntents = object : FooIntents {
    override val form: FormInput = this@FooScreen.form
    override fun submit(): Observable<Unit> = view_complex.submit.clicks()
    override fun back(): Observable<Unit> = Observable.create { emitter -> onBack = Callable { emitter.onNext(Unit); true } }
    override fun events(): List<Observable<*>> = form.events(this@FooScreen)
  }

  override fun render(state: FooState) {
    text.text = state.text
  }

  override fun onBackPressed(): Boolean = if (text.text == "on back") false else onBack.call()
}
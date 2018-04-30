package sk.vander.example

import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.view.View
import android.widget.Button
import android.widget.TextView
import butterknife.BindView
import com.jakewharton.rxbinding2.view.clicks
import com.vander.scaffold.form.Form
import com.vander.scaffold.form.FormData
import com.vander.scaffold.form.FormResult
import com.vander.scaffold.form.validator.EmailRule
import com.vander.scaffold.form.validator.NotEmptyRule
import com.vander.scaffold.form.validator.ValidateRule
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

interface FooIntents : Form.FormIntents {
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
  @BindView(R.id.submit) lateinit var submit: Button
  @BindView(R.id.input_first) lateinit var input1: TextInputLayout
  @BindView(R.id.input_second) lateinit var input2: TextInputLayout
  private val form = Form()

  override fun layout(): Int = R.layout.activity_main

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    form.init(
        input1 to linkedSetOf(NotEmptyRule(R.string.error_empty), EmailRule(R.string.error_email)),
        input2 to linkedSetOf<ValidateRule>(NotEmptyRule(R.string.error_empty))
    )
  }

  override fun intents(): FooIntents = object : FooIntents {
    override val form: Form = this@FooScreen.form
    override fun submit(): Observable<FormResult> = submit.clicks()
        .flatMapSingle { form.validate() }
  }

  override fun render(state: FooState) {
    text.text = state.text
    form.restore(state.formData)
  }

}
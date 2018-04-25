package sk.vander.example

import android.os.Bundle
import android.widget.TextView
import butterknife.BindView
import com.vander.scaffold.screen.Empty
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
    supportFragmentManager.beginTransaction()
        .replace(R.id.container_id, FooScreen())
        .commit()
  }
}

data class FooState(
    val text: String
) : Screen.State

class FooModel @Inject constructor() : ScreenModel<FooState, Empty>() {
  override fun collectIntents(intents: Empty, result: Observable<Result>): Disposable {
    state.onNext(FooState("hello"))
    return CompositeDisposable()
  }
}

class FooScreen : Screen<FooState, Empty>() {
  @BindView(R.id.text) lateinit var text: TextView

  override fun layout(): Int = R.layout.activity_main

  override fun intents(): Empty = Empty

  override fun render(state: FooState) {
    text.text = state.text
  }

}
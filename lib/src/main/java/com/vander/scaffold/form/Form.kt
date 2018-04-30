package com.vander.scaffold.form

import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.view.View
import com.jakewharton.rxbinding2.widget.afterTextChangeEvents
import com.vander.scaffold.form.validator.AfterTextChangedWatcher
import com.vander.scaffold.form.validator.ValidateRule
import com.vander.scaffold.screen.Screen
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

typealias FormData = Map<Int, Editable>
typealias FormResult = Pair<Boolean, Map<Int, Editable>>

class Form {
  val state: MutableMap<Int, Editable> = mutableMapOf()
  lateinit var items: List<Pair<TextInputLayout, LinkedHashSet<ValidateRule>>>

  private fun TextInputLayout.clearErrorAfterChange() {
    editText?.addTextChangedListener(AfterTextChangedWatcher({ isErrorEnabled = false }))
  }

  private fun TextInputLayout.validate(vararg rules: ValidateRule): Boolean =
      if (visibility != View.VISIBLE) true else
        rules.find { !it.validate(editText?.text.toString()) }
            .let {
              error = it?.let { context.getString(it.errorMessage) }
              it == null
            }

  fun init(clazz: KClass<*>) {
//    fetch fields of type 'TextInputLayout' with Validate annotations
//    make a map
  }

  fun init(vararg inputValidationPairs: Pair<TextInputLayout, LinkedHashSet<ValidateRule>>) {
    check(inputValidationPairs.all { it.first.editText != null })
    inputValidationPairs.forEach { it.first.clearErrorAfterChange() }
    items = listOf(*inputValidationPairs)
  }

  fun validate(): Single<FormResult> =
      items.map { (input, rules) -> input.validate(*rules.toTypedArray()) }
          .find { !it }
          .let { Single.just(FormResult(it == null, state.toMap())) }

  fun state(): Observable<FormData> =
      items.map { (input, _) -> input.editText!!.afterTextChangeEvents().skipInitialValue().map { input.id to it.editable()!! } }
          .let { Observable.merge(it) }
          .debounce(300, TimeUnit.MILLISECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .filter { (id, editable) -> state[id].toString() != editable.toString() }
          .doOnNext { (id, editable) -> if (editable.isBlank()) state.remove(id) else state[id] = editable }
          .map { state.toMap() }

  fun restore(formData: FormData) {
    if (state.isEmpty() && formData.isNotEmpty()) {
      state.putAll(formData)
      items.map { it.first.apply { error = null } }.filter { state.containsKey(it.id) }.forEach {
        it.editText!!.text = state[it.id]
        it.editText!!.apply { setSelection(length()) }
      }
    }
  }

  interface FormIntents : Screen.Intents {
    val form: Form
    fun state(): Observable<FormData> = form.state()
  }
}
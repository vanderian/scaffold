package com.vander.scaffold.form

import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.view.View
import com.jakewharton.rxbinding2.widget.afterTextChangeEvents
import com.vander.scaffold.form.validator.AfterTextChangedWatcher
import com.vander.scaffold.form.validator.ValidateRule
import com.vander.scaffold.form.validator.Validation
import com.vander.scaffold.form.validator.Validator
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

typealias FormData = Map<Int, Editable>
typealias FormResult = Pair<Boolean, Map<Int, Editable>>

class Form : Validator {
  private val state: MutableMap<Int, Editable> = mutableMapOf()
  private lateinit var items: Map<TextInputLayout, Set<ValidateRule>>

  private fun TextInputLayout.clearErrorAfterChange() {
    editText?.addTextChangedListener(AfterTextChangedWatcher({ isErrorEnabled = false }))
  }

  private fun TextInputLayout.validate(vararg rules: ValidateRule): Boolean =
      if (visibility != View.VISIBLE) true else
        rules.find { !it.validate(editText?.text.toString()) }
            .let {
              error = it?.let {
                if (it.errorRes != -1) context.getString(it.errorRes)
                else it.errorMessage.invoke(editText?.text.toString())
              }
              it == null
            }

  private fun Map<TextInputLayout, Set<ValidateRule>>.validate(): Single<FormResult> =
      map { (input, rules) -> input.validate(*rules.toTypedArray()) }
          .find { !it }
          .let { Single.just(FormResult(it == null, state.toMap())) }

  private fun init(vararg validations: Validation) {
    check(validations.all { it.input.editText != null })
    validations.forEach { it.input.clearErrorAfterChange() }
    items = validations.associate { it.input to it.rules }
  }

  override fun validate(): Single<FormResult> = items.validate()

  fun with(vararg validations: Validation) = object : Validator {
    override fun validate(): Single<FormResult> {
      val map = items.toMutableMap()
      validations.forEach {
        if (map.containsKey(it.input)) {
          map[it.input] = map[it.input]!!.plus(it.rules)
        } else {
          map[it.input] = it.rules
        }
      }
      return map.validate()
    }
  }

  fun state(): Observable<FormData> =
      items.map { (input, _) -> input.editText!!.afterTextChangeEvents().skipInitialValue().map { input.id to it.editable()!! } }
          .let { Observable.merge(it) }
          .debounce(300, TimeUnit.MILLISECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .filter { (id, editable) -> state[id].toString() != editable.toString() }
          .doOnNext { (id, editable) -> if (editable.isBlank()) state.remove(id) else state[id] = editable }
          .map { state.toMap() }

  fun restore(formData: FormData) {
    if (state.isEmpty()) {
      state.putAll(formData)
      items.map { it.key.apply { error = null } }.filter { state.containsKey(it.id) }.forEach {
        it.editText!!.text = state[it.id]
        it.editText!!.apply { setSelection(length()) }
      }
    }
  }

  companion object {
    fun init(vararg validations: Validation) = Form().apply { init(*validations) }
  }

}
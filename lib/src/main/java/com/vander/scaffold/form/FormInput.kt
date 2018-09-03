package com.vander.scaffold.form

import android.support.design.widget.TextInputLayout
import android.widget.CheckBox
import android.widget.Spinner
import butterknife.internal.Utils.listOf
import com.jakewharton.rxbinding2.widget.afterTextChangeEvents
import com.jakewharton.rxbinding2.widget.checkedChanges
import com.jakewharton.rxbinding2.widget.itemSelections
import com.vander.scaffold.form.validator.AfterTextChangedWatcher
import com.vander.scaffold.screen.Screen
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.Collections.emptyList
import java.util.Collections.emptyMap

class FormInput {
  private var inputLayouts: List<TextInputLayout> = emptyList()
  private var spinners: List<Spinner> = emptyList()
  private var checkBoxes: List<CheckBox> = emptyList()
  internal val enabled: BehaviorSubject<Map<Int, Boolean>> = BehaviorSubject.createDefault(emptyMap())
  private var restore = true
    get() = field.also { field = false }

  private fun TextInputLayout.clearErrorAfterChange() {
    editText?.addTextChangedListener(AfterTextChangedWatcher { isErrorEnabled = false })
  }

  private fun restore(state: FormState) {
    if (restore) {
      inputLayouts.map { it.apply { error = null } }.filter { state.containsKey(it.id) }.forEach {
        it.editText!!.setText(state[it.id] as String)
        it.editText!!.apply { setSelection(length()) }
      }
      spinners.filter { state.containsKey(it.id) }.forEach {
        val value = state[it.id] as Int
        it.post { it.setSelection(value, false) }
      }
      checkBoxes.filter { state.containsKey(it.id) }.forEach {
        it.isChecked = state[it.id] as Boolean
      }
    }
  }

  private fun setErrors(errors: FormErrors) {
    inputLayouts.forEach { input -> input.error = errors[input.id]?.let { input.context.getString(it) } }
  }

  fun validationEnabled(view: TextInputLayout, enabled: Boolean) = this.enabled.onNext(this.enabled.value!! + (view.id to enabled))

  fun withTextInputs(vararg inputs: TextInputLayout): FormInput = this.apply {
    inputLayouts = inputs.toList()
    check(inputLayouts.all { it.editText != null })
    inputLayouts.forEach { it.clearErrorAfterChange() }
  }

  fun withSpinners(vararg spinners: Spinner): FormInput = this.apply {
    this.spinners = spinners.toList()
  }

  fun withCheckBoxes(vararg checkBoxes: CheckBox): FormInput = this.apply {
    this.checkBoxes = checkBoxes.toList()
  }

  internal val inputChanges by lazy<Observable<Pair<Int, String>>> {
    Observable.merge(inputLayouts.map { v -> v.editText!!.afterTextChangeEvents().map { v.id to it.editable().toString() } }).share()
  }

  internal val spinnerChanges by lazy<Observable<Pair<Int, Int>>> {
    Observable.merge(spinners.map { v -> v.itemSelections().map { v.id to it } }).share()
  }
  internal val checkBoxChanges by lazy<Observable<Pair<Int, Boolean>>> {
    Observable.merge(checkBoxes.map { v -> v.checkedChanges().map { v.id to it } }).share()
  }

  fun events(screen: Screen<*, *>): List<Observable<*>> = listOf(
      screen.event(FormEventInit::class).doOnNext { restore(it.state) },
      screen.event(FormEventError::class).doOnNext { setErrors(it.errors) }
  )
}
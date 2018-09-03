package com.vander.scaffold.form

import android.support.annotation.IdRes
import android.support.design.widget.TextInputLayout
import android.util.SparseArray
import android.view.View
import android.widget.CheckBox
import android.widget.Spinner
import com.jakewharton.rxbinding2.widget.afterTextChangeEvents
import com.jakewharton.rxbinding2.widget.checkedChanges
import com.jakewharton.rxbinding2.widget.itemSelections
import com.vander.scaffold.form.validator.AfterTextChangedWatcher
import com.vander.scaffold.screen.Screen
import io.reactivex.Observable

class FormInput {
  private var inputLayouts: List<TextInputLayout> = emptyList()
  private var spinners: List<Spinner> = emptyList()
  private var checkBoxes: List<CheckBox> = emptyList()
  private val enabled: SparseArray<Boolean> = SparseArray()
  private var restore = true
    get() = field.also { field = false }

  private fun TextInputLayout.clearErrorAfterChange() {
    editText?.addTextChangedListener(AfterTextChangedWatcher { isErrorEnabled = false })
  }

  private fun restore(state: FormState) {
    if (restore) {
      inputLayouts.map { it.apply { error = null } }.filter { state.containsKey(it.id) }.forEach {
        it.editText!!.setText(state[it.id]?.value as String)
        it.editText!!.apply { setSelection(length()) }
      }
      spinners.filter { state.containsKey(it.id) }.forEach {
        val value = state[it.id]?.value as Int
        it.post { it.setSelection(value, false) }
      }
      checkBoxes.filter { state.containsKey(it.id) }.forEach {
        it.isChecked = state[it.id]?.value as Boolean
      }
    }
  }

  private fun setErrors(errors: FormErrors) {
    inputLayouts.forEach { input -> input.error = errors[input.id]?.let { input.context.getString(it) } }
  }

  fun validationEnabled(view: TextInputLayout, enabled: Boolean) = this.enabled.put(view.id, enabled)

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

  val inputChanges: Observable<ViewState<String>> by lazy {
    Observable.merge(inputLayouts.map { v ->
      v.editText!!.afterTextChangeEvents().map {
        ViewState(v.id, it.editable().toString(), enabled[v.id] ?: true)
      }
    }).share()
  }

  val spinnerChanges: Observable<ViewState<Int>> by lazy {
    Observable.merge(spinners.map { v -> v.itemSelections().map { ViewState(v.id, it, enabled[v.id] ?: true) } }).share()
  }
  val checkBoxChanges: Observable<ViewState<Boolean>> by lazy {
    Observable.merge(checkBoxes.map { v -> v.checkedChanges().map { ViewState(v.id, it, enabled[v.id] ?: true) } }).share()
  }

  fun events(screen: Screen<*, *>): List<Observable<*>> = listOf(
      screen.event(FormEventInit::class).doOnNext { restore(it.state) },
      screen.event(FormEventError::class).doOnNext { setErrors(it.errors) }
  )
}
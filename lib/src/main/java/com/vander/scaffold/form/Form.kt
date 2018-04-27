package com.vander.scaffold.form

import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.view.View
import com.vander.scaffold.form.validator.AfterTextChangedWatcher
import com.vander.scaffold.form.validator.ValidateRule
import io.reactivex.Single
import kotlin.reflect.KClass

class Form {
  val state: MutableMap<Int, Editable> = mutableMapOf()
  lateinit var items: List<Pair<TextInputLayout, LinkedHashSet<ValidateRule>>>

  fun init(clazz: KClass<*>) {
//    fetch fields of type 'TextInputLayout' with Validate annotations
//    make a map
  }

  fun init(vararg inputValidationPairs: Pair<TextInputLayout, LinkedHashSet<ValidateRule>>) {
    check(inputValidationPairs.all { it.first.editText != null })
    inputValidationPairs.forEach { it.first.clearErrorAfterChange() }
    items = listOf(*inputValidationPairs)
  }

  fun validate(): Single<FormData> =
      items.map { (input, rules) ->
        state[input.id] = input.editText!!.text
        input.validate(*rules.toTypedArray())
      }.find { !it }.let { Single.just(FormData(it == null, state.toMap())) }


  fun TextInputLayout.validate(vararg rules: ValidateRule): Boolean =
      if (visibility != View.VISIBLE) true else
        rules.find { !it.validate(editText?.text.toString()) }
            .let {
              error = it?.let { context.getString(it.errorMessage) }
              it == null
            }

  fun TextInputLayout.clearErrorAfterChange() {
    editText?.addTextChangedListener(AfterTextChangedWatcher({ isErrorEnabled = false }))
  }

  fun restore(formData: FormData) {
    if (state.isEmpty() && formData.state.isNotEmpty()) {
      items.map { it.first }.forEach {
        it.editText!!.text = formData.state[it.id]
      }
    }
  }

  data class FormData(
      val valid: Boolean = false,
      val state: Map<Int, Editable> = emptyMap()
  )


}
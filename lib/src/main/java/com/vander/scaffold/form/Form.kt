package com.vander.scaffold.form

import android.support.annotation.IdRes
import com.vander.scaffold.debug.log
import com.vander.scaffold.form.validator.ValidateRule
import com.vander.scaffold.form.validator.Validation
import com.vander.scaffold.screen.Event
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class Form {
  var state: FormState = mutableMapOf()
  private var validations: Map<Int, Set<ValidateRule>> = mutableMapOf()

  fun subscribe(intents: FormIntents, eventObserver: Observer<Event>): Disposable =
      intents.allChanges()
          .doOnSubscribe { if (state.isNotEmpty()) eventObserver.onNext(FormEventInit(state.toMap())) }
          .doOnNext { (state as MutableMap)[it.id] = it }
          .subscribe()

  fun withInputValidations(vararg validations: Validation) = this.apply { this.validations = validations.associate { it.id to it.rules } }

  fun init(@IdRes id: Int, value: Any) {
    if (!state.containsKey(id)) (state as MutableMap)[id] = ViewState(id, value, true)
  }

  fun spinnerSelection(@IdRes id: Int): Int = state[id]?.value as Int? ?: throw IllegalArgumentException()
  fun inputText(@IdRes id: Int): String = state[id]?.value as String? ?: throw IllegalArgumentException()
  fun checkBoxChecked(@IdRes id: Int): Boolean = state[id]?.value as Boolean? ?: throw IllegalArgumentException()
  fun enabled(@IdRes id: Int): Boolean = state[id]?.enabled ?: throw IllegalArgumentException()

  fun validate(eventObserver: Observer<Event>, vararg validations: Validation): Boolean {
    val errors: FormErrors = mutableMapOf()
    return (this.validations + validations.associate { it.id to it.rules })
        .filterKeys { state.containsKey(it) && enabled(it) }
        .map { (id, rules) ->
          rules.find { !it.validate(inputText(id)) }
              .let { rule ->
                rule?.let { (errors as MutableMap)[id] = it.errorRes }
                rule == null
              }
        }
        .all { it }
        .also { eventObserver.onNext(FormEventError(errors)) }
  }
}

package com.vander.scaffold.form

import android.support.annotation.IdRes
import com.vander.scaffold.form.validator.ValidateRule
import com.vander.scaffold.form.validator.Validation
import com.vander.scaffold.screen.Event
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class Form {
  private val state: FormState = mutableMapOf()
  private var enabled: Map<Int, Boolean> = emptyMap()
  private var validations: Map<Int, Set<ValidateRule>> = mutableMapOf()

  fun subscribe(intents: FormIntents, eventObserver: Observer<Event>): Disposable =
      CompositeDisposable().apply {
        addAll(
            intents.allChanges()
                .doOnSubscribe { if (state.isNotEmpty()) eventObserver.onNext(FormEventInit(state.toMap())) }
                .doOnNext { (state as MutableMap)[it.first] = it.second }
                .subscribe(),
            intents.enabledChanges().subscribe { enabled = it }
        )
      }

  fun withInputValidations(vararg validations: Validation) = this.apply { this.validations = validations.associate { it.id to it.rules } }

  fun init(@IdRes id: Int, value: Any) {
    if (!state.containsKey(id)) (state as MutableMap)[id] = value
  }

  fun spinnerSelection(@IdRes id: Int): Int = state[id] as Int? ?: throw IllegalArgumentException()
  fun inputText(@IdRes id: Int): String = state[id] as String? ?: throw IllegalArgumentException()
  fun checkBoxChecked(@IdRes id: Int): Boolean = state[id] as Boolean? ?: throw IllegalArgumentException()
  fun hasValue(@IdRes id: Int): Boolean = state.containsKey(id)
  fun hasValues(): Boolean = validations.keys.all { hasValue(it) }

  fun validate(eventObserver: Observer<Event>, vararg validations: Validation): Boolean {
    val errors: FormErrors = mutableMapOf()
    return (this.validations + validations.associate { it.id to it.rules })
        .filterKeys { state.containsKey(it) && enabled[it] ?: true }
        .map { (id, rules) ->
          rules.find { !it.validate(inputText(id)) }
              .let { rule ->
                rule?.let { (errors as MutableMap)[id] = it.errorRes to it.errorMessageParams }
                rule == null
              }
        }
        .all { it }
        .also { eventObserver.onNext(FormEventError(errors)) }
  }
}

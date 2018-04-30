package com.vander.scaffold.form

import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.Selection.setSelection
import android.view.View
import com.jakewharton.rxbinding2.widget.afterTextChangeEvents
import com.vander.scaffold.form.validator.*
import com.vander.scaffold.screen.Screen
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure

typealias FormData = Map<Int, Editable>
typealias FormResult = Pair<Boolean, Map<Int, Editable>>

class Form {
  private val state: MutableMap<Int, Editable> = mutableMapOf()
  private lateinit var items: Map<TextInputLayout, LinkedHashSet<ValidateRule>>

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

  fun init(vararg inputValidationPairs: Pair<TextInputLayout, LinkedHashSet<ValidateRule>>) {
    check(inputValidationPairs.all { it.first.editText != null })
    inputValidationPairs.forEach { it.first.clearErrorAfterChange() }
    items = mapOf(*inputValidationPairs)
  }

  fun validate(vararg extra: Pair<TextInputLayout, ValidateRule> = emptyArray()): Single<FormResult> =
//      items.plus(extra.map { it.first to linkedSetOf(it.second) })
      items
          .map { (input, rules) -> input.validate(*rules.toTypedArray()) }
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
    if (state.isEmpty()) {
      state.putAll(formData)
      items.map { it.key.apply { error = null } }.filter { state.containsKey(it.id) }.forEach {
        it.editText!!.text = state[it.id]
        it.editText!!.apply { setSelection(length()) }
      }
    }
  }

  companion object {
    internal val registered = mutableMapOf<KClass<out Annotation>, (Annotation) -> ValidateRule>()

    fun <T : Annotation> register(clazz: KClass<out T>, rule: (T) -> ValidateRule) {
      registered[clazz] = rule as (Annotation) -> ValidateRule
    }

    fun <T : Screen<*, *>> init(screen: T): Form = Form().apply {
      val fields =
          screen::class.declaredMemberProperties
              .filter { it.returnType.jvmErasure == TextInputLayout::class }
              .filter { it.annotations.any { registered.keys.contains(it.annotationClass) } }
              .map { it as KProperty1<T, TextInputLayout> }

      init(*fields.map {
        val input = it.get(screen)
        val rules = it.annotations
            .filter { registered.containsKey(it.annotationClass) }
            .map { registered[it.annotationClass]!!.invoke(it) }
        input to linkedSetOf(*rules.toTypedArray())
      }.toTypedArray())
    }

    init {
      register(EmailValidation::class, { EmailRule(it.msg) })
      register(NotEmptyValidation::class, { NotEmptyRule(it.msg) })
    }
  }

  interface FormIntents : Screen.Intents {
    val form: Form
    fun state(): Observable<FormData> = form.state()
  }
}
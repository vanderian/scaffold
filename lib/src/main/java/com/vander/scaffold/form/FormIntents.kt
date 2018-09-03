package com.vander.scaffold.form

import com.vander.scaffold.screen.Screen
import io.reactivex.Observable

interface FormIntents : Screen.Intents {

  val form: FormInput
  fun inputsTextChanges(): Observable<ViewState<String>> = form.inputChanges
  fun spinnersSelections(): Observable<ViewState<Int>> = form.spinnerChanges
  fun checkBoxesChanges(): Observable<ViewState<Boolean>> = form.checkBoxChanges
  fun allChanges(): Observable<ViewState<*>> = Observable.merge(inputsTextChanges(), spinnersSelections(), checkBoxesChanges())
  fun inputTextChanges(id: Int): Observable<ViewState<String>> = inputsTextChanges().filter { it.id == id }
  fun spinnerSelections(id: Int): Observable<ViewState<Int>> = spinnersSelections().filter { it.id == id }
  fun checkBoxChanges(id: Int): Observable<ViewState<Boolean>> = checkBoxesChanges().filter { it.id == id }
}
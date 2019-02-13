package com.vander.scaffold.form

import com.vander.scaffold.screen.Screen
import io.reactivex.Observable

interface FormIntents : Screen.Intents {

  val form: FormInput
  fun enabledChanges(): Observable<Map<Int, Boolean>> = form.enabled
  fun inputsTextChanges(): Observable<Pair<Int, String>> = form.inputChanges
  fun spinnersSelections(): Observable<Pair<Int, Int>> = form.spinnerChanges
  fun checkBoxesChanges(): Observable<Pair<Int, Boolean>> = form.checkBoxChanges
  fun focusChanges(): Observable<Pair<Int, Boolean>> = form.focusChanges
  fun allChanges(): Observable<Pair<Int, Any>> = Observable.merge(inputsTextChanges(), spinnersSelections(), checkBoxesChanges())
  fun inputTextChanges(id: Int): Observable<String> = inputsTextChanges().filter { it.first == id }.map { it.second }
  fun spinnerSelections(id: Int): Observable<Int> = spinnersSelections().filter { it.first == id }.map { it.second }
  fun checkBoxChanges(id: Int): Observable<Boolean> = checkBoxesChanges().filter { it.first == id }.map { it.second }
  fun focusChanges(id: Int): Observable<Boolean> = focusChanges().filter { it.first == id }.map { it.second }
}
package com.vander.scaffold.form

import com.vander.scaffold.screen.Screen
import io.reactivex.Observable

interface FormIntents : Screen.Intents {

  val form: FormInput
  fun inputsTextChanges(): Observable<Pair<Int, String>> = form.inputChanges
  fun spinnersSelections(): Observable<Pair<Int, Int>> = form.spinnerChanges
  fun checkBoxesChanges(): Observable<Pair<Int, Boolean>> = form.checkBoxChanges
  fun allChanges(): Observable<Pair<Int, Any>> = Observable.merge(inputsTextChanges(), spinnersSelections(), checkBoxesChanges())
  fun inputTextChanges(id: Int): Observable<Pair<Int, String>> = inputsTextChanges().filter { it.first == id }
  fun spinnerSelections(id: Int): Observable<Pair<Int, Int>> = spinnersSelections().filter { it.first == id }
  fun checkBoxChanges(id: Int): Observable<Pair<Int, Boolean>> = checkBoxesChanges().filter { it.first == id }
}
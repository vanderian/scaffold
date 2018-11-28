package com.vander.scaffold.debugyzer.bugreport

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.text.TextUtils
import android.util.AttributeSet
import com.jakewharton.rxbinding2.view.focusChanges
import com.jakewharton.rxbinding2.widget.afterTextChangeEvents
import com.vander.scaffold.R
import com.vander.scaffold.ui.text
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.bugreport_view.view.*

class BugReportView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {
  private val disposables = CompositeDisposable()

  override fun onFinishInflate() {
    super.onFinishInflate()
    checkScreenshot.isChecked = true
    checkLogs.isChecked = true
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    disposables.addAll(
        inputTitle.focusChanges()
            .skipInitialValue()
            .doOnNext { if (it) inputTitle.isErrorEnabled = false }
            .filter { !it }
            .map { TextUtils.isEmpty(inputTitle.text()) }
            .doOnNext { inputTitle.isErrorEnabled = it }
            .filter { it }
            .map { context.getString(R.string.debug_error_empty) }
            .subscribe { inputTitle.error = it }
    )
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    disposables.clear()
  }

  fun onValid(): Observable<Boolean> =
      inputTitle.editText!!.afterTextChangeEvents()
          .map { !TextUtils.isEmpty(it.editable().toString()) }

  val report: Report
    get() = Report(inputTitle.text(), inputDescription.text(), checkScreenshot.isChecked, checkLogs.isChecked)
}

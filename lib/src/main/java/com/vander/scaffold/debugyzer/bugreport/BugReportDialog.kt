package com.vander.scaffold.debugyzer.bugreport

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import com.vander.scaffold.R
import io.reactivex.Maybe
import io.reactivex.disposables.Disposable

class BugReportDialog(context: Context) : AlertDialog(context) {

  private val bugReportView: BugReportView = LayoutInflater.from(context).inflate(R.layout.bugreport_view, null) as BugReportView
  val onReport: Maybe<Report>

  init {
    setTitle(R.string.debug_title_report_bug)
    setView(bugReportView)

    onReport = Maybe.create<Report> { emitter ->
      var disposable: Disposable? = null
      setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.debug_button_positive)) { _, _ -> emitter.onSuccess(bugReportView.report) }
      setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.debug_button_negative), null as DialogInterface.OnClickListener?)
      setOnShowListener {
        disposable = bugReportView.onValid().subscribe { getButton(Dialog.BUTTON_POSITIVE).isEnabled = it }
      }
      setOnDismissListener {
        disposable?.let { if (!it.isDisposed) it.dispose() }
        emitter.onComplete()
      }
      emitter.setCancellable { dismiss() }
    }
  }
}

package com.vander.scaffold.debugyzer.bugreport


import android.app.Activity
import android.os.Build
import android.support.v4.app.ShareCompat
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.util.DisplayMetrics
import android.widget.Toast
import com.mattprecious.telescope.Lens
import com.vander.scaffold.R
import com.vander.scaffold.debugyzer.BugReportFileProvider
import com.vander.scaffold.debugyzer.log.LogCat
import com.vander.scaffold.debugyzer.log.MemoryLog
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File

/**
 * Pops a dialog asking for more information about the bug report and then creates an email with a
 * JIRA-formatted body.
 */
class BugReportLens(
    private val activity: Activity,
    private val memoryLog: MemoryLog,
    private val logCat: LogCat,
    private val reportData: ReportData
) : Lens() {

  private var screenshot: File? = null

  override fun onCapture(screenshot: File?) {
    this.screenshot = screenshot

    val dialog = BugReportDialog(activity)
    dialog.onReport
        .flatMap { report ->
          if (report.includeLogs) mergeLogs(report)
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .doOnError { Timber.e(it) }
              .doOnError { Toast.makeText(activity, R.string.debug_error_logs, Toast.LENGTH_SHORT).show() }
              .onErrorReturn { report }
              .toMaybe()
          else Maybe.just(report)
        }
        .doOnSuccess { submitReport(it) }
        .subscribe()
    dialog.show()
  }

  private fun mergeLogs(report: Report): Single<Report> =
      Single.zip(memoryLog.save(), logCat.save(),
          BiFunction { appLog: File, dumpLog: File -> report.copy(appLogs = appLog, dumpLogs = dumpLog) })

  private fun submitReport(report: Report) {
    val dm = activity.resources.displayMetrics
    val densityBucket = getDensityString(dm)

    val intent = ShareCompat.IntentBuilder.from(activity)
        .setType("message/rfc822")
        .addEmailTo(reportData.email)
        .setSubject(report.title)

    val body = StringBuilder()
    if (!TextUtils.isEmpty(report.description)) {
      body.append("{panel:title=Description}\n").append(report.description).append("\n{panel}\n\n")
    }

    body.append("{panel:title=App}\n")
    body.append("Version: ").append(reportData.name).append('\n')
    body.append("Version code: ").append(reportData.code).append('\n')
    body.append("{panel}\n\n")

    body.append("{panel:title=Device}\n")
    body.append("Make: ").append(Build.MANUFACTURER).append('\n')
    body.append("Model: ").append(Build.MODEL).append('\n')
    body.append("Resolution: ")
        .append(dm.heightPixels)
        .append("x")
        .append(dm.widthPixels)
        .append('\n')
    body.append("Density: ")
        .append(dm.densityDpi)
        .append("dpi (")
        .append(densityBucket)
        .append(")\n")
    body.append("Release: ").append(Build.VERSION.RELEASE).append('\n')
    body.append("API: ").append(Build.VERSION.SDK_INT).append('\n')
    body.append("{panel}")

    intent.setText(body.toString())

    if (screenshot != null && report.includeScreenshot) {
      intent.addStream(FileProvider.getUriForFile(activity, BugReportFileProvider.authority(activity), screenshot!!))
    }
    report.appLogs?.let {
      intent.addStream(FileProvider.getUriForFile(activity, BugReportFileProvider.authority(activity), it))
    }

    report.dumpLogs?.let {
      intent.addStream(FileProvider.getUriForFile(activity, BugReportFileProvider.authority(activity), it))
    }

    intent.startChooser()
  }

  private fun getDensityString(displayMetrics: DisplayMetrics): String =
      when (displayMetrics.densityDpi) {
        DisplayMetrics.DENSITY_LOW -> "ldpi"
        DisplayMetrics.DENSITY_MEDIUM -> "mdpi"
        DisplayMetrics.DENSITY_HIGH -> "hdpi"
        DisplayMetrics.DENSITY_XHIGH -> "xhdpi"
        DisplayMetrics.DENSITY_XXHIGH -> "xxhdpi"
        DisplayMetrics.DENSITY_XXXHIGH -> "xxxhdpi"
        DisplayMetrics.DENSITY_TV -> "tvdpi"
        else -> displayMetrics.densityDpi.toString()
      }
}

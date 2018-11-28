package com.vander.scaffold.debugyzer

import android.app.Activity
import android.content.Context
import com.jakewharton.threetenabp.AndroidThreeTen
import com.mattprecious.telescope.TelescopeLayout
import com.vander.scaffold.debugyzer.bugreport.BugReportLens
import com.vander.scaffold.debugyzer.bugreport.ReportData
import com.vander.scaffold.debugyzer.log.LogCat
import com.vander.scaffold.debugyzer.log.MemoryLog
import timber.log.Timber

/**
 * @author marian on 13.4.2017.
 */
class BugReporter(private val ctx: Context) {
  private val memoryLog = MemoryLog(ctx)
  private val logCat = LogCat(ctx)
  private val memoryLogTree = memoryLog.tree()

  private fun cleanUp() {
    if (Timber.forest().contains(memoryLogTree)) {
      Timber.uproot(memoryLogTree)
    }
    memoryLog.cleanUp()
    logCat.cleanUp()
    TelescopeLayout.cleanUp(ctx)
  }

  fun lens(activity: Activity, reportData: ReportData) = BugReportLens(activity, memoryLog, logCat, reportData)

  fun init(enabled: Boolean) {
    cleanUp()
    if (enabled) {
      Timber.plant(memoryLogTree)
    }
  }
}
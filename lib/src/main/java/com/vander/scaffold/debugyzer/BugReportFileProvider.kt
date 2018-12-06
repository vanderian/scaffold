package com.vander.scaffold.debugyzer

import android.content.Context
import android.support.v4.content.FileProvider
import com.vander.scaffold.BuildConfig

/**
 * @author marian on 14.8.2017.
 */
class BugReportFileProvider : FileProvider() {
  companion object {
    fun authority(context: Context) = "${context.packageName}.debugyzer.provider"
  }
}
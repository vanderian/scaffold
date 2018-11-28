package com.vander.scaffold.debugyzer

import android.support.v4.content.FileProvider
import com.vander.scaffold.BuildConfig

/**
 * @author marian on 14.8.2017.
 */
class BugReportFileProvider : FileProvider() {
  companion object {
    const val authority = BuildConfig.APPLICATION_ID + ".debugyzer.provider"
  }
}
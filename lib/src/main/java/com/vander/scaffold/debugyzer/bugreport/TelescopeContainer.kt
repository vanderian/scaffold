package com.vander.scaffold.debugyzer.bugreport

import android.app.Activity
import android.view.ViewGroup
import com.mattprecious.telescope.TelescopeLayout
import com.vander.scaffold.R
import com.vander.scaffold.debugyzer.BugReporter
import com.vander.scaffold.ui.ViewContainer

/**
 * @author marian on 22.4.2017.
 */
class TelescopeContainer(
    private val enabled: () -> Boolean,
    private val bugReporter: BugReporter,
    private val reportData: ReportData
) : ViewContainer {

  override fun get(activity: Activity): ViewGroup =
      if (enabled()) {
        activity.setContentView(R.layout.container_telescope)
        val telescopeLayout = activity.findViewById(R.id.container_telescope) as TelescopeLayout
        telescopeLayout.setLens(bugReporter.lens(activity, reportData))
        telescopeLayout
      } else {
        activity.findViewById(android.R.id.content) as ViewGroup
      }
}

package com.vander.scaffold.debugyzer.bugreport

import java.io.File

data class Report(
    val title: String,
    val description: String,
    val includeScreenshot: Boolean,
    val includeLogs: Boolean,
    val appLogs: File? = null,
    val dumpLogs: File? = null
)
package com.vander.scaffold.debugyzer.log

import android.content.Context
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.io.File
import java.io.IOException

/**
 * @author marian on 22.4.2017.
 */
class LogCat(private val ctx: Context) {

  fun save(): Single<File> = Single.create { source ->
    val folder = ctx.getExternalFilesDir(null)
    if (folder == null) {
      source.onError(IOException("External storage is not mounted."))
      return@create
    }

    val fileName = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()).replace(':', '-') + FILE_SUFFIX
    val output = File(folder, fileName)

    try {
      val cmd = "logcat -d -v time -f " + output.absolutePath
      Runtime.getRuntime().exec(cmd)

    } catch (e: IOException) {
      source.onError(e)
    }

    source.onSuccess(output)
  }

  /**
   * Delete all of the log files saved to disk. Be careful not to call this before any intents have
   * finished using the file reference.
   */
  fun cleanUp(): Disposable = Single.fromCallable {
    val folder = ctx.getExternalFilesDir(null)
    folder?.run {
      listFiles().filter { endsWith(MemoryLog.FILE_SUFFIX) }.forEach { delete() }
    }
  }.subscribe()

  companion object {
    const val FILE_SUFFIX = "_dump.log"
  }
}
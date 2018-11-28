package com.vander.scaffold.debugyzer.log

import android.content.Context
import android.util.Log
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import okio.BufferedSink
import okio.Okio
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.*

class MemoryLog constructor(private val ctx: Context, private val bufferSize: Int = 500) {

  private val entries = ArrayDeque<Entry>(bufferSize + 1)
  private val entrySubject: PublishProcessor<Entry> = PublishProcessor.create()

  @Synchronized private fun addEntry(entry: Entry) {
    entries.addLast(entry)
    if (entries.size > bufferSize) {
      entries.removeFirst()
    }

    entrySubject.onNext(entry)
  }

  fun tree(): Timber.Tree = object : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
      addEntry(Entry(priority, tag ?: "", message))
    }
  }

  fun bufferedLogs(): List<Entry> = ArrayList(entries)

  fun logs(): Flowable<Entry> = entrySubject

  /**  Save the current logs to disk.  */
  fun save(): Single<File> = Single.create { source ->
    val folder = ctx.getExternalFilesDir(null)
    if (folder == null) {
      source.onError(IOException("External storage is not mounted."))
      return@create
    }

    val fileName = ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()).replace(':', '-') + FILE_SUFFIX
    val output = File(folder, fileName).apply { createNewFile() }

    var sink: BufferedSink? = null
    try {
      sink = Okio.buffer(Okio.sink(output))
      val entries = bufferedLogs()
      for (entry in entries) {
        sink.writeUtf8(entry.prettyPrint()).writeByte('\n'.toInt())
      }
    } catch (e: IOException) {
      source.onError(e)
    } finally {
      sink?.let {
        try {
          it.close()
        } catch (e: IOException) {
          source.onError(e)
        }
      }
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
      listFiles().filter { endsWith(FILE_SUFFIX) }.forEach { delete() }
    }
  }.subscribe()

  class Entry(val level: Int, val tag: String, val message: String) {

    fun prettyPrint(): String =
        String.format("%22s %s %s", tag, displayLevel(),
            // Indent newlines to match the original indentation.
            message.replace("\\n".toRegex(), "\n                         "))

    private fun displayLevel(): String =
        when (level) {
          Log.VERBOSE -> "V"
          Log.DEBUG -> "D"
          Log.INFO -> "I"
          Log.WARN -> "W"
          Log.ERROR -> "E"
          Log.ASSERT -> "A"
          else -> "?"
        }
  }

  companion object {
    const val FILE_SUFFIX = "_app.log"
  }
}

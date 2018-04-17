package com.vander.scaffold.debug

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context.POWER_SERVICE
import android.os.Build
import android.os.PowerManager
import android.os.PowerManager.*
import android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
import com.vander.scaffold.BuildConfig


/**
 * Show the activity over the lockscreen and wake up the device. If you launched the app manually
 * both of these conditions are already true. If you deployed from the IDE, however, this will
 * save you from hundreds of power button presses and pattern swiping per day!
 */
@SuppressLint("NewApi")
fun riseAndShine(activity: Activity) {
  if (BuildConfig.VERSION_CODE >= Build.VERSION_CODES.O_MR1) {
    activity.setShowWhenLocked(true)
  } else {
    activity.window.addFlags(FLAG_SHOW_WHEN_LOCKED)
  }

  val power = activity.getSystemService(POWER_SERVICE) as PowerManager
  val lock = power.newWakeLock(FULL_WAKE_LOCK or ACQUIRE_CAUSES_WAKEUP or ON_AFTER_RELEASE, "wakeup!")
  lock.acquire()
  lock.release()
}
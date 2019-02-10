package com.vander.scaffold

import android.os.Bundle
import android.os.Looper
import android.os.Parcelable
import androidx.navigation.NavArgs
import androidx.navigation.NavArgument
import androidx.navigation.NavArgumentBuilder
import androidx.navigation.NavDirections
import com.vander.scaffold.screen.NavDirection
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

fun <T> Observable<T>.switchToMainIfOther(): Observable<T> = this.flatMapSingle {
  if (Looper.myLooper() == Looper.getMainLooper()) Single.just(it)
  else Single.just(it).observeOn(AndroidSchedulers.mainThread())
}

fun NavDirections.event(): NavDirection = NavDirection(this.actionId, this.arguments)

private const val BUNDLE_PARCELABLE_KEY = "BUNDLE_PARCELABLE_KEY"
fun <T : Parcelable> T.bundle(key: String = BUNDLE_PARCELABLE_KEY): Bundle = Bundle().apply { putParcelable(key, this@bundle) }
fun <T : Parcelable> Bundle.unbundleOptional(key: String = BUNDLE_PARCELABLE_KEY): T? = getParcelable(key)
fun <T : Parcelable> Bundle.unbundle(key: String = BUNDLE_PARCELABLE_KEY): T = unbundleOptional(key)!!

fun <T : Parcelable> T.navArgs(): NavArgument = NavArgument.Builder().setIsNullable(false).setDefaultValue(this).build()

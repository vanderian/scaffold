package com.vander.scaffold

import android.os.Looper
import androidx.navigation.NavDirections
import com.vander.scaffold.screen.NavDirection
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

fun <T> Observable<T>.switchToMainIfOther(): Observable<T> = this.flatMapSingle {
  if (Looper.myLooper() == Looper.getMainLooper()) Single.just(it)
  else Single.just(it).observeOn(AndroidSchedulers.mainThread())
}

fun NavDirections.event(navHostId: Int? = null): NavDirection = NavDirection(this.actionId, this.arguments, navHostId = navHostId)
package com.vander.scaffold

import android.os.Looper
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

fun <T> Observable<T>.switchToMainIfOther(): Observable<T> = this.flatMapSingle {
  if (Looper.myLooper() == Looper.getMainLooper()) Single.just(it)
  else Single.just(it).observeOn(AndroidSchedulers.mainThread())
}
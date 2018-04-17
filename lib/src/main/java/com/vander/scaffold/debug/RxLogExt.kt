package com.vander.scaffold.debug

import io.reactivex.*

/**
 * @author marian on 23.2.2017.
 */
fun <T> Flowable<T>.log(tag: String): Flowable<T> =
    this.compose {
      it.doOnSubscribe { timber.log.Timber.d("%s #onSubscribe on %s", tag, Thread.currentThread()) }
          .doOnNext { value -> timber.log.Timber.d("%s #onNext:%s on %s", tag, value, Thread.currentThread()) }
          .doOnComplete { timber.log.Timber.d("%s #onComplete on %s", tag, Thread.currentThread()) }
          .doOnError { error -> timber.log.Timber.d(error, "%s #onError on %s", tag, Thread.currentThread()) }
          .doOnCancel { timber.log.Timber.d("%s #onCancel on %s", tag, Thread.currentThread()) }
          .doOnRequest { value -> timber.log.Timber.d("%s #onRequest:%s on %s", tag, value, Thread.currentThread()) }
    }

fun <T> Observable<T>.log(tag: String): Observable<T> =
    this.compose {
      it.doOnSubscribe { timber.log.Timber.d("%s #onSubscribe on %s", tag, Thread.currentThread()) }
          .doOnNext { value -> timber.log.Timber.d("%s #onNext:%s on %s", tag, value, Thread.currentThread()) }
          .doOnComplete { timber.log.Timber.d("%s #onComplete on %s", tag, Thread.currentThread()) }
          .doOnError { error -> timber.log.Timber.d(error, "%s #onError on %s", tag, Thread.currentThread()) }
          .doOnDispose { timber.log.Timber.d("%s #onDispose on %s", tag, Thread.currentThread()) }
    }

fun <T> Maybe<T>.log(tag: String): Maybe<T> =
    this.compose {
      it.doOnSubscribe { timber.log.Timber.d("%s #onSubscribe on %s", tag, Thread.currentThread()) }
          .doOnSuccess { value -> timber.log.Timber.d("%s #onSuccess:%s on %s", tag, value, Thread.currentThread()) }
          .doOnComplete { timber.log.Timber.d("%s #onComplete on %s", tag, Thread.currentThread()) }
          .doOnError { error -> timber.log.Timber.d(error, "%s #onError on %s", tag, Thread.currentThread()) }
          .doOnDispose { timber.log.Timber.d("%s #onDispose on %s", tag, Thread.currentThread()) }
    }

fun <T> Single<T>.log(tag: String): Single<T> =
    this.compose {
      it.doOnSubscribe { timber.log.Timber.d("%s #onSubscribe on %s", tag, Thread.currentThread()) }
          .doOnSuccess { value -> timber.log.Timber.d("%s #onSuccess:%s on %s", tag, value, Thread.currentThread()) }
          .doOnError { error -> timber.log.Timber.d(error, "%s #onError on %s", tag, Thread.currentThread()) }
          .doOnDispose { timber.log.Timber.d("%s #onDispose on %s", tag, Thread.currentThread()) }
    }

fun Completable.log(tag: String): Completable =
    this.compose {
      it.doOnSubscribe { timber.log.Timber.d("%s #onSubscribe on %s", tag, Thread.currentThread()) }
          .doOnComplete { timber.log.Timber.d("%s #onComplete on %s", tag, Thread.currentThread()) }
          .doOnError { error -> timber.log.Timber.d(error, "%s #onError on %s", tag, Thread.currentThread()) }
          .doOnDispose { timber.log.Timber.d("%s #onDispose on %s", tag, Thread.currentThread()) }
    }
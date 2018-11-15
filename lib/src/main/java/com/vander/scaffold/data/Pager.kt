package com.vander.scaffold.data

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.disposables.Disposables
import io.reactivex.subjects.PublishSubject

typealias PagingFunction<T> = (T) -> Maybe<T>
typealias PageTransformFunction<I, O> = (I) -> O

class Pager<I, O> internal constructor(
    private val pagingFunction: PagingFunction<I>,
    private val pageTransformer: PageTransformFunction<I, O>
) {

  private lateinit var pages: PublishSubject<Maybe<I>>
  private var nextPage: Maybe<I> = finish()
  private var disposable = Disposables.empty()
  val isDisposed
    get() = disposable.isDisposed

  /**
   * Transforms the given sequence to have its subsequent pages pushed into the observer subscribed
   * to the new sequence returned by this method. You can advance to the next page by calling [.next]
   *
   * @param source the source sequence, which would be the first page of the sequence to be paged
   * @return a new sequence based on `source`, where subscribers keep receiving pages through subsequent calls
   * to [.next]
   */
  fun page(source: Maybe<I>): Observable<O> = Observable.create { emitter ->
    pages = PublishSubject.create()
    disposable = pages.distinctUntilChanged().concatMapMaybe { it }.subscribe({ result ->
      nextPage = pagingFunction(result)
      emitter.onNext(pageTransformer(result))
      if (nextPage === FINISH_SEQUENCE) {
        pages.onComplete()
      }
    }, emitter::onError, emitter::onComplete)
    pages.onNext(source)
    emitter.setDisposable(disposable)
  }

  /**
   * Returns the last page received from the pager. You may use this to
   * retry that observable in case it failed the first time around.
   */
  fun currentPage(): Observable<O> = page(nextPage)

  /**
   * @return true, if there are more pages to be emitted.
   */
  operator fun hasNext(): Boolean = nextPage !== FINISH_SEQUENCE

  /**
   * Advances the pager by pushing the next page of items into the current observer, is there is one. If the pager
   * has been unsubscribed from or there are no more pages, this method does nothing.
   */
  operator fun next() {
    if (!disposable.isDisposed && hasNext()) {
      pages.onNext(nextPage)
    }
  }

  companion object {

    private val FINISH_SEQUENCE = Maybe.never<Any>()

    fun <T> create(pagingFunction: PagingFunction<T>): Pager<T, T> = Pager(pagingFunction) { it }

    fun <I, O> create(pagingFunction: PagingFunction<I>, pageTransformer: PageTransformFunction<I, O>): Pager<I, O> =
        Pager(pagingFunction, pageTransformer)

    /**
     * Used in the paging function to signal the caller that no more pages are available, i.e.
     * to finish paging by completing the paged sequence.
     *
     * @return the finish token
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> finish(): Maybe<T> = FINISH_SEQUENCE as Maybe<T>
  }
}

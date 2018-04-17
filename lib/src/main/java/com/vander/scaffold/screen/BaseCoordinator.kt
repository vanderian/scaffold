package com.vander.scaffold.screen

import android.support.v7.app.AppCompatActivity
import android.view.View
import butterknife.ButterKnife
import com.squareup.coordinators.Coordinator
import io.reactivex.disposables.CompositeDisposable
import com.vander.scaffold.ui.ActivityFinder
import timber.log.Timber

/**
 * @author marian on 9.8.2017.
 */

abstract class BaseCoordinator : Coordinator() {
  protected val disposable = CompositeDisposable()

  override fun attach(view: View) {
    ButterKnife.bind(this, view)
    Timber.d("coordinator attached %s", javaClass.name)
  }

  override fun detach(view: View) {
    Timber.d("coordinator detached %s", javaClass.name)
    disposable.clear()

  }

  fun activity(view: View):AppCompatActivity = ActivityFinder.get(view)
}

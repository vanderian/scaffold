package com.vander.scaffold.screen

import android.view.View

interface CoordinatorProvider {
  fun provideCoordinator(view: View): Coordinator?
}

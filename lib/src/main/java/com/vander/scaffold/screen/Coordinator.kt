package com.vander.scaffold.screen

import android.view.View
import butterknife.ButterKnife

/**
 * @author marian on 9.8.2017.
 */

open class Coordinator {

  open fun attach(view: View) {
    ButterKnife.bind(this, view)
  }
}

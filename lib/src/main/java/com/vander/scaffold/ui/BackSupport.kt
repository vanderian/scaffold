package com.vander.scaffold.ui

import com.vander.scaffold.screen.Screen

interface HandlesBack {
  fun onBackPressed(): Boolean
}

object BackSupport {
  fun handlesBack(any: Any) = (any as? HandlesBack)?.onBackPressed() ?: false
}

fun Screen<*, *>.handlesBack(): Boolean = BackSupport.handlesBack(this)

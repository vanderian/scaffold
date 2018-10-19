package com.vander.scaffold.ui

interface HandlesBack {
  fun onBackPressed(): Boolean
}

object BackSupport {
  fun handlesBack(any: Any?) = (any as? HandlesBack)?.onBackPressed() ?: false
}

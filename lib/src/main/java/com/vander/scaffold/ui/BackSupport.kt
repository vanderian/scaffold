package com.vander.scaffold.ui

interface HandlesBack {
  fun onBackPressed(): Boolean
}

fun Any.handlesBack(): Boolean = (this as? HandlesBack)?.onBackPressed() ?: false

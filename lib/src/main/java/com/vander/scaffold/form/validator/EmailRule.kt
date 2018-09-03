package com.vander.scaffold.form.validator

import android.support.annotation.StringRes
import android.util.Patterns

open class EmailRule(@StringRes override val errorRes: Int) : ValidateRule() {
  override fun validate(text: String) = Patterns.EMAIL_ADDRESS.matcher(text).matches()
}
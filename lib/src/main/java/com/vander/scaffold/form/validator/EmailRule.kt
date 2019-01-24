package com.vander.scaffold.form.validator

import android.util.Patterns
import androidx.annotation.StringRes

open class EmailRule(@StringRes override val errorRes: Int) : ValidateRule() {
  override fun validate(text: String) = Patterns.EMAIL_ADDRESS.matcher(text).matches()
}
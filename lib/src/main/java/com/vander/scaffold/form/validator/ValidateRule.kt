package com.vander.scaffold.form.validator

import android.support.annotation.StringRes

abstract class ValidateRule {
  @StringRes open val errorRes: Int = -1
  abstract fun validate(text: String): Boolean
}
package com.vander.scaffold.form.validator

import android.support.annotation.StringRes

open class NotEmptyRule(@StringRes override val errorRes: Int) : ValidateRule() {
  override fun validate(text: String) = text.isNotBlank()
}
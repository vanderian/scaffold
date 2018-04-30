package com.vander.scaffold.form.validator

import android.util.Patterns

class EmailRule(override val errorMessage: Int) : ValidateRule {
  override fun validate(text: String?) = Patterns.EMAIL_ADDRESS.matcher(text).matches()
}

annotation class EmailValidation(val msg: Int, val order: Int = -1)
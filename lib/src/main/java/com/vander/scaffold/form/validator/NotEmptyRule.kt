package com.vander.scaffold.form.validator

class NotEmptyRule(override val errorMessage: Int) : ValidateRule {
  override fun validate(text: String?) = text?.isNotBlank() ?: false
}

annotation class NotEmptyValidation(val msg: Int, val order: Int = -1)
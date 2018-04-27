package com.vander.scaffold.form.validator

class NotEmptyRule(override val errorMessage: Int) : ValidateRule {
  override fun validate(text: String?) = text?.isNotBlank() ?: false
}
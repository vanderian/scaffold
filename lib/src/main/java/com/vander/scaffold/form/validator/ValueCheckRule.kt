package com.vander.scaffold.form.validator

class ValueCheckRule(val value: String, override val errorMessage: Int) : ValidateRule {
  override fun validate(text: String?) = text == value
}
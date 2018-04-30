package com.vander.scaffold.form.validator

class ValueCheckRule(val value: String, override val errorMessage: (String) -> String) : ValidateRule() {
  override fun validate(text: String?) = text == value
}
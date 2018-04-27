package com.vander.scaffold.form.validator

interface ValidateRule {
  val errorMessage: Int
  fun validate(text: String?): Boolean
}
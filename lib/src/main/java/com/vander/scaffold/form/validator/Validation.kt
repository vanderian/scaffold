package com.vander.scaffold.form.validator

import android.support.design.widget.TextInputLayout
import com.vander.scaffold.form.validator.ValidateRule

data class Validation(
    val input: TextInputLayout,
    val rules: LinkedHashSet<ValidateRule>
) {
  constructor(input: TextInputLayout, vararg rules: ValidateRule) : this(input, linkedSetOf(*rules))
}
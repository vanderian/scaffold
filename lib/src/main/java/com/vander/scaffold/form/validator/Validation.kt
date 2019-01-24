package com.vander.scaffold.form.validator

import androidx.annotation.IdRes

data class Validation(
    @IdRes val id: Int,
    val rules: LinkedHashSet<ValidateRule>
) {
  constructor(@IdRes id: Int, vararg rules: ValidateRule) : this(id, linkedSetOf(*rules))
}

package com.vander.scaffold.form.validator

import com.vander.scaffold.form.FormResult
import io.reactivex.Single

interface Validator {
  fun validate(): Single<FormResult>
}
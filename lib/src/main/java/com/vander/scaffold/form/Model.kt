package com.vander.scaffold.form

import com.vander.scaffold.screen.Event

typealias FormState = Map<Int, Any>
typealias FormErrors = Map<Int, Int>

data class FormEventInit(val state: FormState) : Event
data class FormEventError(val errors: FormErrors) : Event

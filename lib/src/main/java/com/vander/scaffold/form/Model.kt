package com.vander.scaffold.form

import android.support.annotation.IdRes
import com.vander.scaffold.screen.Event

typealias FormState = Map<Int, ViewState<*>>
typealias FormErrors = Map<Int, Int>

data class FormEventInit(val state: FormState) : Event
data class FormEventError(val errors: FormErrors) : Event

data class ViewState<T>(
    @field:IdRes val id: Int,
    val value: T,
    val enabled: Boolean
)
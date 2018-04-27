package com.vander.scaffold.form.validator

import android.text.Editable
import android.text.TextWatcher

class AfterTextChangedWatcher(private val afterTextChanged: () -> Unit) : TextWatcher {
  override fun afterTextChanged(s: Editable?) {
    afterTextChanged()
  }

  override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

  override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}
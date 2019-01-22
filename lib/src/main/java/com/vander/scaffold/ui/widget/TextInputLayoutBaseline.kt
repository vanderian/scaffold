package com.vander.scaffold.ui.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout

class TextInputLayoutBaseline @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextInputLayout(context, attrs, defStyleAttr) {

  override fun getBaseline(): Int = ((getChildAt(0).layoutParams as MarginLayoutParams).topMargin + (editText?.run { paddingTop + baseline } ?: 0))
}
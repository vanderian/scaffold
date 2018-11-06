package com.vander.scaffold.ui.widget

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.util.AttributeSet

class TextInputLayoutBaseline @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextInputLayout(context, attrs, defStyleAttr) {

  override fun getBaseline(): Int = ((getChildAt(0).layoutParams as MarginLayoutParams).topMargin + (editText?.run { paddingTop + baseline } ?: 0))
}
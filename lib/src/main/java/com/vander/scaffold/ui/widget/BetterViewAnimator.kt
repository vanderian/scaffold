package com.vander.scaffold.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ViewAnimator

class BetterViewAnimator(context: Context?, attrs: AttributeSet?) : ViewAnimator(context, attrs) {
  var displayedChildId
    get() = getChildAt(displayedChild).id
    set(value) {
      if (displayedChildId == value) {
        return
      }
      val child: Int = (0..(childCount - 1)).filter { getChildAt(it).id == value }.single()
      displayedChild = child
    }
}
package com.vander.scaffold.ui.widget.adapter

import android.support.annotation.LayoutRes
import android.view.View

/**
 * @author marian on 15.3.2017.
 */
interface ViewHolderFactory<in T : AdapterModel, R> {
  fun create(@LayoutRes layoutRes: Int, root: View): ViewHolder<T, R>
}
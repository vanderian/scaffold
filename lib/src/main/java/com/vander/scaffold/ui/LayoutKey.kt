package com.vander.scaffold.ui

import androidx.annotation.LayoutRes

/**
 * @author marian on 5.9.2017.
 */
interface LayoutKey {
  @LayoutRes fun layout(): Int
}
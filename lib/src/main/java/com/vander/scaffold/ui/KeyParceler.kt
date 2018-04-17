package com.vander.scaffold.ui

import android.os.Parcelable
import flow.KeyParceler

/**
 * @author marian on 5.9.2017.
 */
class KeyParceler : KeyParceler {
  override fun toParcelable(key: Any): Parcelable = key as Parcelable
  override fun toKey(parcelable: Parcelable): Any = parcelable
}
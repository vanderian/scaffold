package com.vander.scaffold.ui.widget.adapter

import android.support.v7.util.DiffUtil
import java.util.*

class ListSource<T : AdapterModel>(private var list: List<T> = Collections.emptyList()) : DataSource<T>() {

  override val itemCount: Int
    get() = list.size

  override operator fun get(position: Int): T = list[position]

  var items: List<T>
    get() = list
    set(value) {
      val old = list
      list = ArrayList<T>(value)

      val diffResult = DiffUtil.calculateDiff(Callback(old, list), false)
      listUpdateCallback?.apply { diffResult.dispatchUpdatesTo(this) }

    }

  internal class Callback<T : AdapterModel>(
      private val oldList: List<T>,
      private val newList: List<T>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition] == newList[newItemPosition]

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size
  }
}
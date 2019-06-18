package com.vander.scaffold.ui.widget.adapter

import androidx.recyclerview.widget.DiffUtil

class DefaultDiffCallback<T> : DiffUtil.ItemCallback<T>() {
  override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = (oldItem == newItem)
  override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = (oldItem == newItem)
}
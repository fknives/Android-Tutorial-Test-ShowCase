package org.fnives.test.showcase.ui.shared

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class ViewBindingAdapter<T : ViewBinding>(val viewBinding: T) : RecyclerView.ViewHolder(viewBinding.root)

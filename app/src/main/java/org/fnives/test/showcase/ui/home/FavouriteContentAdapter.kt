package org.fnives.test.showcase.ui.home

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import org.fnives.test.showcase.R
import org.fnives.test.showcase.databinding.ItemFavouriteContentBinding
import org.fnives.test.showcase.model.content.ContentId
import org.fnives.test.showcase.model.content.FavouriteContent
import org.fnives.test.showcase.ui.shared.ViewBindingAdapter
import org.fnives.test.showcase.ui.shared.executor.AsyncTaskExecutor
import org.fnives.test.showcase.ui.shared.layoutInflater
import org.fnives.test.showcase.ui.shared.loadRoundedImage

class FavouriteContentAdapter(
    private val listener: OnFavouriteItemClicked,
) : ListAdapter<FavouriteContent, ViewBindingAdapter<ItemFavouriteContentBinding>>(
    AsyncDifferConfig.Builder(DiffUtilItemCallback())
        .setBackgroundThreadExecutor(AsyncTaskExecutor.iOThreadExecutor)
        .build()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewBindingAdapter<ItemFavouriteContentBinding> =
        ViewBindingAdapter(ItemFavouriteContentBinding.inflate(parent.layoutInflater(), parent, false)).apply {
            viewBinding.favouriteCta.setOnClickListener {
                if (adapterPosition in 0 until itemCount) {
                    listener.onFavouriteToggleClicked(getItem(adapterPosition).content.id)
                }
            }
        }

    override fun onBindViewHolder(holder: ViewBindingAdapter<ItemFavouriteContentBinding>, position: Int) {
        val item = getItem(position)
        holder.viewBinding.img.loadRoundedImage(item.content.imageUrl)
        holder.viewBinding.title.text = item.content.title
        holder.viewBinding.description.text = item.content.description
        val favouriteResId = if (item.isFavourite) R.drawable.favorite_24 else R.drawable.favorite_border_24
        holder.viewBinding.favouriteCta.setImageResource(favouriteResId)
    }

    interface OnFavouriteItemClicked {
        fun onFavouriteToggleClicked(contentId: ContentId)
    }

    class DiffUtilItemCallback : DiffUtil.ItemCallback<FavouriteContent>() {
        override fun areItemsTheSame(oldItem: FavouriteContent, newItem: FavouriteContent): Boolean =
            oldItem.content.id == newItem.content.id

        override fun areContentsTheSame(oldItem: FavouriteContent, newItem: FavouriteContent): Boolean =
            oldItem == newItem

        override fun getChangePayload(oldItem: FavouriteContent, newItem: FavouriteContent): Any? = oldItem
    }
}

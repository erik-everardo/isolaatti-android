package com.isolaatti.posting.posts.presentation

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import com.isolaatti.images.common.domain.entity.Image

class PostImagesViewPagerAdapter(private val images: List<Image>) :
    RecyclerView.Adapter<PostImagesViewPagerAdapter.PostImagesViewPagerAdapterItemViewHolder>() {
    class PostImagesViewPagerAdapterItemViewHolder(val imageView: ImageView) : ViewHolder(imageView)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostImagesViewPagerAdapterItemViewHolder {
        return PostImagesViewPagerAdapterItemViewHolder(
            imageView = ImageView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
        )
    }

    override fun getItemCount(): Int = images.size

    override fun onBindViewHolder(holder: PostImagesViewPagerAdapterItemViewHolder, position: Int) {
        val image = images[position]

        holder.imageView.load(image.reducedImageUrl)
    }
}
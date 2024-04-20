package com.isolaatti.images.image_list.presentation

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import coil.load
import com.isolaatti.common.CoilImageLoader.imageLoader
import com.isolaatti.databinding.ImageItemBinding
import com.isolaatti.images.common.domain.entity.Image

class ImagesAdapter(
    private val imageOnClick: ((images: List<Image>, position: Int) -> Unit),
    private val itemWidth: Int,
    private val onImageSelectedCountUpdate: ((count: Int) -> Unit)? = null,
    private val onDeleteMode: ((enabled: Boolean) -> Unit)? = null,
    private val onContentRequested: (() -> Unit)? = null) : ListAdapter<Image, ImagesAdapter.ImageViewHolder>(diffCallback){

    companion object {
        val diffCallback = object: DiffUtil.ItemCallback<Image>() {
            override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean {
                return oldItem == newItem
            }

        }
    }

    var deleteMode: Boolean = false
        set(value) {
            field = value
            if(!value) {
                currentList.forEach { it.delete = false }
            }
            notifyDataSetChanged()
        }

    inner class ImageViewHolder(val imageItemBinding: ImageItemBinding) : RecyclerView.ViewHolder(imageItemBinding.root)

    fun getSelectedImages(): List<Image> {
        return currentList.filter { it.delete }
    }

    override fun onCurrentListChanged(
        previousList: MutableList<Image>,
        currentList: MutableList<Image>
    ) {
        super.onCurrentListChanged(previousList, currentList)
        noMoreContent = (currentList.size - previousList.size) == 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ImageItemBinding.inflate(inflater)

        binding.root.layoutParams = LinearLayout.LayoutParams(itemWidth, itemWidth)
        return ImageViewHolder(binding)
    }

    private var requestedNewContent = false
    private var noMoreContent = false

    /**
     * Call this method when new content has been added on onLoadMore() callback
     */
    fun newContentRequestFinished() {
        requestedNewContent = false
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = getItem(position)

        holder.imageItemBinding.image.load(image.reducedImageUrl, imageLoader)

        if(deleteMode) {
            holder.imageItemBinding.imageCheckbox.visibility = View.VISIBLE
            holder.imageItemBinding.imageOverlay.visibility = View.VISIBLE
            holder.imageItemBinding.root.setOnClickListener {
                holder.imageItemBinding.imageCheckbox.isChecked = !holder.imageItemBinding.imageCheckbox.isChecked
            }
            holder.imageItemBinding.imageCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
                image.delete = isChecked

                onImageSelectedCountUpdate?.invoke(currentList.count { it.delete })
            }
            holder.imageItemBinding.imageCheckbox.isChecked = image.delete
            holder.imageItemBinding.root.setOnLongClickListener(null)
        } else {
            holder.imageItemBinding.imageCheckbox.visibility = View.GONE
            holder.imageItemBinding.imageCheckbox.isChecked = false
            holder.imageItemBinding.imageOverlay.visibility = View.GONE
            holder.imageItemBinding.root.setOnClickListener {
                imageOnClick(currentList, position)
            }
            holder.imageItemBinding.imageCheckbox.setOnCheckedChangeListener(null)
            holder.imageItemBinding.root.setOnLongClickListener {
                image.delete = true
                onDeleteMode?.invoke(true)
                onImageSelectedCountUpdate?.invoke(currentList.count { it.delete })
                true
            }
        }
        val totalItems = currentList.size
        if(totalItems > 0 && !requestedNewContent && !noMoreContent) {
            Log.d("ImagesAdapter", "Total items: $totalItems")
            if(position == totalItems - 1) {
                requestedNewContent = true
                onContentRequested?.invoke()
            }
        }

    }


}
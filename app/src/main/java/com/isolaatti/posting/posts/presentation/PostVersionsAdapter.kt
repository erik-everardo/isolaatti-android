package com.isolaatti.posting.posts.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.isolaatti.R
import com.isolaatti.databinding.ItemPostVersionBinding
import com.isolaatti.posting.posts.data.remote.VersionDto
import io.noties.markwon.Markwon

class PostVersionsAdapter(private val markwon: Markwon) : ListAdapter<VersionDto, PostVersionsAdapter.PostVersionViewHolder>(itemCallback) {

    inner class PostVersionViewHolder(val binding: ItemPostVersionBinding) : ViewHolder(binding.root)
    companion object {
        val itemCallback = object: DiffUtil.ItemCallback<VersionDto>() {
            override fun areItemsTheSame(oldItem: VersionDto, newItem: VersionDto): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: VersionDto, newItem: VersionDto): Boolean {
                return oldItem == newItem
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostVersionViewHolder {
        return PostVersionViewHolder(ItemPostVersionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: PostVersionViewHolder, position: Int) {
        val item = getItem(position)
        markwon.setMarkdown(holder.binding.postVersionContent, item.textContent)
        holder.binding.postVersionDate.text = holder.itemView.context?.getString(R.string.edited_at, item.dateTime.toString())
    }
}
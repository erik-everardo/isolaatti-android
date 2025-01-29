package com.isolaatti.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil3.load
import com.isolaatti.R
import com.isolaatti.common.CoilImageLoader.imageLoader
import com.isolaatti.databinding.ItemUserListBinding
import com.isolaatti.profile.domain.entity.ProfileListItem
import com.isolaatti.utils.UrlGen

class UserListRecyclerViewAdapter(private val callback: UserItemCallback) : Adapter<UserListRecyclerViewAdapter.UserListViewHolder>() {

    private var data: List<ProfileListItem> = listOf()

    inner class UserListViewHolder(val item: ItemUserListBinding) : ViewHolder(item.root)

    fun updateData(newData: List<ProfileListItem>, updateEvent: UpdateEvent) {
        data = newData
        updateEvent.notify(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder {
        return UserListViewHolder(ItemUserListBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: UserListViewHolder, position: Int) {
        val context = holder.itemView.context
        holder.itemView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        holder.item.followButton.isEnabled = true
        data[position].let { user ->
            holder.item.root.setOnClickListener {
                callback.itemClick(user.id)
            }
            holder.item.name.text = user.name
            holder.item.image.load(UrlGen.userProfileImage(user.id), imageLoader)
            if(user.following == true) {
                holder.item.followButton.text = context.getText(R.string.unfollow)
                holder.item.followButton.setTextColor(ResourcesCompat.getColor(context.resources, R.color.danger, null))
                holder.item.followButton.setOnClickListener {
                    it.isEnabled = false
                    callback.followButtonClick(user, UserItemCallback.FollowButtonAction.Unfollow)
                }
            } else {
                holder.item.followButton.text = context.getText(R.string.follow)
                holder.item.followButton.setTextColor(ResourcesCompat.getColor(context.resources, R.color.purple_lighter, null))
                holder.item.followButton.setOnClickListener {
                    it.isEnabled = false
                    callback.followButtonClick(user, UserItemCallback.FollowButtonAction.Follow)
                }
            }
        }
    }
}
package com.isolaatti.notifications.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil3.load
import coil3.request.fallback
import com.isolaatti.R
import com.isolaatti.databinding.NotificationItemBinding
import com.isolaatti.notifications.domain.FollowNotification
import com.isolaatti.notifications.domain.LikeNotification
import com.isolaatti.notifications.domain.Notification
import com.isolaatti.utils.UrlGen

class NotificationsAdapter(
    private val onNotificationClick: (notification: Notification) -> Unit,
    private val onItemOptionsClick: (button: View, notification: Notification) -> Unit
) : ListAdapter<Notification, NotificationsAdapter.NotificationViewHolder>(diffCallback) {
    inner class NotificationViewHolder(val notificationItemBinding: NotificationItemBinding) : ViewHolder(notificationItemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(NotificationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val context = holder.notificationItemBinding.root.context
        val notification = getItem(position)
        holder.notificationItemBinding.root.setOnClickListener {
            onNotificationClick(notification)
        }
        holder.notificationItemBinding.optionButton.setOnClickListener {
            onItemOptionsClick(it, notification)
        }
        when(notification) {
            is LikeNotification -> {
                holder.notificationItemBinding.notificationTitle.text = context.getString(R.string.like_notification_title, notification.authorName)
                holder.notificationItemBinding.notificationMessage.text = context.getString(R.string.like_notification_text)
                val authorProfileImageUrl = notification.authorId?.let { UrlGen.userProfileImage(it, false) }

                if(authorProfileImageUrl != null) {
                    holder.notificationItemBinding.notificationMainImage.load(authorProfileImageUrl){
                        fallback(R.drawable.baseline_person_24)
                    }
                } else {
                    holder.notificationItemBinding.notificationMainImage.load(R.drawable.baseline_person_24)
                }

                holder.notificationItemBinding.notificationSecondaryImage.load(R.drawable.hands_clapping_solid)
            }

            is FollowNotification -> {
                holder.notificationItemBinding.notificationTitle.text = context.getString(R.string.new_follower_notification_title, notification.followerName)
                holder.notificationItemBinding.notificationMessage.text = context.getString(R.string.new_follower_notification_text)

                val followerProfileImageUrl = notification.followerUserId?.let { UrlGen.userProfileImage(it, false) }
                if(followerProfileImageUrl != null) {
                    holder.notificationItemBinding.notificationMainImage.load(followerProfileImageUrl) {
                        fallback(R.drawable.baseline_person_24)
                    }
                } else {
                    holder.notificationItemBinding.notificationMainImage.load(R.drawable.baseline_person_24)
                }

                holder.notificationItemBinding.notificationSecondaryImage.load(R.drawable.baseline_star_24)

            }
        }


    }

    companion object {
        val diffCallback = object: DiffUtil.ItemCallback<Notification>() {
            override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
                return oldItem == newItem
            }
        }
    }
}
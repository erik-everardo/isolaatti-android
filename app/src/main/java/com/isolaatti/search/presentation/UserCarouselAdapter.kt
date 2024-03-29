package com.isolaatti.search.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import com.isolaatti.R
import com.isolaatti.databinding.UsersCarouselItemBinding
import com.isolaatti.search.data.ProfileSearchDto
import com.isolaatti.utils.UrlGen

class UserCarouselAdapter(
    private val onProfileClick: (profileId: Int) -> Unit = {}
) : ListAdapter<ProfileSearchDto, UserCarouselAdapter.UserCarouselItemViewHolder>(itemCallback) {

    companion object {
        val itemCallback = object: DiffUtil.ItemCallback<ProfileSearchDto>() {
            override fun areItemsTheSame(
                oldItem: ProfileSearchDto,
                newItem: ProfileSearchDto
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ProfileSearchDto,
                newItem: ProfileSearchDto
            ): Boolean {
                return oldItem == newItem
            }

        }
    }

    inner class UserCarouselItemViewHolder(val usersCarouselItemBinding: UsersCarouselItemBinding) : ViewHolder(usersCarouselItemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserCarouselItemViewHolder {
        return UserCarouselItemViewHolder(UsersCarouselItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: UserCarouselItemViewHolder, position: Int) {
        val user = getItem(position)

        holder.usersCarouselItemBinding.userCarouselName.text = user.name
        if(user.imageId != null) {
            holder.usersCarouselItemBinding.userCarouselImageView.load(UrlGen.imageUrl(user.imageId))
        } else {
            holder.usersCarouselItemBinding.userCarouselImageView.load(R.drawable.avatar)
        }

        holder.usersCarouselItemBinding.userCarouselCard.setOnClickListener { onProfileClick(user.id) }

    }


}
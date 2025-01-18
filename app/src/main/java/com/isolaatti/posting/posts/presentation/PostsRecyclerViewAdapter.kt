package com.isolaatti.posting.posts.presentation

import android.annotation.SuppressLint
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.text.set
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.isolaatti.R
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.common.CoilImageLoader.imageLoader
import com.isolaatti.common.OnUserInteractedWithPostCallback
import com.isolaatti.databinding.PostLayoutBinding
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.utils.UrlGen.userProfileImage


class PostsRecyclerViewAdapter (
    private val callback: OnUserInteractedWithPostCallback
) : RecyclerView.Adapter<PostsRecyclerViewAdapter.FeedViewHolder>(){
    private var postList: List<Post>? = null
    inner class FeedViewHolder(val itemBinding: PostLayoutBinding) : ViewHolder(itemBinding.root) {
        fun bindView(post: Post, payloads: List<Any>) {

            if(payloads.isNotEmpty()) {
                for(payload in payloads) {
                    when {
                        payload is LikeCountUpdatePayload -> {
                            itemBinding.likeButton.isEnabled = true

                            if(post.liked) {
                                itemBinding.likeButton.setIconTintResource(R.color.purple_lighter)
                                itemBinding.likeButton.setTextColor(itemView.context.getColor(R.color.purple_lighter))
                            } else {
                                itemBinding.likeButton.setIconTintResource(R.color.on_surface)
                                itemBinding.likeButton.setTextColor(itemView.context.getColor(R.color.on_surface))
                            }

                            itemBinding.likeButton.text = post.numberOfLikes.toString()
                        }
                        payload is CommentsCountUpdatePayload -> {
                            itemBinding.commentButton.text = post.numberOfComments.toString()
                        }
                        payload is AudioEventPayload && payload == AudioEventPayload.IsPLaying -> {
                            val audio = post.audio
                            if(audio != null){
                                itemBinding.audio.playButton.icon =
                                    AppCompatResources.getDrawable(
                                        itemView.context,
                                        if(audio.isPlaying) R.drawable.baseline_pause_circle_24 else R.drawable.baseline_play_circle_24
                                    )
                            }
                        }
                        payload is AudioEventPayload && payload == AudioEventPayload.ProgressChanged -> {
                            val audio = post.audio
                            if(audio != null){
                                itemBinding.audio.audioProgress.progress = audio.progress
                            }
                        }
                        payload is AudioEventPayload && payload == AudioEventPayload.IsLoading -> {
                            val audio = post.audio
                            if(audio != null){
                                itemBinding.audio.audioProgress.isIndeterminate = audio.isLoading
                            }
                        }
                        payload is AudioEventPayload && payload == AudioEventPayload.DurationChanged -> {
                            val audio = post.audio
                            if(audio != null){
                                itemBinding.audio.audioProgress.max = audio.duration
                            }
                        }
                        payload is AudioEventPayload && payload == AudioEventPayload.Ended -> {
                            val audio = post.audio
                            if(audio != null){
                                itemBinding.audio.audioProgress.progress = 0
                                itemBinding.audio.playButton.icon = AppCompatResources.getDrawable(itemView.context, R.drawable.baseline_play_circle_24)
                            }
                        }
                    }
                }

            } else {
                val username: TextView = itemView.findViewById(R.id.text_view_username)
                username.text = post.userName
                username.setOnClickListener {
                    callback.onProfileClick(post.userId)
                }

                val profileImageView: ImageView = itemView.findViewById(R.id.avatar_picture)
                profileImageView.load(userProfileImage(post.userId), imageLoader)

                val dateTextView: TextView = itemView.findViewById(R.id.text_view_date)
                dateTextView.text = post.date

                val content: TextView = itemView.findViewById(R.id.post_content)
                content.movementMethod = LinkMovementMethod.getInstance()
                val spannableString = SpannableString(post.textContent).apply {
                    post.hashtagsSpans.forEach {
                        set(it.first, it.last + 1, object: ClickableSpan() {
                            override fun onClick(widget: View) {
                                callback.hashtagClicked(post.textContent.substring(it.first + 1, it.last + 1))
                            }
                        })
                    }
                }
                content.text = spannableString

                itemBinding.likeButton.isEnabled = true

                if(post.liked) {
                    itemBinding.likeButton.setIconTintResource(R.color.purple_lighter)
                    itemBinding.likeButton.setTextColor(itemView.context.getColor(R.color.purple_lighter))
                } else {
                    itemBinding.likeButton.setIconTintResource(R.color.on_surface)
                    itemBinding.likeButton.setTextColor(itemView.context.getColor(R.color.on_surface))
                }

                itemBinding.likeButton.text = post.numberOfLikes.toString()

                itemBinding.commentButton.text = post.numberOfComments.toString()

                val moreButton: MaterialButton = itemView.findViewById(R.id.more_button)
                moreButton.setOnClickListener {
                    callback.onOptions(post)
                }

                itemBinding.likeButton.setOnClickListener {
                    itemBinding.likeButton.isEnabled = false
                    if(post.liked){
                        callback.onUnLiked(post.id)
                    } else {
                        callback.onLiked(post.id)
                    }
                }
                itemBinding.commentButton.setOnClickListener {
                    callback.onComment(post.id)
                }

                itemView.findViewById<MaterialCardView>(R.id.card).setOnClickListener {
                    callback.onOpenPost(post.id)
                }
                if(post.audio != null){
                    itemBinding.audio.apply {
                        root.visibility = View.VISIBLE
                        textViewDescription.text = post.audio.name
                    }
                    itemBinding.audio.playButton.setOnClickListener {
                        callback.onPlay(post.audio)
                    }
                } else {
                    itemBinding.audio.root.visibility = View.GONE
                    itemBinding.audio.playButton.setOnClickListener(null)
                }

                if(post.images.isNotEmpty()) {
                    itemBinding.photosViewPager.isVisible = true
                    itemBinding.photosViewPager.adapter = PostImagesViewPagerAdapter(post.images)
                } else {
                    itemBinding.photosViewPager.isVisible = false
                    itemBinding.photosViewPager.adapter = null
                }
                itemBinding.shareButton.setOnClickListener {
                    callback.onShare(post.id)
                }
                itemBinding.infoButton.setOnClickListener {
                    callback.onMoreInfo(post.id)
                }
            }
        }
    }


    data class LikeCountUpdatePayload(val likeCount: Int)
    data class CommentsCountUpdatePayload(val commentsCount: Int)

    private var currentAudio: Audio? = null
    private var currentAudioPosition: Int = -1
    enum class AudioEventPayload {
        ProgressChanged, IsLoading, IsPLaying, DurationChanged, Ended
    }

    fun setIsPlaying(isPlaying: Boolean, audio: Audio) {
        if(audio == currentAudio) {
            currentAudio?.isPlaying = isPlaying
            if(currentAudioPosition > -1) {
                notifyItemChanged(currentAudioPosition, AudioEventPayload.IsPLaying)
            }
            return
        }
        currentAudio?.isPlaying = false

        if(currentAudioPosition > -1) {
            notifyItemChanged(currentAudioPosition, AudioEventPayload.IsPLaying)
        } else {
            if(postList != null) {
                for((index, post) in postList!!.withIndex()){
                    post.audio?.isPlaying = false
                    post.audio?.progress = 0
                    post.audio?.isLoading = false
                    if(post.audio != null) {
                        notifyItemChanged(index)
                    }
                }

            }
        }

        currentAudioPosition = postList?.indexOf(postList?.find { it.audio == audio }) ?: -1
        Log.d(LOG_TAG, "setIsPlaying currentAudioPosition: $currentAudioPosition")

        if(currentAudioPosition > -1) {
            currentAudio = postList?.get(currentAudioPosition)?.audio?.also { it.isPlaying = isPlaying }
            notifyItemChanged(currentAudioPosition, AudioEventPayload.IsPLaying)
        }
    }

    fun setIsLoading(isLoading: Boolean, audio: Audio) {
        if(audio == currentAudio) {
            currentAudio?.isLoading = isLoading
            if(currentAudioPosition > -1) {
                notifyItemChanged(currentAudioPosition, AudioEventPayload.IsLoading)
            }
            return
        }
        currentAudio?.isPlaying = false
        currentAudio?.isLoading = false

        if(currentAudioPosition > -1) {
            notifyItemChanged(currentAudioPosition, AudioEventPayload.IsLoading)
        }

        currentAudioPosition = postList?.indexOf(postList?.find { it.audio == audio }) ?: -1

        Log.d(LOG_TAG, "setIsLoading currentAudioPosition: $currentAudioPosition")

        if(currentAudioPosition > -1) {
            postList?.get(currentAudioPosition)?.audio?.isLoading = isLoading
            notifyItemChanged(currentAudioPosition, AudioEventPayload.IsLoading)
        }
    }

    fun setProgress(progress: Int, audio: Audio){
        if(audio == currentAudio) {
            audio.progress = progress
            if(currentAudioPosition > -1) {
                notifyItemChanged(currentAudioPosition, AudioEventPayload.ProgressChanged)
            }
            return
        }
        currentAudio?.isPlaying = false
        currentAudio?.progress = 0

        if(currentAudioPosition > -1) {
            notifyItemChanged(currentAudioPosition, AudioEventPayload.ProgressChanged)
        }

        currentAudioPosition = postList?.indexOf(postList?.find { it.audio == audio }) ?: -1


        if(currentAudioPosition > -1) {
            postList?.get(currentAudioPosition)?.audio?.progress = progress
            notifyItemChanged(currentAudioPosition, AudioEventPayload.ProgressChanged)
        }
    }

    fun setDuration(duration: Int, audio: Audio) {
        if(audio == currentAudio) {
            audio.duration = duration
            if(currentAudioPosition > -1) {
                notifyItemChanged(currentAudioPosition, AudioEventPayload.ProgressChanged)
            }
            return
        }
        currentAudio?.isPlaying = false

        currentAudioPosition = postList?.indexOf(postList?.find { it.audio == audio }) ?: -1

        if(currentAudioPosition > -1) {
            postList?.get(currentAudioPosition)?.audio?.duration = duration
            notifyItemChanged(currentAudioPosition, AudioEventPayload.ProgressChanged)
        }
    }

    fun setEnded(audio: Audio) {
        if(audio == currentAudio) {
            audio.isPlaying = false
            if(currentAudioPosition > -1) {
                notifyItemChanged(currentAudioPosition, AudioEventPayload.ProgressChanged)
            }
            return
        }
        currentAudio?.isPlaying = false

        if(currentAudioPosition > -1) {
            notifyItemChanged(currentAudioPosition, AudioEventPayload.ProgressChanged)
        }

        currentAudioPosition = postList?.indexOf(postList?.find { it.audio == audio }) ?: -1

        if(currentAudioPosition > -1) {
            postList?.get(currentAudioPosition)?.audio?.isPlaying = false
            notifyItemChanged(currentAudioPosition, AudioEventPayload.ProgressChanged)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        return FeedViewHolder(PostLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    var previousSize = 0
    override fun getItemCount(): Int = postList?.size ?: 0


    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(true)
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(updatedFeed: List<Post>, updateEvent: UpdateEvent? = null) {
        if(updateEvent == null) {
            postList = updatedFeed

            notifyDataSetChanged()
            return
        }
        val postUpdated = updateEvent.affectedPosition?.let {
            if(updateEvent.updateType == UpdateEvent.UpdateType.POST_REMOVED) {
                null
            } else {
                postList?.get(it)
            }
        }
        val position = updateEvent.affectedPosition

        previousSize = itemCount
        postList = updatedFeed

        when(updateEvent.updateType) {
            UpdateEvent.UpdateType.POST_LIKED -> {
                if(postUpdated != null && position != null)
                    notifyItemChanged(position, LikeCountUpdatePayload(postUpdated.numberOfLikes))
            }
            UpdateEvent.UpdateType.POST_COMMENTED -> {
                if(postUpdated != null && position != null)
                    notifyItemChanged(position, CommentsCountUpdatePayload(postUpdated.numberOfComments))
            }
            UpdateEvent.UpdateType.POST_REMOVED -> {
                if(position != null)
                    notifyItemRemoved(position)
            }
            UpdateEvent.UpdateType.POST_ADDED -> {
                notifyItemInserted(0)
            }

            UpdateEvent.UpdateType.PAGE_ADDED -> {
                notifyItemInserted(previousSize)
            }
            UpdateEvent.UpdateType.REFRESH -> {
                notifyDataSetChanged()
            }
        }
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {}

    private var requestedNewContent = false

    /**
     * Call this method when new content has been added on onLoadMore() callback
     */
    fun newContentRequestFinished() {
        requestedNewContent = false
    }
    override fun onBindViewHolder(holder: FeedViewHolder, position: Int, payloads: List<Any>) {
        holder.bindView(postList?.get(position) ?: return, payloads)
        val totalItems = postList?.size
        if(totalItems != null && totalItems > 0 && !requestedNewContent) {
            if(position == totalItems - 1) {
                requestedNewContent = true
                if(payloads.isEmpty()) {
                    callback.onLoadMore()
                }
            }
        }

    }

    companion object {
        const val LOG_TAG = "PostsRecyclerViewAdapter"
    }
}
package com.isolaatti.posting.comments.presentation

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.isolaatti.common.CoilImageLoader.imageLoader
import com.isolaatti.databinding.CommentLayoutBinding
import com.isolaatti.posting.comments.domain.model.Comment
import com.isolaatti.common.OnUserInteractedCallback
import com.isolaatti.utils.UrlGen
import io.noties.markwon.Markwon

class CommentsRecyclerViewAdapter(
    private var list: List<Comment>,
    private val markwon: Markwon,
    private val callback: OnUserInteractedCallback,
    private val onCommentClick: (Comment) -> Unit
) : RecyclerView.Adapter<CommentsRecyclerViewAdapter.CommentViewHolder>() {

    private var previousSize = 0
    var blockInfiniteScroll = false

    inner class CommentViewHolder(val viewBinding: CommentLayoutBinding) : RecyclerView.ViewHolder(viewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(CommentLayoutBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun getItemCount(): Int = list.count()

    private var requestedNewContent = false

    /**
     * Call this method when new content has been added on onLoadMore() callback
     */
    fun newContentRequestFinished() {
        requestedNewContent = false
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = list[position]

        holder.viewBinding.textViewDate.text = comment.date
        markwon.setMarkdown(holder.viewBinding.postContent, comment.textContent)
        holder.viewBinding.textViewUsername.text = comment.username
        holder.viewBinding.textViewUsername.setOnClickListener {
            callback.onProfileClick(comment.userId)
        }
        holder.viewBinding.moreButton.setOnClickListener {
            callback.onOptions(comment)
        }

        holder.viewBinding.avatarPicture.load(UrlGen.userProfileImage(comment.userId), imageLoader)

        val totalItems = list.size
        if(totalItems > 0 && !requestedNewContent) {
            if(position == totalItems - 1 && !blockInfiniteScroll) {
                requestedNewContent = true
                callback.onLoadMore()
            }

        }
    }



    @SuppressLint("NotifyDataSetChanged")
    fun updateList(updatedList: List<Comment>, updateEvent: UpdateEvent? = null) {

        if(updateEvent == null) {
            list = updatedList

            notifyDataSetChanged()
            return
        }


        val position = updateEvent.affectedPosition
        previousSize = itemCount
        list = updatedList

        when(updateEvent.updateType) {

            UpdateEvent.UpdateType.COMMENT_REMOVED -> {
                if(position != null)
                    notifyItemRemoved(position)
            }
            UpdateEvent.UpdateType.COMMENT_ADDED_TOP -> {
                notifyItemInserted(0)
            }

            UpdateEvent.UpdateType.COMMENT_PAGE_ADDED_BOTTOM -> {
                notifyItemInserted(previousSize)
            }
            UpdateEvent.UpdateType.REFRESH -> {
                notifyDataSetChanged()
            }

            UpdateEvent.UpdateType.COMMENT_UPDATED ->  {
                if(updateEvent.affectedPosition != null) {
                    notifyItemChanged(updateEvent.affectedPosition)
                }
            }
        }
    }
}
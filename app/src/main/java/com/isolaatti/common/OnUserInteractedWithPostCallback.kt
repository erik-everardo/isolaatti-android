package com.isolaatti.common

import com.isolaatti.audio.common.domain.Audio

interface OnUserInteractedWithPostCallback : OnUserInteractedCallback {
    fun onLiked(postId: Long)
    fun onUnLiked(postId: Long)
    fun onComment(postId: Long)
    fun onOpenPost(postId: Long)
    fun onPlay(audio: Audio)
    fun onMoreInfo(postId: Long)
    fun onShare(postId: Long)
}
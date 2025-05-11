package com.isolaatti.posting.posts.domain

import android.net.Uri
import com.isolaatti.images.common.domain.entity.LocalImage
import com.isolaatti.images.common.domain.entity.RemoteImage
import com.isolaatti.posting.posts.data.remote.FeedFilterDto
import com.isolaatti.posting.posts.data.remote.PostDeletedDto
import com.isolaatti.posting.posts.data.remote.VersionDto
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.profile.domain.entity.ProfileListItem
import com.isolaatti.utils.Resource
import com.isolaatti.utils.ResourceWithSteps
import kotlinx.coroutines.flow.Flow

interface PostsRepository {

    fun getFeed(lastId: Long, hashtag: String?): Flow<Resource<MutableList<Post>>>

    fun getProfilePosts(userId: Int, lastId: Long, olderFirst: Boolean, filter: FeedFilterDto?): Flow<Resource<MutableList<Post>>>

    fun makePost(privacy: PostPrivacy, text: String, squadId: String?, images: List<LocalImage>, audio: Uri?): Flow<ResourceWithSteps<Post, PostingSteps>>
    fun editPost(postId: Long, privacy: PostPrivacy, text: String, squadId: String?, images: List<LocalImage>, imagesToRemove: List<RemoteImage>, audio: Uri?, removeOldAudio: Boolean): Flow<ResourceWithSteps<Post, PostingSteps>>
    fun deletePost(postId: Long): Flow<Resource<PostDeletedDto>>
    fun loadPost(postId: Long): Flow<Resource<Post>>

    fun getUsersLikedPost(postId: Long): Flow<Resource<List<ProfileListItem>>>
    fun getPostVersions(postId: Long): Flow<Resource<List<VersionDto>>>
}
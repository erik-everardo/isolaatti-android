package com.isolaatti.squads

import com.isolaatti.images.common.domain.entity.Image
import com.isolaatti.posting.posts.domain.entity.Post

sealed interface SquadAction {
    data object ClickOnSquadImage : SquadAction
    data object DismissPictureDialog : SquadAction
    data object DismissConfirmChangePictureDialog : SquadAction
    data object AcceptChangePictureDialog : SquadAction
    data object RefreshPosts : SquadAction
    data object LoadPostsNextPage : SquadAction
    data object RefreshSquad : SquadAction
    data object UploadSquadPictureFromGallery : SquadAction
    data object TakeSquadPictureFromCamera : SquadAction
    data class OpenPost(val postId: Long) : SquadAction
    data class OpenProfile(val userId: Long) : SquadAction
    data class OpenHashtag(val hashtag: String) : SquadAction
    data class OpenPictures(val image: Image, val images: List<Image>) : SquadAction
    data object NewPost : SquadAction

    data class PostCreated(val post: Post) : SquadAction

}
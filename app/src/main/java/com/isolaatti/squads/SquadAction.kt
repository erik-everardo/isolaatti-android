package com.isolaatti.squads

import com.isolaatti.images.common.domain.entity.Image
import com.isolaatti.images.common.domain.entity.LocalImage
import com.isolaatti.posting.posts.domain.entity.Post

sealed interface SquadAction {
    data object ClickOnSquadImage : SquadAction
    data object DismissPictureDialog : SquadAction
    data object DismissConfirmChangePictureDialog : SquadAction
    data object AcceptChangePictureDialog : SquadAction
    data object RefreshSquad : SquadAction
    data object UploadSquadPictureFromGallery : SquadAction
    data object TakeSquadPictureFromCamera : SquadAction
    data class OpenHashtag(val hashtag: String) : SquadAction
    data class OpenPictures(val image: Image, val images: List<Image>) : SquadAction
    data object NewPost : SquadAction
    data class ExtendedDescriptionEdited(val description: String) : SquadAction
    data class PostCreated(val post: Post) : SquadAction
    data class ImageForSquadSelected(val image: LocalImage) : SquadAction
}
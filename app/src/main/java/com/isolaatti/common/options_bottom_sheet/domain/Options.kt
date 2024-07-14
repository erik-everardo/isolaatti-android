package com.isolaatti.common.options_bottom_sheet.domain

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.isolaatti.R

data class Options(
    @StringRes val title: Int,
    val id: Int,
    val items: List<Option>
) {
    data class Option(
        @StringRes val stringRes: Int,
        @DrawableRes val icon: Int,
        val optionId: Int
    ) {
        companion object {
            const val OPTION_DELETE = 1
            const val OPTION_EDIT = 2
            const val OPTION_REPORT = 3
            const val OPTION_SAVE = 4
            const val OPTION_SNAPSHOT = 5
            const val OPTION_PROFILE_PHOTO_VIEW_PHOTO = 6
            const val OPTION_PROFILE_PHOTO_CHANGE_PHOTO = 7
            const val OPTION_PROFILE_PHOTO_REMOVE_PHOTO = 8
            const val OPTION_PROFILE_DESCRIPTION_SET_AUDIO = 9
        }
    }

    companion object {
        const val POST_OPTIONS = 1
        const val COMMENT_OPTIONS = 2
        const val PROFILE_PHOTO_OPTIONS = 3
        const val PROFILE_DESCRIPTION_OPTIONS = 4

        val noOptions = Options(0, 0, listOf())

        fun getPostsOptions(userOwned: Boolean, savable: Boolean, snapshotAble: Boolean): Options {
            val list = mutableListOf(
                Option(R.string.report, R.drawable.baseline_report_24, Option.OPTION_REPORT)
            )

            if(userOwned) {
                list.addAll(listOf(
                    Option(R.string.delete, R.drawable.baseline_delete_24, Option.OPTION_DELETE),
                    Option(R.string.edit, R.drawable.baseline_edit_24, Option.OPTION_EDIT)
                )
                )
            }

            if(savable) {
                list.add(Option(R.string.save, R.drawable.baseline_save_24, Option.OPTION_SAVE))
            }

            if(snapshotAble) {
                list.add(Option(R.string.save_snapshot, R.drawable.baseline_download_24, Option.OPTION_SNAPSHOT))
            }

            return Options(R.string.post_options_title, POST_OPTIONS, list)
        }

        fun getCommentOptions(userOwned: Boolean, savable: Boolean, snapshotAble: Boolean): Options {
            val list = mutableListOf(
                Option(R.string.report, R.drawable.baseline_report_24, Option.OPTION_REPORT)
            )

            if(userOwned) {
                list.addAll(listOf(
                    Option(R.string.delete, R.drawable.baseline_delete_24, Option.OPTION_DELETE),
                    Option(R.string.edit, R.drawable.baseline_edit_24, Option.OPTION_EDIT)
                )
                )
            }

            if(savable) {
                list.add(Option(R.string.save, R.drawable.baseline_save_24, Option.OPTION_SAVE))
            }

            if(snapshotAble) {
                list.add(Option(R.string.save_snapshot, R.drawable.baseline_download_24, Option.OPTION_SNAPSHOT))
            }

            return Options(R.string.comment_options, COMMENT_OPTIONS, list)
        }

        fun getProfilePhotoOptions(userOwned: Boolean): Options {
            val list = mutableListOf(Option(R.string.view_photo, R.drawable.baseline_image_24, Option.OPTION_PROFILE_PHOTO_VIEW_PHOTO))

            if(userOwned) {
                list.addAll(listOf(
                    Option(R.string.change_profile_photo, R.drawable.baseline_edit_24, Option.OPTION_PROFILE_PHOTO_CHANGE_PHOTO),
                    Option(R.string.remove_photo, R.drawable.baseline_remove_circle_24, Option.OPTION_PROFILE_PHOTO_REMOVE_PHOTO)
                ))
            }

            return Options(R.string.profile_photo,PROFILE_PHOTO_OPTIONS, list)
        }

        fun getProfileDescriptionOptions(): Options {
            val list = listOf(Option(R.string.set_audio_description, R.drawable.baseline_audio_file_24, Option.OPTION_PROFILE_DESCRIPTION_SET_AUDIO))
            return Options(R.string.description, PROFILE_DESCRIPTION_OPTIONS, list)
        }
    }
}
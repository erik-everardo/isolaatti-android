package com.isolaatti.posting.post_creator.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import com.isolaatti.posting.post_creator.ui.CreatePostActivity
import com.isolaatti.posting.posts.domain.entity.Post
import kotlinx.parcelize.Parcelize

class CreatePostContract : ActivityResultContract<CreatePostContract.Input, Post?>() {

    @Parcelize
    data class Input(
        val squadId: String? = null,
        val squadName: String? = null
    ) : Parcelable

    override fun createIntent(context: Context, input: Input): Intent {
        return Intent(context, CreatePostActivity::class.java).apply {
            putExtra(CreatePostActivity.Companion.EXTRA_INPUT, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Post? {
        return if(resultCode == Activity.RESULT_OK) {
            intent?.extras?.getParcelable(CreatePostActivity.Companion.EXTRA_KEY_POST_POSTED)
        } else {
            null
        }
    }
}
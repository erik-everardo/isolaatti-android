package com.isolaatti.posting.post_creator.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.isolaatti.posting.post_creator.ui.CreatePostActivity
import com.isolaatti.posting.posts.domain.entity.Post

class EditPostContract : ActivityResultContract<Long, Post?>() {
    override fun createIntent(context: Context, input: Long): Intent {
        return Intent(context, CreatePostActivity::class.java).apply {
            putExtra(CreatePostActivity.Companion.EXTRA_KEY_MODE, CreatePostActivity.Companion.EXTRA_MODE_EDIT)
            putExtra(CreatePostActivity.Companion.EXTRA_KEY_POST_ID, input)
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
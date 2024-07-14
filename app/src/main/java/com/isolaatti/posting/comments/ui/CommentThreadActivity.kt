package com.isolaatti.posting.comments.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.isolaatti.R
import com.isolaatti.databinding.ActivityCommentThreadBinding

class CommentThreadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCommentThreadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentThreadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val commentId = intent.extras?.getLong(EXTRA_COMMENT_ID)

        if(commentId == null || commentId == 0L) {
            Toast.makeText(this, R.string.invalid_arg, Toast.LENGTH_SHORT).show()
            finish()
        } else {
            (supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment)
                .navController.setGraph(R.navigation.comment_thread_navigation,
                    Bundle().apply {
                        putLong(CommentThreadFragment.ARG_COMMENT_ID, commentId)
                    })
        }
    }

    companion object {
        private const val EXTRA_COMMENT_ID = "commentId"
        fun startActivity(context: Context, commentId: Long) {
            val intent = Intent(context, CommentThreadActivity::class.java).apply {
                putExtra(EXTRA_COMMENT_ID, commentId)
            }

            context.startActivity(intent)
        }
    }
}
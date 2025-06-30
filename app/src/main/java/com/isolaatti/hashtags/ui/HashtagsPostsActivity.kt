package com.isolaatti.hashtags.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import com.isolaatti.R
import com.isolaatti.databinding.ActivityPostsHashtagBinding
import com.isolaatti.posting.posts.ui.PostListingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HashtagsPostsActivity : AppCompatActivity() {
    lateinit var binding: ActivityPostsHashtagBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityPostsHashtagBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setupListeners()
        val hashtag = intent.extras?.getString(EXTRA_HASHTAG)!!
        binding.toolbar.title = "#$hashtag"
        (supportFragmentManager.findFragmentById(R.id.post_list_fragment_container) as NavHostFragment)
            .navController.setGraph(R.navigation.post_listing_navigation, Bundle().apply { putString(
                PostListingFragment.ARG_HASHTAG, hashtag) })

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->

            val padding = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.toolbar.updatePadding(top = padding.top, left = padding.left, right = padding.right)

            insets
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    companion object {
        const val EXTRA_HASHTAG = "hashtag"
        fun startActivity(context: Context, hashtag: String) {
            val intent = Intent(context, HashtagsPostsActivity::class.java)
            intent.putExtra(EXTRA_HASHTAG, hashtag)

            context.startActivity(intent)
        }
    }
}
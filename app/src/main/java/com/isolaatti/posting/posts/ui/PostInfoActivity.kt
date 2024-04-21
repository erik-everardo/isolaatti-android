package com.isolaatti.posting.posts.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import com.isolaatti.R
import com.isolaatti.common.IsolaattiBaseActivity
import com.isolaatti.databinding.ActivityPostInfoBinding
import com.isolaatti.posting.posts.presentation.PostInfoViewPagerAdapter

class PostInfoActivity : IsolaattiBaseActivity() {
    private lateinit var binding: ActivityPostInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPostInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val postId = intent.extras?.getLong(EXTRA_POST_ID) ?: 0
        if(postId != 0L) {
            binding.viewPagerPostInfo.adapter = PostInfoViewPagerAdapter(this, postId)

            TabLayoutMediator(binding.tabsPostInfo, binding.viewPagerPostInfo) { tab, position ->
                if(position == 0) {
                    tab.text = getString(R.string.people_who_clapped)
                } else {
                    tab.text = getString(R.string.post_history)
                }
            }.attach()
        } else {
            finish()
        }

    }

    companion object {
        const val EXTRA_POST_ID = "postId"
        fun startActivity(context: Context, postId: Long) {
            val intent = Intent(context, PostInfoActivity::class.java).apply {
                putExtra(EXTRA_POST_ID, postId)
            }
            context.startActivity(intent)
        }
    }
}
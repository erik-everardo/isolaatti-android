package com.isolaatti.posting.posts.presentation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.isolaatti.posting.posts.ui.PostLikesFragment
import com.isolaatti.posting.posts.ui.PostVersionsFragment

class PostInfoViewPagerAdapter(fragmentActivity: FragmentActivity, private val postId: Long) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment = if(position == 0) {
        PostLikesFragment.getInstance(postId)
    } else{
        PostVersionsFragment.getInstance(postId)
    }
}
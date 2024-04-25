package com.isolaatti.followers.presentation

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.isolaatti.followers.ui.FollowersFragment
import com.isolaatti.followers.ui.FollowingFragment
import com.isolaatti.profile.profile_listing.ui.ProfileListingFragment

class FollowersViewPagerAdapter(fragment: Fragment, private val userId: Int) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        if(position == 0) {
            return ProfileListingFragment.getInstanceForFollowers(userId)
        }

        return ProfileListingFragment.getInstanceForFollowing(userId)
    }
}
package com.isolaatti.profile.profile_listing.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.isolaatti.common.UserItemCallback
import com.isolaatti.common.UserListRecyclerViewAdapter
import com.isolaatti.databinding.FragmentUserListBinding
import com.isolaatti.profile.domain.entity.ProfileListItem
import com.isolaatti.profile.profile_listing.presentation.ProfileListingViewModel
import com.isolaatti.profile.ui.ProfileActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileListingFragment : Fragment(), UserItemCallback {

    private val viewModel: ProfileListingViewModel by viewModels()
    private lateinit var binding: FragmentUserListBinding
    private var mode: Int = 0

    private lateinit var adapter: UserListRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mode = arguments?.getInt(ARG_MODE) ?: 0
        when(mode) {
            FOLLOWING, FOLLOWERS -> viewModel.userId = arguments?.getInt(ARG_USER_ID) ?: 0
            POST_LIKES -> viewModel.postId = arguments?.getLong(ARG_POST_ID) ?: 0
        }
        loadData()

    }

    private fun loadData(refresh: Boolean = false) {
        when(mode) {
            FOLLOWING -> viewModel.fetchFollowings(refresh)
            FOLLOWERS -> viewModel.fetchFollowers(refresh)
            POST_LIKES -> viewModel.fetchPostLikedBy(refresh)
            BROWSE_PROFILES -> {}
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = UserListRecyclerViewAdapter(this)
        binding.recyclerUsers.adapter = adapter
        binding.recyclerUsers.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        viewModel.users.observe(viewLifecycleOwner) { (list, updateEvent) ->
            adapter.updateData(list, updateEvent)
            binding.swipeToRefresh.isRefreshing = false
        }

        binding.swipeToRefresh.setOnRefreshListener {
            loadData(refresh = true)
        }
    }

    companion object {
        private const val ARG_MODE = "mode"
        private const val ARG_USER_ID = "userId"
        private const val ARG_POST_ID = "postId"
        const val FOLLOWERS = 1
        const val FOLLOWING = 2
        const val POST_LIKES = 3
        const val BROWSE_PROFILES = 4

        fun getInstanceForFollowers(userId: Int): ProfileListingFragment {
            return ProfileListingFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_MODE, FOLLOWERS)
                    putInt(ARG_USER_ID, userId)
                }
            }
        }

        fun getInstanceForFollowing(userId: Int): ProfileListingFragment {
            return ProfileListingFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_MODE, FOLLOWING)
                    putInt(ARG_USER_ID, userId)
                }
            }
        }

        fun getInstanceForPostLikes(postId: Long): ProfileListingFragment {
            return ProfileListingFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_MODE, POST_LIKES)
                    putLong(ARG_POST_ID, postId)
                }
            }
        }

        fun getInstanceForBrowseProfile(): ProfileListingFragment {
            return ProfileListingFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_MODE, BROWSE_PROFILES)
                }
            }
        }
    }

    override fun itemClick(userId: Int) {
        ProfileActivity.startActivity(requireContext(), userId)
    }

    override fun followButtonClick(
        user: ProfileListItem,
        action: UserItemCallback.FollowButtonAction
    ) {
        when(action) {
            UserItemCallback.FollowButtonAction.Follow -> viewModel.followUser(user)
            UserItemCallback.FollowButtonAction.Unfollow -> viewModel.unfollowUser(user)
        }
    }
}
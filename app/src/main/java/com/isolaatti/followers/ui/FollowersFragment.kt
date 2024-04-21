package com.isolaatti.followers.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.isolaatti.common.UserItemCallback
import com.isolaatti.common.UserListRecyclerViewAdapter
import com.isolaatti.databinding.FragmentUserListBinding
import com.isolaatti.followers.presentation.FollowersViewModel
import com.isolaatti.profile.domain.entity.ProfileListItem
import com.isolaatti.profile.ui.ProfileActivity

class FollowersFragment : Fragment(), UserItemCallback {
    private lateinit var binding: FragmentUserListBinding
    private val viewModel: FollowersViewModel by viewModels({ requireParentFragment() })
    private lateinit var adapter: UserListRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserListBinding.inflate(inflater)
        return binding.root
    }

    private fun setObservers() {
        viewModel.followers.observe(viewLifecycleOwner) { (list, updateEvent) ->
            adapter.updateData(list, updateEvent)
            binding.swipeToRefresh.isRefreshing = false
        }
    }

    private fun bind() {
        adapter = UserListRecyclerViewAdapter(this)
        binding.recyclerUsers.adapter = adapter
        binding.recyclerUsers.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.swipeToRefresh.setOnRefreshListener {
            viewModel.fetchFollowers(refresh = true)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bind()
        setObservers()

        viewModel.fetchFollowers()
    }

    override fun itemClick(userId: Int) {
        ProfileActivity.startActivity(requireContext(), userId)
    }

    override fun followButtonClick(user: ProfileListItem, action: UserItemCallback.FollowButtonAction) {
        when(action) {
            UserItemCallback.FollowButtonAction.Follow -> viewModel.followUser(user)
            UserItemCallback.FollowButtonAction.Unfollow -> viewModel.unfollowUser(user)
        }
    }
}
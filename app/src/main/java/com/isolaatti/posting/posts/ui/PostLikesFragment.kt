package com.isolaatti.posting.posts.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.isolaatti.common.ListUpdateEvent
import com.isolaatti.common.UpdateEvent
import com.isolaatti.common.UserItemCallback
import com.isolaatti.common.UserListRecyclerViewAdapter
import com.isolaatti.databinding.FragmentUserListBinding
import com.isolaatti.posting.posts.presentation.PostLikesViewModel
import com.isolaatti.profile.domain.entity.ProfileListItem
import com.isolaatti.profile.ui.ProfileActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostLikesFragment : Fragment() {
    private lateinit var binding: FragmentUserListBinding
    private val viewModel: PostLikesViewModel by viewModels()
    private var adapter: UserListRecyclerViewAdapter? = null
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
        viewModel.postId = arguments?.getLong(ARG_POST_ID) ?: 0

        if(viewModel.postId != 0L)
            viewModel.getUsers()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = UserListRecyclerViewAdapter(object: UserItemCallback {
            override fun itemClick(userId: Int) {
                ProfileActivity.startActivity(requireContext(), userId)
            }

            override fun followButtonClick(
                user: ProfileListItem,
                action: UserItemCallback.FollowButtonAction
            ) {

            }

        })
        binding.recyclerUsers.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerUsers.adapter = adapter

        binding.swipeToRefresh.setOnRefreshListener {
            viewModel.getUsers()
        }

        viewModel.users.observe(viewLifecycleOwner) {
            adapter?.updateData(it, UpdateEvent(ListUpdateEvent.Refresh, arrayOf()))
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            binding.swipeToRefresh.isRefreshing = it
        }
    }
    companion object {
        const val ARG_POST_ID = "postId"
        fun getInstance(postId: Long): PostLikesFragment {
            return PostLikesFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_POST_ID, postId)
                }
            }
        }
    }
}
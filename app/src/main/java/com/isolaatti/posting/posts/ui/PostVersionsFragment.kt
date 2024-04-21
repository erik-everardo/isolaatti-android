package com.isolaatti.posting.posts.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.isolaatti.databinding.FragmentPostVersionsBinding
import com.isolaatti.posting.posts.presentation.PostVersionsAdapter
import com.isolaatti.posting.posts.presentation.PostVersionsViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import javax.inject.Inject

@AndroidEntryPoint
class PostVersionsFragment : Fragment() {
    private lateinit var binding: FragmentPostVersionsBinding
    private val viewModel: PostVersionsViewModel by viewModels()
    private var adapter: PostVersionsAdapter? = null

    @Inject lateinit var markwon: Markwon

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostVersionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.postId = arguments?.getLong(ARG_POST_ID) ?: 0

        if(viewModel.postId != 0L) {
            viewModel.getPostVersions()
        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = PostVersionsAdapter(markwon)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.swipeToRefresh.setOnRefreshListener {
            viewModel.getPostVersions()
        }

        viewModel.versions.observe(viewLifecycleOwner) {
            adapter?.submitList(it)
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            binding.swipeToRefresh.isRefreshing = it
        }
    }

    companion object {
        const val LOG_TAG = "PostVersionsFragment"
        const val ARG_POST_ID = "postId"
        fun getInstance(postId: Long): PostVersionsFragment {
            return PostVersionsFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_POST_ID, postId)
                }
            }
        }
    }
}
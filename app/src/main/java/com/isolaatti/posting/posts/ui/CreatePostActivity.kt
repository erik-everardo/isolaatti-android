package com.isolaatti.posting.posts.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayoutMediator
import com.isolaatti.R
import com.isolaatti.common.IsolaattiBaseActivity
import com.isolaatti.databinding.ActivityCreatePostBinding
import com.isolaatti.posting.posts.presentation.CreatePostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreatePostActivity : IsolaattiBaseActivity() {
    companion object {
        const val EXTRA_MODE_CREATE = 0
        /**
         * Post activity in edit mode
         */
        const val EXTRA_MODE_EDIT = 1
        const val EXTRA_KEY_MODE = "mode"

        /**
         * post id, pass long
         */
        const val EXTRA_KEY_POST_ID = "postId"

        const val EXTRA_KEY_POST_POSTED = "post"
    }

    lateinit var binding: ActivityCreatePostBinding
    val viewModel: CreatePostViewModel by viewModels()
    var mode: Int = EXTRA_MODE_CREATE
    var postId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        intent.extras?.let {
            mode = it.getInt(EXTRA_KEY_MODE, EXTRA_MODE_CREATE)
            postId = it.getLong(EXTRA_KEY_POST_ID)
        }


        binding = ActivityCreatePostBinding.inflate(layoutInflater)

        if(mode == EXTRA_MODE_EDIT && postId != 0L) {
            viewModel.loadDiscussion(postId)
        }



        setupUI()
        setListeners()
        setObservers()
        setContentView(binding.root)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                errorViewModel.retry.collect {
                    if(!it) {
                        return@collect
                    }

                    if(mode == EXTRA_MODE_EDIT && postId != 0L) {
                        viewModel.editDiscussion(postId)
                    } else {
                        viewModel.postDiscussion()
                    }
                }
            }
        }

    }

    private fun setupUI() {
        binding.toolbar.setTitle(if(mode == EXTRA_MODE_EDIT && postId != 0L) R.string.edit else R.string.new_post)

        binding.pager.adapter = CreatePostFragmentStateAdapter(this)

    }

    private fun setListeners() {
        binding.toolbar.setNavigationOnClickListener {
            exit()
        }


        binding.postButton.setOnClickListener {
            if(mode == EXTRA_MODE_EDIT && postId != 0L) {
                viewModel.editDiscussion(postId)
            } else {
                viewModel.postDiscussion()
            }
        }

    }

    private fun setObservers() {
        viewModel.validation.observe(this@CreatePostActivity) {
            binding.postButton.isEnabled = it
        }

        viewModel.error.observe(this@CreatePostActivity) {
            errorViewModel.error.postValue(it)
        }

        viewModel.posted.observe(this@CreatePostActivity) {
            setResult(Activity.RESULT_OK, Intent().apply{
                putExtra(EXTRA_KEY_POST_POSTED, it)
            })
            finish()
        }

        viewModel.sendingPost.observe(this@CreatePostActivity) {
            binding.progressBarLoading.visibility = if(it) View.VISIBLE else View.GONE
        }
    }

    private fun exit() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}
package com.isolaatti.posting.posts.viewer.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.app.TaskStackBuilder
import androidx.core.content.res.ResourcesCompat
import coil.imageLoader
import coil.load
import com.isolaatti.BuildConfig
import com.isolaatti.R
import com.isolaatti.common.IsolaattiBaseActivity
import com.isolaatti.databinding.ActivityPostViewerBinding
import com.isolaatti.posting.comments.ui.BottomSheetPostComments
import com.isolaatti.posting.posts.ui.PostInfoActivity
import com.isolaatti.posting.posts.viewer.presentation.PostViewerViewModel
import com.isolaatti.profile.ui.ProfileActivity
import com.isolaatti.utils.UrlGen
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.image.destination.ImageDestinationProcessorRelativeToAbsolute
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostViewerActivity : IsolaattiBaseActivity() {
    companion object {
        const val POST_ID = "postId"
        const val LOG_TAG = "PostViewerActivity"

        fun getIntent(context: Context, postId: Long): Intent {
            return Intent(context, PostViewerActivity::class.java).apply {
                putExtra(POST_ID, postId)
            }
        }
        fun startActivity(context: Context, postId: Long) {
            context.startActivity(getIntent(context, postId))
        }

        fun getPendingIntent(context: Context, postId: Long): PendingIntent? {
            return TaskStackBuilder.create(context).run {
                addNextIntentWithParentStack(Companion.getIntent(context, postId))

                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }
        }
    }


    private lateinit var binding: ActivityPostViewerBinding
    private val viewModel: PostViewerViewModel by viewModels()
    private var postId: Long = 0
    private lateinit var markwon: Markwon

    private fun openComments() {
        val modalBottomSheet = BottomSheetPostComments.getInstance(postId)
        modalBottomSheet.show(supportFragmentManager, BottomSheetPostComments.TAG)
    }

    private fun setObservers() {
        viewModel.error.observe(this) {
            errorViewModel.error.postValue(it)
            CoroutineScope(Dispatchers.Default).launch {
                errorViewModel.retry.collect {
                    viewModel.retry()
                    errorViewModel.handleRetry()
                }
            }
        }

        viewModel.post.observe(this) {
            binding.markwonContainer.text = it.textContent
            binding.author.text = it.userName
            binding.profileImageView.load(UrlGen.userProfileImage(it.userId), imageLoader)
            binding.commentsInfo.text = getString(R.string.comments_info, it.numberOfComments)
            binding.likesInfo.text = getString(R.string.likes_info, it.numberOfLikes)
            binding.author.setOnClickListener {_ ->
                ProfileActivity.startActivity(this@PostViewerActivity, it.userId)
            }


        }

        viewModel.postLiked.observe(this) {
            val color = if(it) R.color.purple_lighter else R.color.on_surface
            val menuItem = binding.toolbar.menu.findItem(R.id.like)

            menuItem.isEnabled = true
            menuItem.icon?.setTint(ResourcesCompat.getColor(resources, color, null))


        }

    }

    private fun setListeners() {
        binding.toolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.like -> {
                    viewModel.likeDislikePost()
                    true
                }
                R.id.comments -> {
                    openComments()
                    true
                }
                else -> false
            }
        }

        binding.commentsInfo.setOnClickListener {
            openComments()
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.likesInfo.setOnClickListener {
            PostInfoActivity.startActivity(this, postId)
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        intent?.getLongExtra(POST_ID, 0)?.let {
            postId = it
            viewModel.postId = postId
            viewModel.getPost()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postId = intent.getLongExtra(POST_ID, 0)
        Log.d(LOG_TAG, "Post id: $postId")

        binding = ActivityPostViewerBinding.inflate(layoutInflater)
        markwon = Markwon.builder(this)
            .usePlugin(object: AbstractMarkwonPlugin() {
                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                    builder
                        .imageDestinationProcessor(
                            ImageDestinationProcessorRelativeToAbsolute
                            .create(BuildConfig.backend))
                }
            })
            .usePlugin(CoilImagesPlugin.create(this, imageLoader))
            .usePlugin(LinkifyPlugin.create())
            .build()

        setContentView(binding.root)
        if(postId!! > 0) {
            viewModel.postId = postId
            setObservers()
            setListeners()
            viewModel.getPost()
        }

    }
}
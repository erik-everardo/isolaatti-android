package com.isolaatti.posting.posts.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.isolaatti.BuildConfig
import com.isolaatti.R
import com.isolaatti.audio.common.components.AudioRecorderState
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.common.Dialogs
import com.isolaatti.common.ErrorMessageViewModel
import com.isolaatti.common.IsolaattiTheme
import com.isolaatti.common.OnUserInteractedWithPostCallback
import com.isolaatti.common.Ownable
import com.isolaatti.common.options_bottom_sheet.domain.OptionClicked
import com.isolaatti.common.options_bottom_sheet.domain.Options
import com.isolaatti.common.options_bottom_sheet.presentation.BottomSheetPostOptionsViewModel
import com.isolaatti.common.options_bottom_sheet.ui.BottomSheetPostOptionsFragment
import com.isolaatti.hashtags.ui.HashtagsPostsActivity
import com.isolaatti.home.ui.FeedFragment
import com.isolaatti.images.picture_viewer.ui.PictureViewerActivity
import com.isolaatti.posting.comments.ui.BottomSheetPostComments
import com.isolaatti.posting.posts.components.PostComponent
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.posting.posts.presentation.EditPostContract
import com.isolaatti.posting.posts.presentation.PostListingViewModel
import com.isolaatti.posting.posts.viewer.ui.PostViewerActivity
import com.isolaatti.profile.ui.ProfileActivity
import com.isolaatti.reports.data.ContentType
import com.isolaatti.reports.ui.NewReportBottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostListingFragment : Fragment() {
    companion object {
        const val ARG_HASHTAG = "hashtag"
        const val LOG_TAG = "PostListingFragment"
    }

    var hashtag: String? = null

    private val errorViewModel: ErrorMessageViewModel by activityViewModels()
    private val viewModel: PostListingViewModel by viewModels()
    val optionsViewModel: BottomSheetPostOptionsViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(LOG_TAG, "onCreate ; hashtag: $hashtag")
        hashtag = arguments?.getString(ARG_HASHTAG)
        viewModel.getFeed(false, hashtag)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                var isRefreshing by remember { mutableStateOf(false) }
                val posts by viewModel.posts.collectAsState(emptyList())
                IsolaattiTheme {
                    PullToRefreshBox(isRefreshing, onRefresh = {viewModel.getFeed(true, null)}) {
                        LazyColumn(flingBehavior = ScrollableDefaults.flingBehavior()) {
                            items(count = posts.size, key = {posts[it].id}) {
                                val post = posts[it]
                                PostComponent(
                                    modifier = Modifier.animateItem(),
                                    post = post,
                                    onClick = {
                                        PostViewerActivity.startActivity(requireContext(), post.id)
                                    },
                                    onComments = {
                                        val modalBottomSheet = BottomSheetPostComments.getInstance(post.id)
                                        modalBottomSheet.show(requireActivity().supportFragmentManager, BottomSheetPostComments.TAG)
                                    },
                                    onOptions = {
                                        optionsViewModel.setOptions(Options.POST_OPTIONS, FeedFragment.CALLER_ID, post)
                                        val modalBottomSheet = BottomSheetPostOptionsFragment()
                                        modalBottomSheet.show(requireActivity().supportFragmentManager, BottomSheetPostOptionsFragment.TAG)
                                    },
                                    onInfo = {
                                        PostInfoActivity.startActivity(requireContext(), post.id)
                                    },
                                    onLike = {
                                        viewModel.likePost(post.id)
                                    },
                                    onDislike = {
                                        viewModel.unLikePost(post.id)
                                    },
                                    onShare = {
                                        val intent = Intent.createChooser(Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, "${BuildConfig.backend}/pub/${post.id}")
                                            type = "text/plain"
                                        }, getString(R.string.share_post))
                                        startActivity(intent)
                                    },
                                    onImageClick = { images, index ->
                                        PictureViewerActivity.startActivityWithImages(requireContext(), images.toTypedArray(), index)
                                    },
                                    onUsernameClick = {
                                        ProfileActivity.startActivity(requireContext(), post.userId)
                                    },
                                    onHashtagClick = { hashtag ->
                                        HashtagsPostsActivity.startActivity(requireContext(), hashtag)
                                    },
                                    onPlay = {},
                                    onPause = {},
                                    onSeek = {}
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.errorLoading.observe(viewLifecycleOwner) {
            errorViewModel.error.postValue(it)
        }

        optionsViewModel.optionClicked.observe(viewLifecycleOwner, optionsObserver)
    }

    /*
    override fun onLiked(postId: Long) = viewModel.likePost(postId)

    override fun onUnLiked(postId: Long) = viewModel.unLikePost(postId)

    override fun onOptions(post: Ownable) {

    }

    override fun onComment(postId: Long) {

    }

    override fun onOpenPost(postId: Long) {
        PostViewerActivity.startActivity(requireContext(), postId)
    }

    override fun onPlay(audio: Audio) {
    }

    override fun onMoreInfo(postId: Long) {

    }

    override fun onShare(postId: Long) {
        val intent = Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "${BuildConfig.backend}/pub/${postId}")
            type = "text/plain"
        }, getString(R.string.share_post))
        startActivity(intent)
    }

    override fun hashtagClicked(hashtag: String) {
        HashtagsPostsActivity.startActivity(requireContext(), hashtag)
    }

    override fun onProfileClick(userId: Int) {
        ProfileActivity.startActivity(requireContext(), userId)
    }

    override fun onLoadMore() {
        viewModel.getFeed(false, hashtag)
    }

     */

    private val editDiscussion = registerForActivityResult(EditPostContract()) {
        if(it != null) {
            viewModel.onPostUpdate(it)
        }
    }

    private val optionsObserver: Observer<OptionClicked?> = Observer { optionClicked ->
        if(optionClicked?.callerId == FeedFragment.CALLER_ID) {
            // post id should come as payload
            val post = optionClicked.payload as? Post ?: return@Observer
            when(optionClicked.optionId) {
                Options.Option.OPTION_DELETE -> {
                    Dialogs.buildDeletePostDialog(requireContext()) { delete ->
                        optionsViewModel.handle()
                        if(delete) {
                            viewModel.deletePost(post.id)
                        }
                    }.show()

                }
                Options.Option.OPTION_EDIT -> {
                    optionsViewModel.handle()
                    editDiscussion.launch(post.id)
                }
                Options.Option.OPTION_REPORT -> {
                    optionsViewModel.handle()
                    NewReportBottomSheetDialogFragment.newInstance(ContentType.Post, post.id.toString())
                        .show(childFragmentManager, NewReportBottomSheetDialogFragment.LOG_TAG)
                }
            }
        }
    }


}
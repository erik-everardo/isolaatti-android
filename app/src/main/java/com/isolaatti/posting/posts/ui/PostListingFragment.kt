package com.isolaatti.posting.posts.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.isolaatti.BuildConfig
import com.isolaatti.R
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.audio.common.domain.Playable
import com.isolaatti.audio.player.AudioPlayerConnector
import com.isolaatti.common.CoilImageLoader
import com.isolaatti.common.Dialogs
import com.isolaatti.common.ErrorMessageViewModel
import com.isolaatti.common.OnUserInteractedWithPostCallback
import com.isolaatti.common.Ownable
import com.isolaatti.common.options_bottom_sheet.domain.OptionClicked
import com.isolaatti.common.options_bottom_sheet.domain.Options
import com.isolaatti.common.options_bottom_sheet.presentation.BottomSheetPostOptionsViewModel
import com.isolaatti.common.options_bottom_sheet.ui.BottomSheetPostOptionsFragment
import com.isolaatti.databinding.FragmentPostListingBinding
import com.isolaatti.hashtags.ui.HashtagsPostsActivity
import com.isolaatti.home.ui.FeedFragment
import com.isolaatti.posting.comments.ui.BottomSheetPostComments
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.posting.posts.presentation.EditPostContract
import com.isolaatti.posting.posts.presentation.PostListingViewModel
import com.isolaatti.posting.posts.presentation.PostsRecyclerViewAdapter
import com.isolaatti.posting.posts.viewer.ui.PostViewerActivity
import com.isolaatti.profile.ui.ProfileActivity
import com.isolaatti.reports.data.ContentType
import com.isolaatti.reports.ui.NewReportBottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.image.destination.ImageDestinationProcessorRelativeToAbsolute
import io.noties.markwon.linkify.LinkifyPlugin

@AndroidEntryPoint
class PostListingFragment : Fragment(), OnUserInteractedWithPostCallback {
    companion object {
        const val ARG_HASHTAG = "hashtag"
        const val LOG_TAG = "PostListingFragment"
    }

    var hashtag: String? = null

    private lateinit var viewBinding: FragmentPostListingBinding
    private val errorViewModel: ErrorMessageViewModel by activityViewModels()
    private val viewModel: PostListingViewModel by viewModels()
    val optionsViewModel: BottomSheetPostOptionsViewModel by activityViewModels()
    private lateinit var adapter: PostsRecyclerViewAdapter
    private lateinit var audioPlayerConnector: AudioPlayerConnector

    private val audioPlayerConnectorListener = object: AudioPlayerConnector.Listener {
        override fun onPlaying(isPlaying: Boolean, audio: Playable) {
            if(audio is Audio)
                adapter.setIsPlaying(isPlaying, audio)
        }

        override fun isLoading(isLoading: Boolean, audio: Playable) {
            if(audio is Audio)
                adapter.setIsLoading(isLoading, audio)
        }

        override fun progressChanged(second: Int, audio: Playable) {
            if(audio is Audio)
                adapter.setProgress(second, audio)
        }

        override fun durationChanged(duration: Int, audio: Playable) {
            if(audio is Audio)
                adapter.setDuration(duration, audio)
        }

        override fun onEnded(audio: Playable) {
            if(audio is Audio)
                adapter.setEnded(audio)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(LOG_TAG, "onCreate ; hashtag: $hashtag")
        hashtag = arguments?.getString(ARG_HASHTAG)
        viewModel.getFeed(false, hashtag)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentPostListingBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioPlayerConnector = AudioPlayerConnector(requireContext())
        audioPlayerConnector.addListener(audioPlayerConnectorListener)
        viewLifecycleOwner.lifecycle.addObserver(audioPlayerConnector)

        val markwon = Markwon.builder(requireContext())
            .usePlugin(object: AbstractMarkwonPlugin() {
                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                    builder
                        .imageDestinationProcessor(
                            ImageDestinationProcessorRelativeToAbsolute
                            .create(BuildConfig.backend))
                }
            })
            .usePlugin(CoilImagesPlugin.create(requireContext(), CoilImageLoader.imageLoader))
            .usePlugin(LinkifyPlugin.create())
            .build()
        adapter = PostsRecyclerViewAdapter(this)
        viewBinding.feedRecyclerView.adapter = adapter
        viewBinding.feedRecyclerView.setItemViewCacheSize(7)
        viewBinding.feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())


        viewBinding.swipeToRefresh.setOnRefreshListener {
            viewModel.getFeed(refresh = true, hashtag)
        }

        viewModel.posts.observe(viewLifecycleOwner){
            if (it?.first != null) {
                adapter.updateList(it.first!!, it.second)
                adapter.newContentRequestFinished()
            }
        }

        viewModel.loadingPosts.observe(viewLifecycleOwner) {
            viewBinding.loadingIndicator.visibility = if(it) View.VISIBLE else View.GONE
            if(!it) {
                viewBinding.swipeToRefresh.isRefreshing = false
            }
        }


        viewModel.errorLoading.observe(viewLifecycleOwner) {
            errorViewModel.error.postValue(it)
        }

        optionsViewModel.optionClicked.observe(viewLifecycleOwner, optionsObserver)
    }

    override fun onLiked(postId: Long) = viewModel.likePost(postId)

    override fun onUnLiked(postId: Long) = viewModel.unLikePost(postId)

    override fun onOptions(post: Ownable) {
        optionsViewModel.setOptions(Options.POST_OPTIONS, FeedFragment.CALLER_ID, post)
        val modalBottomSheet = BottomSheetPostOptionsFragment()
        modalBottomSheet.show(requireActivity().supportFragmentManager, BottomSheetPostOptionsFragment.TAG)
    }

    override fun onComment(postId: Long) {
        val modalBottomSheet = BottomSheetPostComments.getInstance(postId)
        modalBottomSheet.show(requireActivity().supportFragmentManager, BottomSheetPostComments.TAG)
    }

    override fun onOpenPost(postId: Long) {
        PostViewerActivity.startActivity(requireContext(), postId)
    }

    override fun onPlay(audio: Audio) {
        audioPlayerConnector.playPauseAudio(audio)
    }

    override fun onMoreInfo(postId: Long) {
        PostInfoActivity.startActivity(requireContext(), postId)
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
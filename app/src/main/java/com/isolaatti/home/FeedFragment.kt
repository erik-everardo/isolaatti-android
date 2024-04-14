package com.isolaatti.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.isolaatti.BuildConfig
import com.isolaatti.R
import com.isolaatti.about.AboutActivity
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.audio.common.domain.Playable
import com.isolaatti.audio.player.AudioPlayerConnector
import com.isolaatti.common.CoilImageLoader.imageLoader
import com.isolaatti.common.Dialogs
import com.isolaatti.common.ErrorMessageViewModel
import com.isolaatti.databinding.FragmentFeedBinding
import com.isolaatti.drafts.ui.DraftsActivity
import com.isolaatti.home.presentation.FeedViewModel
import com.isolaatti.images.picture_viewer.ui.PictureViewerActivity
import com.isolaatti.posting.posts.viewer.ui.PostViewerActivity
import com.isolaatti.posting.comments.ui.BottomSheetPostComments
import com.isolaatti.common.OnUserInteractedWithPostCallback
import com.isolaatti.common.Ownable
import com.isolaatti.common.options_bottom_sheet.domain.OptionClicked
import com.isolaatti.common.options_bottom_sheet.domain.Options
import com.isolaatti.common.options_bottom_sheet.presentation.BottomSheetPostOptionsViewModel
import com.isolaatti.common.options_bottom_sheet.ui.BottomSheetPostOptionsFragment
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.posting.posts.presentation.CreatePostContract
import com.isolaatti.posting.posts.presentation.EditPostContract
import com.isolaatti.posting.posts.presentation.PostsRecyclerViewAdapter
import com.isolaatti.profile.ui.ProfileActivity
import com.isolaatti.reports.ui.NewReportBottomSheetDialogFragment
import com.isolaatti.settings.ui.SettingsActivity
import com.isolaatti.utils.UrlGen
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.image.destination.ImageDestinationProcessorRelativeToAbsolute
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.coroutines.launch
import org.w3c.dom.Text

@AndroidEntryPoint
class FeedFragment : Fragment(), OnUserInteractedWithPostCallback {

    companion object {
        fun newInstance() = FeedFragment()
        const val CALLER_ID = 20
    }

    private val errorViewModel: ErrorMessageViewModel by activityViewModels()
    private val viewModel: FeedViewModel by activityViewModels()
    val optionsViewModel: BottomSheetPostOptionsViewModel by activityViewModels()
    private var currentUserId = 0

    private lateinit var viewBinding: FragmentFeedBinding
    private lateinit var adapter: PostsRecyclerViewAdapter

    private lateinit var audioPlayerConnector: AudioPlayerConnector

    // region launchers

    private val createDiscussion = registerForActivityResult(CreatePostContract()) {
        if(it != null) {
            Toast.makeText(requireContext(), R.string.posted_successfully, Toast.LENGTH_SHORT).show()
        }
    }

    private val editDiscussion = registerForActivityResult(EditPostContract()) {
        if(it != null) {
            viewModel.onPostUpdate(it)
        }
    }

    // endregion

    // region observers

    private val optionsObserver: Observer<OptionClicked?> = Observer { optionClicked ->
        if(optionClicked?.callerId == CALLER_ID) {
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
                    NewReportBottomSheetDialogFragment.newInstance().show(childFragmentManager, NewReportBottomSheetDialogFragment.LOG_TAG)
                }
            }
        }
    }

    // endregion

    // region lifecycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentFeedBinding.inflate(inflater)

        return viewBinding.root
    }

    // endregion

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

    // region events
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.topAppBar.setNavigationOnClickListener {
            viewBinding.drawerLayout?.openDrawer(viewBinding.homeDrawer)
        }


        viewBinding.homeDrawer.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.settings_menu_item -> {
                    startActivity(Intent(requireActivity(), SettingsActivity::class.java))
                    true
                }
                R.id.about_menu_item -> {
                    startActivity(Intent(requireActivity(), AboutActivity::class.java))
                    true
                }
                else -> {true}
            }
        }

        audioPlayerConnector = AudioPlayerConnector(requireContext())
        audioPlayerConnector.addListener(audioPlayerConnectorListener)
        viewLifecycleOwner.lifecycle.addObserver(audioPlayerConnector)

        val markwon = Markwon.builder(requireContext())
            .usePlugin(object: AbstractMarkwonPlugin() {
                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                    builder
                        .imageDestinationProcessor(ImageDestinationProcessorRelativeToAbsolute
                            .create(BuildConfig.backend))
                }
            })
            .usePlugin(CoilImagesPlugin.create(requireContext(), imageLoader))
            .usePlugin(LinkifyPlugin.create())
            .build()
        adapter = PostsRecyclerViewAdapter(markwon, this)
        viewBinding.feedRecyclerView.adapter = adapter
        viewBinding.feedRecyclerView.setItemViewCacheSize(7)
        viewBinding.feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())


        viewBinding.swipeToRefresh.setOnRefreshListener {
            viewModel.getFeed(refresh = true)
        }


        viewBinding.topAppBar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.menu_item_new_discussion -> {
                    createDiscussion.launch(Unit)
                    true
                }
                else -> {
                    false
                }
            }
        }

        viewModel.userProfile.observe(viewLifecycleOwner) {
            val header = viewBinding.homeDrawer.getHeaderView(0) as? ConstraintLayout

            val image: ImageView? = header?.findViewById(R.id.profileImageView)
            val textViewName: TextView? = header?.findViewById(R.id.textViewName)
            val textViewEmail: TextView? = header?.findViewById(R.id.textViewEmail)
            val textViewUsername: TextView? = header?.findViewById(R.id.textViewUsername)

            image?.load(UrlGen.userProfileImage(it.userId), imageLoader)

            val card: MaterialCardView? = header?.findViewById(R.id.drawer_header_card)
            card?.setOnClickListener {
                ProfileActivity.startActivity(requireContext(), currentUserId)
            }

            textViewName?.text = it.name
            textViewEmail?.text = it.email
            textViewUsername?.text = "@${it.uniqueUsername}"
            currentUserId = it.userId
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

        viewModel.getProfile()

        optionsViewModel.optionClicked.observe(viewLifecycleOwner, optionsObserver)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                errorViewModel.retry.collect {
                    viewModel.retry()
                    errorViewModel.handleRetry()
                }
            }
        }
    }

    override fun onLiked(postId: Long) = viewModel.likePost(postId)

    override fun onUnLiked(postId: Long) = viewModel.unLikePost(postId)

    override fun onOptions(post: Ownable) {
        optionsViewModel.setOptions(Options.POST_OPTIONS, CALLER_ID, post)
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

    override fun onProfileClick(userId: Int) {
        ProfileActivity.startActivity(requireContext(), userId)
    }

    override fun onLoadMore() {
        viewModel.getFeed(false)
    }

    // endregion
}
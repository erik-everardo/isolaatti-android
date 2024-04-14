package com.isolaatti.profile.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.isolaatti.BuildConfig
import com.isolaatti.R
import com.isolaatti.audio.audios_list.ui.AudiosFragment
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.audio.common.domain.Playable
import com.isolaatti.audio.player.AudioPlayerConnector
import com.isolaatti.common.CoilImageLoader.imageLoader
import com.isolaatti.common.Dialogs
import com.isolaatti.common.ErrorMessageViewModel
import com.isolaatti.common.Ownable
import com.isolaatti.common.options_bottom_sheet.domain.OptionClicked
import com.isolaatti.common.options_bottom_sheet.domain.Options
import com.isolaatti.common.options_bottom_sheet.presentation.BottomSheetPostOptionsViewModel
import com.isolaatti.common.options_bottom_sheet.ui.BottomSheetPostOptionsFragment
import com.isolaatti.databinding.FragmentDiscussionsBinding
import com.isolaatti.followers.domain.FollowingState
import com.isolaatti.images.image_chooser.ui.ImageChooserContract
import com.isolaatti.images.image_list.ui.ImagesFragment
import com.isolaatti.images.picture_viewer.ui.PictureViewerActivity
import com.isolaatti.posting.comments.ui.BottomSheetPostComments
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.posting.posts.presentation.CreatePostContract
import com.isolaatti.posting.posts.presentation.EditPostContract
import com.isolaatti.posting.posts.presentation.PostListingRecyclerViewAdapterWiring
import com.isolaatti.posting.posts.presentation.PostsRecyclerViewAdapter
import com.isolaatti.posting.posts.presentation.UpdateEvent
import com.isolaatti.posting.posts.viewer.ui.PostViewerActivity
import com.isolaatti.profile.domain.entity.UserProfile
import com.isolaatti.profile.presentation.EditProfileContract
import com.isolaatti.profile.presentation.ProfileViewModel
import com.isolaatti.reports.ui.NewReportBottomSheetDialogFragment
import com.isolaatti.utils.UrlGen
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.image.destination.ImageDestinationProcessorRelativeToAbsolute
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileMainFragment : Fragment() {
    lateinit var viewBinding: FragmentDiscussionsBinding
    private val viewModel: ProfileViewModel by viewModels()
    val optionsViewModel: BottomSheetPostOptionsViewModel by activityViewModels()
    val errorViewModel: ErrorMessageViewModel by activityViewModels()
    private var userId: Int? = null

    lateinit var postsAdapter: PostsRecyclerViewAdapter

    private var audioDescriptionAudio: Audio? = null

    private lateinit var audioPlayerConnector: AudioPlayerConnector

    // collapsing bar
    private var title = ""
    private var scrollRange = -1
    private var isShow = false

    private val createDiscussion = registerForActivityResult(CreatePostContract()) {
        if(it == null)
            return@registerForActivityResult

        Toast.makeText(requireContext(), R.string.posted_successfully, Toast.LENGTH_SHORT).show()

        viewModel.onPostAddedAtTheBeginning(it)
    }

    private val chooseImageLauncher = registerForActivityResult(ImageChooserContract()) { image ->
        // here change profile picture
        if(image != null) {
            viewModel.setProfileImage(image)
        }
    }

    private val editDiscussion = registerForActivityResult(EditPostContract()) {
        if(it != null) {
            viewModel.onPostUpdate(it)
        }
    }

    private val editProfile = registerForActivityResult(EditProfileContract()) { updatedProfile ->
        if(updatedProfile != null) {
            viewModel.setProfile(updatedProfile)
        }
    }


    private val audioPlayerConnectorListener = object: AudioPlayerConnector.Listener {
        override fun onPlaying(isPlaying: Boolean, audio: Playable) {
            Log.d(LOG_TAG, "onPlaying() isPlaying: $isPlaying: audio $audio")
            if(audio == audioDescriptionAudio) {
                viewBinding.playButton.icon =
                    AppCompatResources.getDrawable(
                        requireContext(),
                        if(isPlaying) R.drawable.baseline_pause_circle_24 else R.drawable.baseline_play_circle_24
                    )
            }
            if(audio is Audio)
                postsAdapter.setIsPlaying(isPlaying, audio)
        }

        override fun isLoading(isLoading: Boolean, audio: Playable) {
            if(audio == audioDescriptionAudio) {
                viewBinding.playButton.isEnabled = !isLoading
                viewBinding.audioProgress.isIndeterminate = isLoading
            }

            if(audio is Audio)
                postsAdapter.setIsLoading(isLoading, audio)

        }

        override fun progressChanged(second: Int, audio: Playable) {
            if(audio == audioDescriptionAudio) {
                viewBinding.audioProgress.setProgress(second, true)
            }

            if(audio is Audio)
                postsAdapter.setProgress(second, audio)
        }

        override fun durationChanged(duration: Int, audio: Playable) {
            if(audio == audioDescriptionAudio) {
                viewBinding.audioProgress.max = duration
            }

            if(audio is Audio)
                postsAdapter.setDuration(duration, audio)
        }

        override fun onEnded(audio: Playable) {
            if(audio == audioDescriptionAudio) {
                viewBinding.audioProgress.progress = 0
            }

            if(audio is Audio)
                postsAdapter.setEnded(audio)
        }

    }

    private val profileObserver = Observer<UserProfile> { profile ->
        viewBinding.profileImageView.load(UrlGen.userProfileImage(profile.userId, invalidateCache = true), imageLoader)

        title = profile.name
        viewBinding.textViewUsername.text = profile.name
        viewBinding.textViewDescription.text = profile.descriptionText
        if(profile.descriptionText.isNullOrBlank()) {
            viewBinding.descriptionCard.visibility = View.GONE
        }

        viewBinding.goToFollowersBtn.text = getString(
            R.string.go_to_followers_btn_text,
            profile.numberOfFollowers.toString(),
            profile.numberOfFollowing.toString()
        )


        viewBinding.profileImageView.setOnClickListener {
            optionsViewModel.setOptions(Options.PROFILE_PHOTO_OPTIONS, CALLER_ID, profile)
            val fragment = BottomSheetPostOptionsFragment()
            fragment.show(parentFragmentManager, BottomSheetPostOptionsFragment.TAG)
        }

        audioDescriptionAudio = profile.descriptionAudio
        viewBinding.playButtonContainer.visibility = if(profile.descriptionAudio != null) View.VISIBLE else View.GONE

        setupUiForUserType(profile.isUserItself)
    }

    private val postsObserver: Observer<Pair<List<Post>?, UpdateEvent>?> = Observer {
        if(it?.first != null) {
            postsAdapter.updateList(it.first!!, it.second)
            postsAdapter.newContentRequestFinished()
        }

    }

    private val followingStateObserver: Observer<FollowingState> = Observer {
        when(it) {
            FollowingState.FollowingThisUser -> {
                viewBinding.textViewFollowingState.setText(R.string.following_user)
                viewBinding.followButton.setText(R.string.unfollow)
                viewBinding.followButton.isChecked = true
            }
            FollowingState.MutuallyFollowing -> {
                viewBinding.textViewFollowingState.setText(R.string.mutually_following)
                viewBinding.followButton.setText(R.string.unfollow)
                viewBinding.followButton.isChecked = true
            }
            FollowingState.ThisUserIsFollowingMe -> {
                viewBinding.textViewFollowingState.setText(R.string.following_you)
                viewBinding.followButton.setText(R.string.follow)
                viewBinding.followButton.isChecked = false
            }
            FollowingState.NotMutuallyFollowing -> {
                viewBinding.textViewFollowingState.text = ""
                viewBinding.followButton.setText(R.string.follow)
                viewBinding.followButton.isChecked = false
            }
        }
    }

    private val optionsObserver: Observer<OptionClicked?> = Observer { optionClicked ->
        if(optionClicked?.callerId == CALLER_ID) {
            Log.d("ProfileMainFragment", optionClicked.toString())

            when(optionClicked.optionsId) {
                Options.PROFILE_PHOTO_OPTIONS -> {
                    val profile = optionClicked.payload as? UserProfile
                    when(optionClicked.optionId) {
                        Options.Option.OPTION_PROFILE_PHOTO_CHANGE_PHOTO -> {
                            chooseImageLauncher.launch(ImageChooserContract.Requester.UserPost)
                        }
                        Options.Option.OPTION_PROFILE_PHOTO_REMOVE_PHOTO -> {
                            showRemoveProfileImageDialog()
                        }
                        Options.Option.OPTION_PROFILE_PHOTO_VIEW_PHOTO -> {
                            val profilePictureUrl = profile?.profilePictureUrl
                            if(profilePictureUrl != null) {
                                // TODO show image
                            }
                        }
                    }
                    optionsViewModel.handle()
                }
                Options.POST_OPTIONS -> {
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


        }
    }

    private fun showRemoveProfileImageDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.remove_image_message_confirmation)
            .setTitle(R.string.remove_photo)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.yes_continue) { _, _ ->
                // remove image here
            }
            .show()
    }

    private lateinit var postListingRecyclerViewAdapterWiring: PostListingRecyclerViewAdapterWiring


    private fun setupCollapsingBar() {

        viewBinding.topAppBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (scrollRange == -1) scrollRange = appBarLayout.totalScrollRange
            if (scrollRange + verticalOffset == 0) {
                viewBinding.collapsingToolbarLayout.title = viewModel.profile.value?.name
                isShow = true
            } else if (isShow) {
                viewBinding.collapsingToolbarLayout.title = " "
            }
        }
    }

    private fun bind() {

        if(userId == null) {
            return
        }


        viewBinding.topAppBar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        viewBinding.topAppBar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.edit_profile -> {
                    viewModel.profile.value?.let { profile -> editProfile.launch(profile) }

                    true
                }
                R.id.user_link_menu_item -> {
                    viewModel.profile.value?.let { profile ->
                        findNavController().navigate(ProfileMainFragmentDirections.actionMainFragmentToQrCodeFragment(profile))
                    }

                    true
                }
                R.id.report_profile_menu_item -> {
                    true
                }
                R.id.block_profile_menu_item -> {
                    true
                }
                else -> false
            }
        }

        viewBinding.audiosButton.setOnClickListener {
            findNavController().navigate(ProfileMainFragmentDirections.actionDiscussionsFragmentToAudiosFragment(AudiosFragment.SOURCE_PROFILE, userId.toString()))
        }

        viewBinding.imagesButton.setOnClickListener {
            findNavController().navigate(
                ProfileMainFragmentDirections.actionDiscussionsFragmentToImagesFragment(ImagesFragment.SOURCE_PROFILE, userId.toString())
            )
        }

        viewBinding.createPostButton.setOnClickListener {
            createDiscussion.launch(Unit)
        }

        viewBinding.goToFollowersBtn.setOnClickListener {
            findNavController().navigate(ProfileMainFragmentDirections.actionDiscussionsFragmentToMainFollowersFragment(userId!!))
        }

        viewBinding.feedRecyclerView.adapter = postsAdapter
        viewBinding.feedRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        viewBinding.swipeToRefresh.setOnRefreshListener {
            viewModel.getFeed(true, null)
        }
        viewBinding.playButton.setOnClickListener {
            audioDescriptionAudio?.let { audio ->
                audioPlayerConnector.playPauseAudio(audio)
            }
        }

        viewBinding.followButton.setOnClickListener {
            it.isEnabled = false
            viewModel.followUser()
        }
    }

    private fun setObservers() {
        viewModel.profile.observe(viewLifecycleOwner, profileObserver)
        viewModel.posts.observe(viewLifecycleOwner, postsObserver)
        viewModel.followingState.observe(viewLifecycleOwner, followingStateObserver)
        optionsViewModel.optionClicked.observe(viewLifecycleOwner, optionsObserver)
        viewModel.loadingPosts.observe(viewLifecycleOwner) {
            viewBinding.loadingIndicator.visibility = if(it) View.VISIBLE else View.GONE
            if(!it) {
                viewBinding.swipeToRefresh.isRefreshing = false
            }
        }
        viewModel.errorLoading.observe(viewLifecycleOwner) {
            errorViewModel.error.postValue(it)
        }
        viewModel.followingLoading.observe(viewLifecycleOwner) {
            viewBinding.followButton.isEnabled = !it
        }
    }

    private fun getData() {

        userId?.let { profileId ->
            viewModel.profileId = profileId
            viewModel.getProfile()
            viewModel.getFeed(true, null)
        }
    }

    private fun setupPostsAdapter() {
        val markwon = Markwon.builder(requireContext())
            .usePlugin(object: AbstractMarkwonPlugin() {
                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                    builder
                        .imageDestinationProcessor(
                            ImageDestinationProcessorRelativeToAbsolute
                            .create(BuildConfig.backend))
                }
            })
            .usePlugin(CoilImagesPlugin.create(requireContext(), imageLoader))
            .usePlugin(LinkifyPlugin.create())
            .build()

        postsAdapter = PostsRecyclerViewAdapter(markwon,postListingRecyclerViewAdapterWiring )

    }

    private fun setupUiForUserType(isOwnProfile: Boolean) {
        if(isOwnProfile) {
            viewBinding.followButton.visibility = View.GONE
            viewBinding.topAppBar.menu?.run {
                removeItem(R.id.block_profile_menu_item)
                removeItem(R.id.report_profile_menu_item)
            }
        } else {
            viewBinding.createPostButton.visibility = View.GONE
            viewBinding.topAppBar.menu.removeItem(R.id.edit_profile)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userId = (requireActivity()).intent.extras?.getInt(ProfileActivity.EXTRA_USER_ID)
        getData()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        postListingRecyclerViewAdapterWiring = object: PostListingRecyclerViewAdapterWiring(viewModel) {
            override fun onComment(postId: Long) {
                val modalBottomSheet = BottomSheetPostComments.getInstance(postId)
                modalBottomSheet.show(requireActivity().supportFragmentManager, BottomSheetPostComments.TAG)
            }

            override fun onOpenPost(postId: Long) {
                PostViewerActivity.startActivity(requireContext(), postId)
            }

            override fun onOptions(post: Ownable) {
                optionsViewModel.setOptions(Options.POST_OPTIONS, CALLER_ID, post)
                val modalBottomSheet = BottomSheetPostOptionsFragment()
                modalBottomSheet.show(requireActivity().supportFragmentManager, BottomSheetPostOptionsFragment.TAG)
            }

            override fun onProfileClick(userId: Int) {
                //ProfileActivity.startActivity(requireContext(), userId)
            }

            override fun onPlay(audio: Audio) {
                audioPlayerConnector.playPauseAudio(audio)
            }

            override fun onLoadMore() {
                viewModel.getFeed(false, null)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentDiscussionsBinding.inflate(inflater)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPostsAdapter()
        bind()
        setObservers()
        setupCollapsingBar()

        audioPlayerConnector = AudioPlayerConnector(requireContext())

        audioPlayerConnector.addListener(audioPlayerConnectorListener)

        viewLifecycleOwner.lifecycle.addObserver(audioPlayerConnector)


        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                errorViewModel.retry.collect {
                    viewModel.retry()
                    errorViewModel.handleRetry()
                }
            }
        }
    }

    companion object {
        const val CALLER_ID = 30
        const val LOG_TAG = "ProfileMainFragment"
    }
}
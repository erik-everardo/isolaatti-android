package com.isolaatti.home.ui

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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import coil3.load
import com.google.android.material.card.MaterialCardView
import com.isolaatti.R
import com.isolaatti.about.AboutActivity
import com.isolaatti.common.CoilImageLoader.imageLoader
import com.isolaatti.common.ErrorMessageViewModel
import com.isolaatti.databinding.FragmentFeedBinding
import com.isolaatti.home.presentation.FeedViewModel
import com.isolaatti.posting.post_creator.presentation.CreatePostContract
import com.isolaatti.posting.posts.ui.PostListingFragment
import com.isolaatti.profile.ui.ProfileActivity
import com.isolaatti.settings.ui.SettingsActivity
import com.isolaatti.squads.ui.SquadsActivity
import com.isolaatti.utils.UrlGen
import com.isolaatti.video_streaming.ui.VideoStreamingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FeedFragment : Fragment() {

    companion object {
        fun newInstance() = FeedFragment()
        const val CALLER_ID = 20
    }

    private val errorViewModel: ErrorMessageViewModel by activityViewModels()
    private val viewModel: FeedViewModel by activityViewModels()

    private var currentUserId = 0

    private lateinit var viewBinding: FragmentFeedBinding

    private val createDiscussion = registerForActivityResult(CreatePostContract()) {
        if(it != null) {
            Toast.makeText(requireContext(), R.string.posted_successfully, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentFeedBinding.inflate(inflater)

        return viewBinding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.topAppBar.setNavigationOnClickListener {
            viewBinding.drawerLayout?.openDrawer(viewBinding.homeDrawer)
        }


        viewBinding.homeDrawer.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.squads_menu_item -> {
                    startActivity(Intent(requireActivity(), SquadsActivity::class.java))
                    true
                }
                R.id.settings_menu_item -> {
                    startActivity(Intent(requireActivity(), SettingsActivity::class.java))
                    true
                }
                R.id.about_menu_item -> {
                    startActivity(Intent(requireActivity(), AboutActivity::class.java))
                    true
                }
                R.id.lives -> {
                    startActivity(Intent(requireActivity(), VideoStreamingActivity::class.java))
                    true
                }
                else -> {true}
            }
        }


        viewBinding.topAppBar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.menu_item_new_discussion -> {
                    createDiscussion.launch(CreatePostContract.Input(null))
                    true
                }
                else -> {
                    false
                }
            }
        }

        // show feed

        (childFragmentManager.findFragmentById(R.id.post_list_fragment_container) as NavHostFragment).navController.setGraph(R.navigation.post_listing_navigation)

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




        viewModel.getProfile()



        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                errorViewModel.retry.collect {
                    viewModel.retry()
                    errorViewModel.handleRetry()
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.root) {_, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            viewBinding.homeDrawer.getHeaderView(0).updatePadding(top = systemBarsInsets.top)
            insets
        }
    }
}
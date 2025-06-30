package com.isolaatti.profile.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.isolaatti.R
import com.isolaatti.databinding.FragmentBrowseProfileBinding
import com.isolaatti.posting.posts.ui.PostListingFragment
import com.isolaatti.profile.profile_listing.ui.ProfileListingFragment

class BrowseProfilesFragment : Fragment() {
    private lateinit var binding: FragmentBrowseProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBrowseProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        (childFragmentManager.findFragmentById(R.id.post_list_fragment_container) as NavHostFragment)
            .navController.setGraph(R.navigation.profile_listing_navigation, Bundle().apply { putInt(
                ProfileListingFragment.ARG_MODE, ProfileListingFragment.BROWSE_PROFILES) })

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val padding = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.toolbar.updatePadding(top = padding.top, left = padding.left, right = padding.right)


            insets
        }

    }
}
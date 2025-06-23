package com.isolaatti.search.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.UncontainedCarouselStrategy
import com.google.android.material.chip.Chip
import com.isolaatti.R
import com.isolaatti.databinding.FragmentSearchBinding
import com.isolaatti.hashtags.ui.HashtagsPostsActivity
import com.isolaatti.posting.viewer.ui.PostViewerActivity
import com.isolaatti.profile.domain.entity.ProfileListItem
import com.isolaatti.profile.ui.ProfileActivity
import com.isolaatti.search.data.HashtagsDto
import com.isolaatti.search.data.SearchDto
import com.isolaatti.search.data.SearchHistoryEntity
import com.isolaatti.search.data.SearchResultType
import com.isolaatti.search.presentation.SearchResultsAdapter
import com.isolaatti.search.presentation.SearchSuggestionsAdapter
import com.isolaatti.search.presentation.SearchViewModel
import com.isolaatti.search.presentation.UserCarouselAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() {

    companion object {
        const val LOG_TAG = "SearchFragment"
    }

    lateinit var viewBinding: FragmentSearchBinding
    private val viewModel: SearchViewModel by viewModels()
    private var searchSuggestionsAdapter: SearchSuggestionsAdapter? = null
    private var newestUsersAdapter: UserCarouselAdapter? = null
    private var searchResultsAdapter: SearchResultsAdapter? = null

    private val searchSuggestionsObserver: Observer<List<SearchHistoryEntity>> = Observer {
        searchSuggestionsAdapter?.submitList(it)
    }

    private val searchResultsObserver: Observer<SearchDto> = Observer {
        Log.d(LOG_TAG, it.toString())
        searchResultsAdapter?.submitList(it.result)
    }

    private val trendingHashtagsObserver: Observer<HashtagsDto> = Observer {
        Log.d(LOG_TAG, it.toString())

        viewBinding.chipGroup.removeAllViews()
        it.result.forEach { hashtag ->
            viewBinding.chipGroup.addView(Chip(requireContext()).apply {
                text = "#$hashtag"
                setOnClickListener {
                    HashtagsPostsActivity.startActivity(requireContext(), hashtag)
                }
            })
        }
    }

    private val newestUsersObserver: Observer<List<ProfileListItem>> = Observer {
        newestUsersAdapter?.submitList(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.loadTendingHashtags()
        viewModel.loadNewestUsers()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentSearchBinding.inflate(inflater)
        return viewBinding.root
    }

    private fun showResults(show: Boolean) {
        if(show) {
            viewBinding.viewAnimator.displayedChild = 1
            viewBinding.searchBar.menu.findItem(R.id.close_button)?.setVisible(true)
        } else {
            viewBinding.viewAnimator.displayedChild = 0
            viewModel.searchQuery = ""
            viewBinding.searchBar.setText("")
            viewBinding.searchBar.menu.findItem(R.id.close_button)?.setVisible(false)
        }
    }
    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupObservers()
        searchSuggestionsAdapter = SearchSuggestionsAdapter(
            onItemClick = {
                viewBinding.searchBar.setText(it.query)
                viewModel.searchQuery = it.query
                viewBinding.searchView.hide()
                viewModel.search()
                showResults(true)
            },
            onItemDeleteClick = {
                viewModel.deleteSuggestion(it)
            }
        )
        viewBinding.searchHistoryRecyclerView.apply {
            adapter = searchSuggestionsAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        newestUsersAdapter = UserCarouselAdapter(
            onProfileClick = {
                ProfileActivity.startActivity(requireContext(), it)
            }
        )
        viewBinding.newUsersRecyclerView.apply {
            adapter = newestUsersAdapter
            layoutManager = CarouselLayoutManager(UncontainedCarouselStrategy())
        }

        searchResultsAdapter = SearchResultsAdapter(
            onItemClick = {
                when(it.type) {
                    SearchResultType.Profile -> {
                        it.resourceId.toIntOrNull()?.also { ProfileActivity.startActivity(requireContext(), it) }
                    }
                    SearchResultType.Post -> {
                        it.resourceId.toLongOrNull()?.also { PostViewerActivity.startActivity(requireContext(), it) }
                    }
                    SearchResultType.Hashtag -> {
                        HashtagsPostsActivity.startActivity(requireContext(), it.resourceId)
                    }
                    SearchResultType.Unknown -> {}
                }
            }
        )
        viewBinding.recyclerViewSearchResults.adapter = searchResultsAdapter
        viewBinding.recyclerViewSearchResults.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun setupListeners() {
        viewBinding.searchView.editText.doAfterTextChanged { searchText ->
            if(searchText != null) {
                viewModel.getSuggestions(searchText.toString())
            }

        }

        viewBinding.searchView.editText.setOnEditorActionListener { v, actionId, event ->
            Log.d(LOG_TAG, "actionId: $actionId; event: $event")
            viewBinding.searchBar.setText(viewBinding.searchView.text)
            viewBinding.searchView.hide()
            viewModel.search()
            showResults(true)
            true
        }

        viewBinding.searchBar.setOnMenuItemClickListener {
            if(it.itemId == R.id.close_button) {
                showResults(false)
                true
            } else {
                false
            }

        }

        viewBinding.openHashtagsButton.setOnClickListener {
            findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToHashtagsFragment())
        }

        viewBinding.browseProfilesButton.setOnClickListener {
            findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToBrowseProfilesFragment())
        }
    }

    private fun setupObservers() {
        viewModel.searchSuggestions.observe(viewLifecycleOwner, searchSuggestionsObserver)
        viewModel.searchResults.observe(viewLifecycleOwner, searchResultsObserver)
        viewModel.trendingHashtags.observe(viewLifecycleOwner, trendingHashtagsObserver)
        viewModel.newestUsers.observe(viewLifecycleOwner, newestUsersObserver)
    }
}
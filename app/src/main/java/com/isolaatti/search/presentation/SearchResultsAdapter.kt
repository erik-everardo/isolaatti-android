package com.isolaatti.search.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import com.isolaatti.R
import com.isolaatti.databinding.SearchResultItemBinding
import com.isolaatti.search.data.SearchResultDto
import com.isolaatti.search.data.SearchResultType
import com.isolaatti.utils.UrlGen

class SearchResultsAdapter(
    private val onItemClick: (item: SearchResultDto ) -> Unit = {}
) : ListAdapter<SearchResultDto, SearchResultsAdapter.SearchResultViewHolder>(itemCallback) {
    companion object {
        val itemCallback = object: DiffUtil.ItemCallback<SearchResultDto>() {
            override fun areItemsTheSame(oldItem: SearchResultDto, newItem: SearchResultDto): Boolean {
                return oldItem.resourceId == newItem.resourceId && oldItem.type == newItem.type
            }

            override fun areContentsTheSame(oldItem: SearchResultDto, newItem: SearchResultDto): Boolean {
                return oldItem == newItem
            }
        }
    }
    inner class SearchResultViewHolder(val searchResultItemBinding: SearchResultItemBinding) : ViewHolder(searchResultItemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        return SearchResultViewHolder(SearchResultItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val searchResult = getItem(position)
        holder.searchResultItemBinding.apply {
            searchResultTitle.text = searchResult.title
            searchResultDescription.text = searchResult.description
            root.setOnClickListener { onItemClick(searchResult) }

            // TODO complete this
            val image = when(searchResult.type) {
                SearchResultType.Profile -> {
                    val userId = searchResult.resourceId.toIntOrNull()
                    if(userId != null) {
                        UrlGen.userProfileImage(userId)
                    } else {
                        R.drawable.baseline_search_24
                    }

                }
                else -> R.drawable.baseline_search_24
            }

            searchResultImage.load(image)
        }
    }
}
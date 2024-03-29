package com.isolaatti.search.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.isolaatti.databinding.SearchSuggestionItemBinding
import com.isolaatti.search.data.SearchHistoryEntity

class SearchSuggestionsAdapter(
    private val onItemClick: (item: SearchHistoryEntity) -> Unit = {},
    private val onItemDeleteClick: (item: SearchHistoryEntity) -> Unit = {}
) : ListAdapter<SearchHistoryEntity, SearchSuggestionsAdapter.SearchSuggestionItemViewHolder>(itemCallback) {

    inner class SearchSuggestionItemViewHolder(val searchSuggestionItemBinding: SearchSuggestionItemBinding) : ViewHolder(searchSuggestionItemBinding.root)

    companion object {
        val itemCallback = object: DiffUtil.ItemCallback<SearchHistoryEntity>() {
            override fun areItemsTheSame(oldItem: SearchHistoryEntity, newItem: SearchHistoryEntity): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: SearchHistoryEntity, newItem: SearchHistoryEntity): Boolean {
                return oldItem.query == newItem.query
            }

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchSuggestionItemViewHolder {
        return SearchSuggestionItemViewHolder(SearchSuggestionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: SearchSuggestionItemViewHolder, position: Int) {
        val searchSuggestion = getItem(position)

        holder.searchSuggestionItemBinding.suggestionText.text = searchSuggestion.query

        holder.searchSuggestionItemBinding.root.setOnClickListener { onItemClick(searchSuggestion) }
        holder.searchSuggestionItemBinding.deleteButton.setOnClickListener { onItemDeleteClick(searchSuggestion) }
    }
}
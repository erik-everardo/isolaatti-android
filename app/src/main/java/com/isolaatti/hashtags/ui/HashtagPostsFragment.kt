package com.isolaatti.hashtags.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.isolaatti.databinding.FragmentPostsHashtagBinding
import androidx.navigation.fragment.navArgs

class HashtagPostsFragment : Fragment() {

    private lateinit var binding: FragmentPostsHashtagBinding
    private val navArgs: HashtagPostsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostsHashtagBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = "#${navArgs.hashtag}"

        setupListeners()
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }
}
package com.isolaatti.images.image_chooser.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.isolaatti.databinding.FragmentImageChooserPreviewBinding
import com.isolaatti.images.image_chooser.presentation.ImageChooserViewModel

class ImageChooserPreview : Fragment() {
    private lateinit var binding: FragmentImageChooserPreviewBinding
    private val viewModel: ImageChooserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageChooserPreviewBinding.inflate(inflater)

        return binding.root
    }

    private fun showLoading(show: Boolean) {
        binding.chooseImageButton.visibility = if(show) View.INVISIBLE else View.VISIBLE
        binding.progressBarLoading.visibility = if(show) View.VISIBLE else View.INVISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.image.load(viewModel.selectedImage?.imageUrl)

        binding.chooseImageButton.setOnClickListener {
            showLoading(true)
            viewModel.choose.value = true
        }
    }
}
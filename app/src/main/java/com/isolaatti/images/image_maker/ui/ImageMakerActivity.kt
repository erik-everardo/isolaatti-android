package com.isolaatti.images.image_maker.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.isolaatti.R
import com.isolaatti.common.IsolaattiBaseActivity
import com.isolaatti.databinding.ActivityImageMakerBinding
import com.isolaatti.images.image_maker.presentation.ImageMakerViewModel
import com.isolaatti.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImageMakerActivity : IsolaattiBaseActivity() {
    private lateinit var binding: ActivityImageMakerBinding
    private val viewModel: ImageMakerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageMakerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel.imageUri = intent.data
        binding.imagePreview.load(intent.data)
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
        setupListeners()
        setupObservers()
    }

    private val onBackPressedCallback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            showExitConfirmationDialog()
        }
    }

    private fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.discard_image)
            .setPositiveButton(R.string.yes_discard_image) {_, _ ->
                finish()
            }.setNegativeButton(R.string.no, null)
            .show()
    }

    private fun setupListeners() {
        binding.uploadPhotoFab.setOnClickListener {
            viewModel.uploadPicture()
        }
        binding.toolbar.setNavigationOnClickListener {
            showExitConfirmationDialog()
        }
    }

    private fun setupObservers() {
        viewModel.image.observe(this) {
            when(it) {
                is Resource.Error -> {
                    errorViewModel.error.value = it.errorType
                    binding.progressBarLoading.visibility = View.GONE
                    binding.uploadPhotoFab.visibility = View.VISIBLE
                }
                is Resource.Loading -> {
                    binding.progressBarLoading.visibility = View.VISIBLE
                    binding.uploadPhotoFab.visibility = View.INVISIBLE
                }
                is Resource.Success -> {
                    binding.progressBarLoading.visibility = View.GONE
                    binding.uploadPhotoFab.visibility = View.VISIBLE
                    setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_IMAGE, it.data))
                    finish()
                }
            }
        }
    }

    companion object {
        const val EXTRA_IMAGE = "image"
    }
}
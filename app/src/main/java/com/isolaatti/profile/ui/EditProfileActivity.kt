package com.isolaatti.profile.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import com.isolaatti.common.IsolaattiBaseActivity
import com.isolaatti.databinding.ActivityEditProfileBinding
import com.isolaatti.profile.domain.entity.UserProfile
import com.isolaatti.profile.presentation.EditProfileViewModel
import com.isolaatti.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditProfileActivity : IsolaattiBaseActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel: EditProfileViewModel by viewModels()

    private var inputProfile: UserProfile? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupListeners()
        setupObservers()

        inputProfile = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.extras?.getSerializable(EXTRA_IN_USER_PROFILE, UserProfile::class.java)
        } else {
            intent.extras?.getSerializable(EXTRA_IN_USER_PROFILE) as UserProfile
        }

        if(inputProfile != null) {
            binding.displayName.editText?.setText(inputProfile!!.name)
            binding.description.editText?.setText(inputProfile!!.descriptionText)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val padding = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBarLayout.updatePadding(top = padding.top, left = padding.left, right = padding.right)
            insets
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.displayName.editText?.doOnTextChanged { text, _, _, _ ->
            viewModel.displayName = text.toString()
        }

        binding.description.editText?.doOnTextChanged { text, _, _, _ ->
            viewModel.description = text.toString()
        }

        binding.acceptButton.setOnClickListener {
            viewModel.updateProfile()
        }
    }

    private fun setupObservers() {
        viewModel.isValid.observe(this) {
            binding.acceptButton.isEnabled = it
        }

        viewModel.updateResult.observe(this) {
            when(it) {
                is Resource.Error -> {}
                is Resource.Loading -> {}
                is Resource.Success -> {
                    inputProfile?.apply {
                        name = viewModel.displayName
                        descriptionText = viewModel.description
                    }
                    if(inputProfile != null) {
                        val resultIntent = Intent().apply {
                            putExtra(EXTRA_OUT_USER_PROFILE, inputProfile)
                        }
                        setResult(RESULT_OK, resultIntent)
                    }

                    finish()
                }
            }
        }
    }

    companion object {
        const val EXTRA_OUT_USER_PROFILE = "out_user_profile"
        const val EXTRA_IN_USER_PROFILE = "in_user_profile"
    }
}
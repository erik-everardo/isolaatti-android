package com.isolaatti.sign_up.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.isolaatti.BuildConfig
import com.isolaatti.R
import com.isolaatti.databinding.FragmentMakeAccountBinding
import com.isolaatti.home.ui.HomeActivity
import com.isolaatti.sign_up.domain.entity.SignUpResultCode
import com.isolaatti.sign_up.presentation.MakeAccountViewModel
import com.isolaatti.sign_up.presentation.SignUpViewModel
import com.isolaatti.utils.Resource
import com.isolaatti.utils.textChanges
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class MakeAccountFragment : Fragment() {

    private lateinit var binding: FragmentMakeAccountBinding
    private val activityViewModel: SignUpViewModel by activityViewModels()
    private val viewModel: MakeAccountViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMakeAccountBinding.inflate(inflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.code = activityViewModel.code
        setupListeners()
        setupObservers()
    }

    @OptIn(FlowPreview::class)
    private fun setupListeners() {
        binding.readTermsAndConditions.setOnClickListener {
            val termsAndConditionsCustomTabsIntent = CustomTabsIntent.Builder().build()
            termsAndConditionsCustomTabsIntent.launchUrl(requireContext(), Uri.parse(BuildConfig.terms))
        }

        binding.privacyPolicy.setOnClickListener {
            val termsAndConditionsCustomTabsIntent = CustomTabsIntent.Builder().build()
            termsAndConditionsCustomTabsIntent.launchUrl(requireContext(), Uri.parse(BuildConfig.privacyPolicy))
        }
        binding.password.editText?.doOnTextChanged { text, start, before, count ->
            viewModel.password = text.toString()
        }
        binding.textUsername.editText?.textChanges()?.debounce(300)?.onEach { text ->
            viewModel.username = text.toString()
        }?.launchIn(lifecycleScope)
        binding.textDisplayName.editText?.doOnTextChanged { text, _, _, _ ->
            viewModel.displayName = text.toString()
        }
        binding.signUpButton.setOnClickListener {
            viewModel.makeAccount()
        }
    }

    private fun setupObservers() {
        viewModel.formIsValid.observe(viewLifecycleOwner) {
            binding.signUpButton.isEnabled = it
        }

        viewModel.usernameIsValid.observe(viewLifecycleOwner) {
            binding.textUsername.error = if(it) {
                null
            } else {
                getText(R.string.username_invalid_feedback)
            }
        }

        viewModel.passwordIsValid.observe(viewLifecycleOwner) {
            binding.password.error = if(it) {
                null
            } else {
                getText(R.string.password_invalid_feedback)
            }
        }

        viewModel.displayNameIsValid.observe(viewLifecycleOwner) {
            binding.textDisplayName.error = if(it){
                null
            } else {
                getString(R.string.display_name_invalid_feedback)
            }
        }

        viewModel.signUpResult.observe(viewLifecycleOwner) { resource ->

            when(resource) {
                is Resource.Error -> {}
                is Resource.Loading -> {}
                is Resource.Success -> {
                    when(resource.data?.resultCode) {
                        SignUpResultCode.EmailNotAvailable -> {
                            showNotAvailableEmailMessage()
                            viewModel.signUpResult.value = null
                        }
                        SignUpResultCode.ValidationProblems -> {
                            showValidationProblemsMessage()
                            viewModel.signUpResult.value = null
                        }
                        SignUpResultCode.UsernameUnavailable -> {
                            showUnavailableMessage()
                            viewModel.signUpResult.value = null
                        }
                        SignUpResultCode.Ok -> {
                            resource.data.session?.let { viewModel.saveSession(it) }
                            viewModel.signUpResult.value = null
                        }
                        else -> showErrorMessage()
                    }
                }
                null -> {}
            }
        }

        viewModel.sessionSaved.observe(viewLifecycleOwner) {
            Log.d("***", "sessionSaved")
            if(it == true) {
                goToHome()

                viewModel.sessionSaved.value = null
            }
        }
    }

    private fun showNotAvailableEmailMessage() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.email_used)
            .show()
    }
    private fun showValidationProblemsMessage() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.validation_error)
            .show()
    }
    private fun showErrorMessage() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.unknown_error)
            .show()
    }
    private fun showUnavailableMessage() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.username_invalid_feedback)
            .show()
    }

    private fun goToHome() {
        val intent = Intent(requireContext(), HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
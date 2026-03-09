package com.isolaatti.home.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.isolaatti.R
import com.isolaatti.auth.GetAuthCodeContract
import com.isolaatti.auth.TokenResponseWrapper
import com.isolaatti.auth.ui.Auth
import com.isolaatti.auth.ui.LocalAuth
import com.isolaatti.common.IsolaattiBaseActivity
import com.isolaatti.common.IsolaattiTheme
import com.isolaatti.databinding.ActivityHomeBinding
import com.isolaatti.home.presentation.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@AndroidEntryPoint
class HomeActivity : IsolaattiBaseActivity() {
    private val feedViewModel: MainViewModel by viewModels()

    companion object {
        const val LOG_TAG = "HomeActivity"
    }


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (!isGranted) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.push_notifications_dialog_title)
                .setMessage(R.string.push_notifications_dialog_rejected_message)
                .show()
        }
    }

    private val authLauncher = registerForActivityResult(GetAuthCodeContract()) {
        feedViewModel.authViewModelHelper.onAuthorizationResult(it.response, it.exception)
    }

    private fun doAuth() {
        feedViewModel.authViewModelHelper.onLoginRequested {
            authLauncher.launch(it)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val authState by feedViewModel.authViewModelHelper.authState.collectAsState()

            val mainViewModel: MainViewModel = hiltViewModel()


            val auth = remember(authState) {
                Auth(
                    authState = authState,
                    doAuth = {
                        lifecycleScope.launch { doAuth() }
                    },
                    onAuthRequired = {
                        mainViewModel.authViewModelHelper.onAuthRequired()
                    },
                    showAuthDialog = {
                        mainViewModel.authViewModelHelper.onShowAuth()
                    }
                )
            }

            CompositionLocalProvider(LocalAuth provides auth) {
                IsolaattiTheme {
                    MainScreen(
                        viewModel = mainViewModel
                    )
                }
            }


        }

        askNotificationPermission()

    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {

            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.push_notifications_dialog_title)
                    .setMessage(R.string.push_notifications_dialog_message)
                    .setPositiveButton(R.string.accept) { _, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {

        }
    }

}
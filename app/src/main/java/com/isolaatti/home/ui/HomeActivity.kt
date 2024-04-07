package com.isolaatti.home.ui

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.isolaatti.R
import com.isolaatti.common.IsolaattiBaseActivity
import com.isolaatti.dataStore
import com.isolaatti.databinding.ActivityHomeBinding
import com.isolaatti.home.presentation.FeedViewModel

class HomeActivity : IsolaattiBaseActivity() {
    private lateinit var viewBinding: ActivityHomeBinding
    private val feedViewModel: FeedViewModel by viewModels()

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        viewBinding.bottomNavigation?.setupWithNavController(navHostFragment.navController)
        viewBinding.navigationRail?.setupWithNavController(navHostFragment.navController)

        askNotificationPermission()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
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
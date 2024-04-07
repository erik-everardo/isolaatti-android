package com.isolaatti.about

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.isolaatti.BuildConfig
import com.isolaatti.R
import com.isolaatti.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAboutBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.blogButton.setOnClickListener {
            CustomTabsIntent.Builder()
                .build()
                .launchUrl(this, BuildConfig.blogUrl.toUri())
        }

        binding.sourceCodeButton.setOnClickListener {
            CustomTabsIntent.Builder()
                .setSendToExternalDefaultHandlerEnabled(true)
                .build()
                .launchUrl(this, BuildConfig.sourceCodeUrl.toUri())
        }

        binding.openSourceLicences.setOnClickListener {
            CustomTabsIntent.Builder()
                .build()
                .launchUrl(this, BuildConfig.openSourceLicences.toUri())
        }

        binding.privacyPolicyButton.setOnClickListener {
            CustomTabsIntent.Builder()
                .build()
                .launchUrl(this, BuildConfig.privacyPolicy.toUri())
        }

        binding.termsButon.setOnClickListener {
            CustomTabsIntent.Builder()
                .build()
                .launchUrl(this, BuildConfig.terms.toUri())
        }

        binding.appVersion.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)
    }
}
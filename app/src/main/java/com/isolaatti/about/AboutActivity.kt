package com.isolaatti.about

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.isolaatti.BuildConfig
import com.isolaatti.R
import com.isolaatti.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)


        binding = ActivityAboutBinding.inflate(layoutInflater)

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.updatePadding(top = systemBarsInsets.top, left = systemBarsInsets.left, right = systemBarsInsets.right)
            binding.root.updatePadding(bottom = systemBarsInsets.bottom, left = systemBarsInsets.left, right = systemBarsInsets.right)
            insets
        }

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
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))

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
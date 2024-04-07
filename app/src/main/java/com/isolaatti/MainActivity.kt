package com.isolaatti

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.isolaatti.auth.data.AuthRepositoryImpl
import com.isolaatti.home.ui.HomeActivity
import com.isolaatti.login.LogInActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var authRepository: AuthRepositoryImpl

    private val signInActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if(activityResult.resultCode == Activity.RESULT_OK) {
            val homeActivityIntent = Intent(this@MainActivity, HomeActivity::class.java)
            homeActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(homeActivityIntent)
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        var isLoading = true
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { isLoading }
        super.onCreate(savedInstanceState)
        // Decide what activity to start
        // Set isLoading to false when ended
        val currentToken = authRepository.getCurrentToken()

        if(currentToken == null) {
            val loginIntent = Intent(this@MainActivity, LogInActivity::class.java)
            signInActivityResult.launch(loginIntent)
        } else {
            val homeActivityIntent = Intent(this@MainActivity, HomeActivity::class.java)
            homeActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(homeActivityIntent)
        }
        isLoading = false
    }
}
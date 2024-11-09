package com.isolaatti.profile.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.app.TaskStackBuilder
import com.isolaatti.common.IsolaattiBaseActivity
import com.isolaatti.databinding.ActivityProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : IsolaattiBaseActivity() {

    lateinit var viewBinding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        viewBinding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

    }

    companion object {
        const val EXTRA_USER_ID = "user_id"

        fun getIntent(context: Context, userId: Int): Intent {
            return Intent(context, ProfileActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
            }
        }

        fun startActivity(context: Context, userId: Int) {
            context.startActivity(getIntent(context, userId))
        }

        fun getPendingIntent(context: Context, userId: Int): PendingIntent? {
            return TaskStackBuilder.create(context).run {
                addNextIntentWithParentStack(getIntent(context, userId))

                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }
        }
    }
}
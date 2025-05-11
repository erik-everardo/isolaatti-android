package com.isolaatti.audio.player

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.isolaatti.auth.domain.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MediaService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    @Inject
    lateinit var authRepository: AuthRepository

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()




        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(
                    DataSource.Factory {
                        DefaultDataSource
                            .Factory(
                                this,
                                DefaultHttpDataSource
                                    .Factory()
                                    .setDefaultRequestProperties(
                                        mapOf(
                                            "Authorization" to (authRepository.getCurrentToken()?.token ?: "")
                                        )
                                    )
                            )
                            .createDataSource()
                    }
                )
            )
            .build()
        mediaSession = MediaSession.Builder(this, player).build()

    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if(player?.playWhenReady == true) {
            player.pause()
        }
        stopSelf()
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
}
package com.isolaatti.video_streaming.ui

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioFormat
import android.media.MediaCodecInfo
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.util.TypedValue
import android.view.SurfaceHolder
import android.view.WindowInsets
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.ZoomState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.isolaatti.databinding.StreamerActivityBinding
import com.pedro.common.ConnectChecker
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.library.generic.GenericStream
import com.pedro.library.rtmp.RtmpStream
import com.pedro.library.util.sources.audio.AudioSource
import com.pedro.library.util.sources.audio.MicrophoneSource
import com.pedro.library.util.sources.video.Camera2Source
import com.pedro.library.util.sources.video.VideoSource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StreamerActivity : AppCompatActivity() {
    private val TAG = "StreamerActivity"
    companion object {
        const val REQUEST_CODE = 100
        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }

    private fun askForPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if(permissionsToRequest.isNotEmpty()) {
            requestPermissions(permissionsToRequest, REQUEST_CODE)
        } else {
            startCamera()
        }
    }

    private lateinit var imagePreview: Preview
    private var camera: Camera? = null

    val connectionChecker = object : ConnectChecker {
        override fun onAuthError() {
            android.util.Log.e(TAG, "Auth error")
        }

        override fun onAuthSuccess() {
            android.util.Log.i(TAG, "Auth success")
        }

        override fun onConnectionFailed(reason: String) {
            android.util.Log.e(TAG, "Connection failed: $reason")
        }

        override fun onConnectionStarted(url: String) {
            android.util.Log.i(TAG, "Connection started: $url")
        }

        override fun onConnectionSuccess() {
            android.util.Log.i(TAG, "Connection success")
        }

        override fun onDisconnect() {
            android.util.Log.i(TAG, "Disconnected")
        }

        override fun onNewBitrate(bitrate: Long) {
            android.util.Log.i(TAG, "New bitrate: $bitrate")
        }

    }



    var genericStream: GenericStream? = null
    private var _videoSource: Camera2Source? = null
    private var _audioSource: AudioSource? = null


    private fun startCamera() {

        val videoSource = Camera2Source(this)
        val audioSource = MicrophoneSource()

        _videoSource = videoSource
        _audioSource = audioSource

        val stream = GenericStream(this, connectionChecker, videoSource, audioSource)
        genericStream = stream


        val prepared = try {
            stream.prepareVideo(1280, 720, 2500, rotation = 90, profile = MediaCodecInfo.CodecProfileLevel.AVCProfileMain) &&
                    stream.prepareAudio(44100, true, 160)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Illegal argument", e)
            false
        }

        if(prepared) {
            Log.i(TAG, "Prepared")
            binding.preview.holder.addCallback(object: SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {

                }

                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                    if (!stream.isOnPreview) stream.startPreview(binding.preview)
                    else stream.stopPreview()

                    stream.startStream("rtmp://isolaatti.com:1935/live?34D7C931BC7F682FB84B/c0c2c0b2-3b51-4f10-a21c-b97956651be2")

                    binding.switchCameraButton.isEnabled = true
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {

                }
            })



        } else {
            Log.e(TAG, "Not prepared")
        }


    }

    override fun onPause() {
        super.onPause()

        if(genericStream?.isOnPreview == true) {
            genericStream?.stopPreview()
        }

        if(genericStream?.isStreaming == true){
            genericStream?.stopStream()
        }
    }

    override fun onResume() {
        super.onResume()

        askForPermissions()
    }

    private lateinit var binding: StreamerActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = StreamerActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)



        ViewCompat.setOnApplyWindowInsetsListener(binding.root) {
                root, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.actionsCard.updateLayoutParams<ConstraintLayout.LayoutParams> {
                bottomMargin = insets.bottom + TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics).toInt()
            }
            windowInsets
        }

        binding.switchCameraButton.setOnClickListener {
            _videoSource?.switchCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_CODE) {
            if(grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startCamera()
            }
        }
    }
}
package com.isolaatti.audio.recorder.ui

import android.Manifest
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources.Theme
import android.media.AudioManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.view.get
import androidx.core.widget.doOnTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import com.google.android.material.tabs.TabLayoutMediator
import com.isolaatti.R
import com.isolaatti.audio.common.domain.Playable
import com.isolaatti.audio.drafts.domain.AudioDraft
import com.isolaatti.audio.player.AudioPlayerConnector
import com.isolaatti.audio.recorder.presentation.AudioRecorderViewModel
import com.isolaatti.audio.recorder.presentation.RecorderPagerAdapter
import com.isolaatti.databinding.ActivityAudioRecorderBinding
import com.isolaatti.utils.Resource
import com.isolaatti.utils.clockFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.UUID

@AndroidEntryPoint
class AudioRecorderActivity : AppCompatActivity() {

    companion object {
        const val LOG_TAG = "AudioRecorderActivity"

        const val IN_EXTRA_DRAFT_ID = "in_draft_id"
        const val OUT_EXTRA_DRAFT = "out_draft"
    }

    private lateinit var binding: ActivityAudioRecorderBinding

    private var audioRecorder: MediaRecorder? = null
    private var recordingPaused = false
    private var audioPlayerConnector: AudioPlayerConnector? = null

    private val viewModel: AudioRecorderViewModel by viewModels()
    private lateinit var outputFile: String

    private val requestAudioPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if(it) {
            startRecording()
        } else {
            showPermissionRationale()
        }
    }

    private val audioPlayerListener = object: AudioPlayerConnector.DefaultListener() {
        override fun onPlaying(isPlaying: Boolean, audio: Playable) {
            if(isPlaying) {
                binding.seekbar.isEnabled = true
                binding.playPauseButton.icon = ResourcesCompat.getDrawable(resources, R.drawable.baseline_pause_24, theme)
            } else {
                binding.playPauseButton.icon = ResourcesCompat.getDrawable(resources, R.drawable.baseline_play_arrow_24, theme)
            }

        }

        override fun onEnded(audio: Playable) {
            binding.seekbar.isEnabled = false
            binding.seekbar.progress = 0
        }

        override fun durationChanged(duration: Int, audio: Playable) {
            super.durationChanged(duration, audio)
            binding.seekbar.max = duration
            totalTime = duration
        }

        override fun progressChanged(second: Int, audio: Playable) {
            binding.seekbar.progress = second
            setDisplayTime(second, true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAudioRecorderBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Path where results are stored
        File("${filesDir.absolutePath}/audios/").let {
            if(!it.isDirectory) {
                it.mkdir()
            }
        }

        binding.seekbar.isEnabled = false
        outputFile =  "${cacheDir.absolutePath}/audio_recorder.3gp"

        setupListeners()
        setupObservers()

        audioPlayerConnector = AudioPlayerConnector(this)
        audioPlayerConnector?.addListener(audioPlayerListener)

        lifecycle.addObserver(audioPlayerConnector!!)

    }

    private fun checkRecordAudioPermission(): Boolean {
        return when {
            PermissionChecker.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PERMISSION_GRANTED -> true
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO) -> {
                showPermissionRationale()
                false
            }

            else -> {
                requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                false
            }
        }
    }

    private fun showPermissionRationale() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Record audio permission")
            .setMessage("We need permission to access your microphone so that you can record your audio. Go to settings.")
            .setPositiveButton("Go to settings"){_, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            .setNegativeButton("No, thanks", null)
            .show()
    }


    private fun setupListeners() {
        binding.recordButton.setOnClickListener {
            if(checkRecordAudioPermission()) {
                Log.d(LOG_TAG, "Starts recording")
                startRecording()
            } else {
                Log.d(LOG_TAG, "Failed to start recording: mic permission not granted")
            }
        }
        binding.stopRecording.setOnClickListener {
            stopRecording()
        }

        binding.cancelButton.setOnClickListener {
            discardRecording()
        }

        binding.pauseRecording.setOnClickListener {
            pauseRecording()
        }

        binding.playPauseButton.setOnClickListener {
            playPauseRecording(object: Playable() {
                override val uri: Uri
                    get() = outputFile.toUri()
                override val thumbnail: String?
                    get() = null

            })
        }

        binding.inputDraftName.editText?.doOnTextChanged { text, _, _, _ ->
            viewModel.name = text.toString()
        }

        binding.toolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.save_draft -> {

                    viewModel.relativePath = "/audios/${UUID.randomUUID()}.3gp"
                    File(outputFile).run {
                        viewModel.size = length()
                        copyTo(File(filesDir.absolutePath, viewModel.relativePath), overwrite = true)
                    }

                    viewModel.saveAudioDraft()
                    true
                }
                else -> false
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.canSave.observe(this) {
            binding.toolbar.menu.findItem(R.id.save_draft).isEnabled = it
        }

        // audio draft is saved!
        viewModel.audioDraft.observe(this) {
            val result = Intent().apply {
                putExtra(OUT_EXTRA_DRAFT, it)
            }
            setResult(RESULT_OK, result)
            finish()
        }
    }

    // region timer
    private var timer: Job? = null
    private var timerValue = 0
    private var totalTime = 0

    private fun setDisplayTime(seconds: Int, showTotalTime: Boolean) {
        binding.time.text = buildString {
            append(seconds.clockFormat())

            if(showTotalTime) {
                append("/")
                append(totalTime.clockFormat())
            }
        }
    }
    private fun startTimerRecorder() {

        timer = CoroutineScope(Dispatchers.Main).launch {
            setDisplayTime(timerValue, false)
            delay(1000)
            timerValue++
            startTimerRecorder()
        }
    }


    private fun stopTimer() {
        timer?.cancel()
    }

    // end region

    // region record functions
    private fun startRecording() {
        viewModel.onClearAudio()
        recordingPaused = false
        audioRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(outputFile)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
                start()
                timerValue = 0
                startTimerRecorder()
                binding.viewAnimator.displayedChild = 1

            } catch(e: IOException) {
                Log.e(LOG_TAG, "prepare() failed\n${e.message}")
            }
        }
    }

    private fun stopRecording() {
        audioRecorder?.apply {
            stop()
            release()
        }
        stopTimer()
        audioRecorder = null

        // shows third state: audio recorded
        binding.viewAnimator.displayedChild = 2
        recordingPaused = false
        binding.pauseRecording.icon = ResourcesCompat.getDrawable(resources, R.drawable.baseline_pause_24, theme)
        binding.pauseRecording.setIconTintResource(com.google.android.material.R.color.m3_icon_button_icon_color_selector)
        viewModel.onAudioRecorded()
    }

    private fun pauseRecording() {
        if(recordingPaused) {
            binding.pauseRecording.icon = ResourcesCompat.getDrawable(resources, R.drawable.baseline_pause_24, theme)
            binding.pauseRecording.setIconTintResource(com.google.android.material.R.color.m3_icon_button_icon_color_selector)
            audioRecorder?.resume()
            startTimerRecorder()
        } else {
            binding.pauseRecording.icon = ResourcesCompat.getDrawable(resources, R.drawable.baseline_circle_24, theme)
            binding.pauseRecording.setIconTintResource(R.color.danger)
            audioRecorder?.pause()
            stopTimer()
        }
        recordingPaused = !recordingPaused
    }

    private fun discardRecording(){
        File(outputFile).apply {
            try {
                delete()
            } catch(e: SecurityException) {
                Log.e(LOG_TAG, "Could not delete file\n${e.message}")
            }
        }
        binding.viewAnimator.displayedChild = 0
        viewModel.onClearAudio()
        totalTime = 0
        timerValue = 0
        binding.time.text = ""
        audioPlayerConnector?.stopPlayback()
    }

    // end region

    private fun playPauseRecording(audioDraft: Playable) {
        audioPlayerConnector?.playPauseAudio(audioDraft)
    }


}
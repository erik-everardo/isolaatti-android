package com.isolaatti.posting.posts.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.isolaatti.R
import com.isolaatti.audio.audio_selector.ui.AudioSelectorContract
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.audio.common.domain.Playable
import com.isolaatti.audio.drafts.domain.AudioDraft
import com.isolaatti.audio.player.AudioPlayerConnector
import com.isolaatti.audio.recorder.ui.AudioRecorderContract
import com.isolaatti.databinding.FragmentMarkdownEditingBinding
import com.isolaatti.images.image_chooser.ui.ImageChooserContract
import com.isolaatti.posting.link_creator.presentation.LinkCreatorViewModel
import com.isolaatti.posting.link_creator.ui.LinkCreatorFragment
import com.isolaatti.posting.posts.presentation.CreatePostViewModel

class PostEditingFragment : Fragment(){
    companion object {
        const val LOG_TAG = "PostEditingFragment"
    }


    private lateinit var binding: FragmentMarkdownEditingBinding
    private val viewModel: CreatePostViewModel by activityViewModels()
    private val linkCreatorViewModel: LinkCreatorViewModel by viewModels()

    private var audioPlayerConnector: AudioPlayerConnector? = null

    private val audioRecorderLauncher = registerForActivityResult(AudioRecorderContract()) { audioDraft ->
        if(audioDraft != null) {
            viewModel.putAudioDraft(audioDraft)
            binding.viewAnimator.displayedChild = 1
        }
    }

    private val imageChooserLauncher = registerForActivityResult(ImageChooserContract()) { image ->
        Log.d(LOG_TAG, "${image?.markdown}")

        if(image != null) {
            viewModel.content += "\n\n ${image.markdown}"
            binding.filledTextField.editText?.setText(viewModel.content)
        }


    }

    private val audioSelectorLauncher = registerForActivityResult(AudioSelectorContract()) {

    }

    private val audioListener = object: AudioPlayerConnector.Listener {
        override fun durationChanged(duration: Int, audio: Playable) {
            binding.audioItem.audioProgress.max = duration
        }

        override fun onEnded(audio: Playable) {
            binding.audioItem.audioProgress.progress = 0
        }

        override fun progressChanged(second: Int, audio: Playable) {
            binding.audioItem.audioProgress.progress = second
        }

        override fun onPlaying(isPlaying: Boolean, audio: Playable) {
            binding.audioItem.playButton.icon = AppCompatResources.getDrawable(requireContext(), if(isPlaying) R.drawable.baseline_pause_circle_24 else R.drawable.baseline_play_circle_24)
        }

        override fun isLoading(isLoading: Boolean, audio: Playable) {
            binding.audioItem.audioProgress.isIndeterminate = isLoading
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioPlayerConnector = AudioPlayerConnector(requireContext())
        audioPlayerConnector?.addListener(audioListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMarkdownEditingBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycle.addObserver(audioPlayerConnector!!)

        viewLifecycleOwner.lifecycle.addObserver(object: LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                Log.d(LOG_TAG, event.toString())
            }
        })

        setupListeners()
        setupObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewLifecycleOwner.lifecycle.removeObserver(audioPlayerConnector!!)
    }

    private fun setupListeners() {
        binding.filledTextField.editText?.setText(viewModel.content)
        binding.filledTextField.requestFocus()
        binding.filledTextField.editText?.doOnTextChanged { text, _, _, _ ->
            // make better validation :)
            viewModel.validation.postValue(!text.isNullOrEmpty())
            viewModel.content = text.toString()
        }
        binding.addImageButton.setOnClickListener {
            insertImage()
        }
        binding.addLinkButton.setOnClickListener {
            insertLink()
        }

        binding.addAudioButton.setOnClickListener {

            val popupMenu = PopupMenu(requireContext(), it)

            popupMenu.inflate(R.menu.attach_audio_menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when(menuItem.itemId){
                    R.id.record_new_audio_item -> {
                        audioRecorderLauncher.launch(null)
                        true
                    }
                    R.id.select_from_audios -> {
                        audioSelectorLauncher.launch(AudioSelectorContract.SelectorConfig(false, null))
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }

        binding.removeAudio.setOnClickListener {
            binding.viewAnimator.displayedChild = 0
            viewModel.removeAudio()
        }
    }

    private fun setupObservers(){
        viewModel.postToEdit.observe(viewLifecycleOwner) {
            binding.filledTextField.editText?.setText(it.content)
        }
        linkCreatorViewModel.inserted.observe(viewLifecycleOwner) {
            if(it) {
                viewModel.content += " ${linkCreatorViewModel.markdown}"
                binding.filledTextField.editText?.setText(viewModel.content)
                linkCreatorViewModel.inserted.value = false
            }
        }

        viewModel.audioAttachment.observe(viewLifecycleOwner) { playable ->
            if(playable != null) {
                audioPlayerConnector?.stopPlayback()
                binding.audioItem.playButton.setOnClickListener {
                    audioPlayerConnector?.playPauseAudio(playable)
                }

                when(playable) {
                    is Audio -> {
                        binding.audioItem.textViewDescription.text = playable.name
                    }
                    is AudioDraft -> {
                        binding.audioItem.textViewDescription.text = playable.name
                    }
                }
            }
        }
    }


    private fun insertImage() {
        imageChooserLauncher.launch(ImageChooserContract.Requester.UserPost)
    }

    private fun insertLink() {
        LinkCreatorFragment().show(childFragmentManager, null)
    }
}
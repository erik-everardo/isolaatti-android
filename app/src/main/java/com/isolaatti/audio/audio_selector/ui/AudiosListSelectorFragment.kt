package com.isolaatti.audio.audio_selector.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.isolaatti.audio.audio_selector.presentation.AudioSelectorViewModel
import com.isolaatti.audio.audios_list.presentation.AudiosAdapter
import com.isolaatti.databinding.FragmentAudiosListSelectorBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AudiosListSelectorFragment : Fragment() {
    private lateinit var binding: FragmentAudiosListSelectorBinding
    private val viewModel: AudioSelectorViewModel by activityViewModels()

    private var mode: Int = ARG_VAL_MODE_AUDIOS
    private var squadId: String? = null

    private lateinit var adapter: AudiosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getInt(ARG_MODE)?.let { mode = it }
        arguments?.getString(ARG_SQUAD_ID)?.let { squadId = it }


        when(mode) {
            ARG_VAL_MODE_AUDIOS -> {}
            ARG_VAL_MODE_DRAFTS -> {
                viewModel.getAudioDrafts()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAudiosListSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = AudiosAdapter(
            onPlayClick = {

            },
            onOptionsClick = {audio, button ->
                false
            }
        )
        binding.recycler.adapter = adapter

        when(mode) {
            ARG_VAL_MODE_AUDIOS -> {
                viewModel.audios.observe(viewLifecycleOwner) {

                }
            }
            ARG_VAL_MODE_DRAFTS -> {
                viewModel.drafts.observe(viewLifecycleOwner) {

                }
            }
        }
    }

    companion object {
        private const val ARG_MODE = "mode"
        private const val ARG_SQUAD_ID = "squadId"
        private const val ARG_VAL_MODE_DRAFTS = 0
        private const val ARG_VAL_MODE_AUDIOS = 1
        fun getInstance(): AudiosListSelectorFragment {
            return AudiosListSelectorFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_MODE, ARG_VAL_MODE_AUDIOS)
                }
            }
        }

        fun getInstanceForDrafts(): AudiosListSelectorFragment {
            return AudiosListSelectorFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_MODE, ARG_VAL_MODE_DRAFTS)
                }
            }
        }
        fun getInstanceForSquad(squadId: String): AudiosListSelectorFragment {
            return AudiosListSelectorFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SQUAD_ID, squadId)
                }
            }
        }
    }
}
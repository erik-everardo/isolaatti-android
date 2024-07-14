package com.isolaatti.audio.audio_selector.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.isolaatti.R
import com.isolaatti.audio.audio_selector.presentation.AudioSelectorFragmentAdapter
import com.isolaatti.audio.audio_selector.presentation.AudioSelectorViewModel
import com.isolaatti.audio.recorder.ui.AudioRecorderContract
import com.isolaatti.common.IsolaattiBaseActivity
import com.isolaatti.common.SortingEnum
import com.isolaatti.databinding.ActivityAudioSelectorBinding

class AudioSelectorActivity : IsolaattiBaseActivity() {
    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_FOR_SQUAD = "forSquad"
        const val OUT_EXTRA_PLAYABLE = "playable"
    }

    private lateinit var binding: ActivityAudioSelectorBinding
    private val viewModel: AudioSelectorViewModel by viewModels()

    private val audioRecorderLauncher = registerForActivityResult(AudioRecorderContract()) {
        
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAudioSelectorBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.viewpager.adapter = AudioSelectorFragmentAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.viewpager) {tab, position ->
            when(position) {
                0 -> tab.text = getString(R.string.audios)
                1 -> tab.text = getString(R.string.drafts)
            }
        }.attach()

        binding.sort.setText(R.string.descending_by_name)

        setupListeners()

    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.newAudio.setOnClickListener {
            audioRecorderLauncher.launch(null)
        }

        binding.sort.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.inflate(R.menu.audios_sort_menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                binding.sort.text = menuItem.title
                when(menuItem.itemId) {
                    R.id.desc_by_name -> {
                        viewModel.setSorting(SortingEnum.DescendingByName)
                        true
                    }
                    R.id.asc_by_name -> {
                        viewModel.setSorting(SortingEnum.AscendingByName)
                        true
                    }
                    R.id.asc_by_creation_date -> {
                        viewModel.setSorting(SortingEnum.AscendingByCreationDate)
                        true
                    }
                    R.id.desc_by_creation_date -> {
                        viewModel.setSorting(SortingEnum.DescendingByCreationDate)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }


}
package com.isolaatti.audio.audio_selector.presentation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.isolaatti.audio.audio_selector.ui.AudiosListSelectorFragment

class AudioSelectorFragmentAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> AudiosListSelectorFragment.getInstance()
            1 -> AudiosListSelectorFragment.getInstanceForDrafts()
            else -> Fragment()
        }
    }
}
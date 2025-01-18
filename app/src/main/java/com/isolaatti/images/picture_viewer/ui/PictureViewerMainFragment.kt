package com.isolaatti.images.picture_viewer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.isolaatti.databinding.FragmentMainPictureViewerBinding
import com.isolaatti.images.common.domain.entity.Image
import com.isolaatti.images.picture_viewer.presentation.PictureViewerViewPagerAdapter

class PictureViewerMainFragment : Fragment() {

    private lateinit var binding: FragmentMainPictureViewerBinding

    lateinit var images: Array<Image>

    private val onPageChangeCallback = object: ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            //binding.imageAuthor.text = images[position].username
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainPictureViewerBinding.inflate(inflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        images = requireActivity().intent.extras?.getSerializable(PictureViewerActivity.EXTRA_IMAGES) as Array<Image>
        val position = requireActivity().intent.extras?.getInt(PictureViewerActivity.EXTRA_IMAGE_POSITiON) ?: 0
        val adapter = PictureViewerViewPagerAdapter(this, images)
        binding.viewpager.adapter = adapter
        binding.viewpager.setCurrentItem(position, false)
        binding.viewpager.registerOnPageChangeCallback(onPageChangeCallback)
        //binding.imageAuthor.text = images[position].username
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewpager.unregisterOnPageChangeCallback(onPageChangeCallback)
    }

    fun enableViewPagerUserInput(enabled: Boolean) {
        binding.viewpager.isUserInputEnabled = enabled
    }
}
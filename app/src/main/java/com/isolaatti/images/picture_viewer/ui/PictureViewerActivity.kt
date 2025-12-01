package com.isolaatti.images.picture_viewer.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.isolaatti.databinding.ActivityPictureViewerBinding
import com.isolaatti.images.common.domain.entity.Image
import com.isolaatti.images.common.domain.entity.RemoteImage

class PictureViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPictureViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityPictureViewerBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }



    companion object {
        const val EXTRA_IMAGES = "images"
        const val EXTRA_IMAGE_POSITiON = "position"

        fun startActivityWithImages(context: Context, images: ArrayList<Image>, position: Int = 0) {
            if(images.isEmpty()) {
                return
            }
            val intent = Intent(context, PictureViewerActivity::class.java)
            intent.putExtra(EXTRA_IMAGES, images)
            intent.putExtra(EXTRA_IMAGE_POSITiON, position)
            context.startActivity(intent)
        }
    }
}
package com.isolaatti.common

import android.content.Context
import coil3.ImageLoader
import com.isolaatti.MyApplication

object CoilImageLoader {
    val imageLoader by lazy {
        ImageLoader
            .Builder(MyApplication.myApp)
            .build()
    }

    fun getImageLoader(context: Context): ImageLoader {
        return ImageLoader
            .Builder(context)
            .build()
    }
}
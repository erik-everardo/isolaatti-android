package com.isolaatti.images

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.isolaatti.MyApplication
import java.io.File
import java.util.Calendar

fun Context.makePhotoUri(): Uri {
    val cacheFile = File(filesDir, "temp_picture_${Calendar.getInstance().timeInMillis}")
    return FileProvider.getUriForFile(this, "${MyApplication.Companion.myApp.packageName}.provider", cacheFile)
}
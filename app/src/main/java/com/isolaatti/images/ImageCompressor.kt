package com.isolaatti.images

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject

class ImageCompressor @Inject constructor(private val contentResolver: ContentResolver) {
    companion object {
        const val LOG_TAG = "ImageCompressor"
    }
    fun compressImage(imageUri: Uri): ByteArrayOutputStream? {
        var imageInputStream: InputStream? = null
        try {
            imageInputStream = contentResolver.openInputStream(imageUri)
            var rotationDegrees = 0
            imageInputStream?.use {
                val exifInterface = ExifInterface(it)
                rotationDegrees = exifInterface.rotationDegrees
            }
            imageInputStream = contentResolver.openInputStream(imageUri)

            var bitmap = BitmapFactory.decodeStream(imageInputStream)
            if(bitmap == null) {
                Log.e(LOG_TAG, "Error resolving image with content resolver")
                return null
            }

            val outputStream = ByteArrayOutputStream()
            if(rotationDegrees != 0) {
                val matrix = Matrix()
                matrix.postRotate(rotationDegrees.toFloat())
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                bitmap.compress(Bitmap.CompressFormat.WEBP, 50, outputStream)
            } else {
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 50, outputStream)
            }
            return outputStream
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error compressing image $e")
            return null
        } finally {
            imageInputStream?.close()
        }
    }
}
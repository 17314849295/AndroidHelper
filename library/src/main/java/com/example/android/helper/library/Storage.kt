package com.example.android.helper.library

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.WorkerThread
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
@WorkerThread
fun saveBitmapToAlbum(context: Context, bitmap: Bitmap): Uri? {
    val displayName = "${SimpleDateFormat.getDateInstance().format(Date())}.jpg"
    val mimeType = "image/jpeg"
    val bitmapDetails = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        bitmapDetails.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
    } else {
        bitmapDetails.put(
            MediaStore.MediaColumns.DATA,
            "${Environment.getExternalStorageDirectory().path}/${Environment.DIRECTORY_DCIM}/$displayName"
        )
    }
    val contentResolver = context.applicationContext.contentResolver
    val uri = contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        bitmapDetails
    )
    if (uri != null) {
        contentResolver.openOutputStream(uri)?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
    }
    return uri
}

@Suppress("DEPRECATION")
@WorkerThread
fun saveBitmapToAlbum(context: Context, uri: Uri): Uri? {
    val bitmap = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    } else {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
    return saveBitmapToAlbum(context, bitmap)
}
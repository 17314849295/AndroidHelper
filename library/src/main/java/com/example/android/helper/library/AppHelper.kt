package com.example.android.helper.library

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class AppHelper(
    private val activity: FragmentActivity
) : LifecycleObserver {

    init {
        activity.lifecycle.addObserver(this)
    }

    private var takePictureContinuation: Continuation<Uri?>? = null
    private var takePictureLauncher: ActivityResultLauncher<Uri>? = null
    private var takedPictureUri: Uri? = null
    private var takePicture = false

    private var pickPictureLauncher: ActivityResultLauncher<String>? = null
    private var pickPictureContinuation: Continuation<Uri>? = null
    private var pickPicture = false

    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        takePictureLauncher =
            activity.registerForActivityResult(ActivityResultContracts.TakePicture()) {
                if (it) {
                    takePictureContinuation?.resume(takedPictureUri)
                } else {
                    takePictureContinuation?.resume(null)
                }
            }
        pickPictureLauncher =
            activity.registerForActivityResult(ActivityResultContracts.GetContent()) {
                pickPictureContinuation?.resume(it)
            }
    }

    suspend fun takePicture() = suspendCancellableCoroutine<Uri?> { con ->
        if (con.isCompleted) return@suspendCancellableCoroutine
        takePicture = true
        con.invokeOnCancellation { con.resume(null) }
        takePictureContinuation = con
        val photoFile = try {
            createImageFile()
        } catch (e: Exception) {
            null
        }
        takedPictureUri = photoFile?.let {
            FileProvider.getUriForFile(activity, "${activity.packageName}.fileprovider", it)
        }
        if (takedPictureUri != null) {
            takePictureLauncher?.launch(takedPictureUri)
        } else {
            con.resume(null)
        }
    }

    suspend fun pickPicture(mimeType: String? = "image/jpeg") =
        suspendCancellableCoroutine<Uri> { con ->
            pickPicture = true
            pickPictureContinuation = con
            pickPictureLauncher?.launch(mimeType)
        }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat.getDateInstance().format(Date())
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(timestamp, ".jpg", storageDir)
    }
}

fun Activity.shareImages(title: String = "Share image", vararg imageUris: Uri) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND_MULTIPLE
        val imageUriList = arrayListOf<Uri>()
        imageUriList.addAll(imageUris)
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUriList)
        type = "image/*"
    }
    startActivity(Intent.createChooser(shareIntent, title))
}

fun Activity.openGooglePlay() {
    try {
        val intent = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
        startActivity(intent)
    } catch (e: Exception) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "https://play.google.com/store/apps/details?id=$packageName".toUri()
        )
        startActivity(intent)
    }
}
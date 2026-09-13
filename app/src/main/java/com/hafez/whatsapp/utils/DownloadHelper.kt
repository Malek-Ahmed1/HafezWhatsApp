package com.hafez.status.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.widget.Toast
import com.hafez.status.R
import com.hafez.status.data.MediaType
import java.io.File
import java.io.IOException

object DownloadHelper {

    fun downloadFile(context: Context, source: File, mediaType: MediaType) {
        val folderName = if (mediaType == MediaType.IMAGE) "Hafez/Images" else "Hafez/Videos"
        val destDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            folderName
        )
        if (!destDir.exists()) destDir.mkdirs()

        val destFile = File(destDir, source.name)

        try {
            source.inputStream().use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
            MediaScannerConnection.scanFile(
                context,
                arrayOf(destFile.absolutePath),
                null, null
            )
            Toast.makeText(context, R.string.saved_success, Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

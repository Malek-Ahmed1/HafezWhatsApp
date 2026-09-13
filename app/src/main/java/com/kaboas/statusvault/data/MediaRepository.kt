package com.kaboas.statusvault.data

import android.os.Environment
import java.io.File

enum class MediaType { IMAGE, VIDEO }

object MediaRepository {

    fun getStatusDirs(): List<File> {
        val dirs = mutableListOf<File>()
        val paths = listOf(
            "/WhatsApp/Media/.Statuses",
            "/WhatsApp Business/Media/.Statuses",
            "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses",
            "/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses"
        )
        paths.forEach { path ->
            val dir = File(Environment.getExternalStorageDirectory(), path)
            if (dir.exists() && dir.isDirectory) dirs.add(dir)
        }
        return dirs
    }

    fun listMedia(type: MediaType): List<File> {
        val result = mutableListOf<File>()
        val extensions = when (type) {
            MediaType.IMAGE -> listOf("jpg", "jpeg", "png", "webp")
            MediaType.VIDEO -> listOf("mp4", "mkv", "3gp", "avi")
        }
        getStatusDirs().forEach { dir ->
            dir.listFiles()?.forEach { file ->
                if (file.extension.lowercase() in extensions) result.add(file)
            }
        }
        return result.sortedByDescending { it.lastModified() }
    }
}

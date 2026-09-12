package com.hafez.whatsapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.hafez.whatsapp.adapter.MediaAdapter
import com.hafez.whatsapp.data.AppDatabase
import com.hafez.whatsapp.data.FavoriteEntity
import com.hafez.whatsapp.data.MediaRepository
import com.hafez.whatsapp.data.MediaType
import com.hafez.whatsapp.utils.DownloadHelper
import kotlinx.coroutines.launch
import java.io.File

class StatusMediaActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var db: AppDatabase

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status_media)

        db = AppDatabase.getInstance(this)
        recycler = findViewById(R.id.recyclerMedia)
        recycler.layoutManager = GridLayoutManager(this, 2)

        checkPermissions()

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                loadMedia(tab.position)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        loadMedia(0)
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun loadMedia(tabIndex: Int) {
        val files: List<File> = when (tabIndex) {
            0 -> MediaRepository.listMedia(MediaType.IMAGE)
            1 -> MediaRepository.listMedia(MediaType.VIDEO)
            else -> emptyList()
        }

        val adapter = MediaAdapter(
            files,
            onDownload = { file ->
                val type = if (file.extension.lowercase() in listOf("mp4", "mkv", "3gp", "avi"))
                    MediaType.VIDEO else MediaType.IMAGE
                DownloadHelper.downloadFile(this, file, type)
            },
            onFavorite = { file ->
                lifecycleScope.launch {
                    db.favoriteDao().insert(
                        FavoriteEntity(file.absolutePath, file.name, file.extension)
                    )
                    Toast.makeText(this@StatusMediaActivity, R.string.added_favorite, Toast.LENGTH_SHORT).show()
                }
            }
        )
        recycler.adapter = adapter

        if (files.isEmpty() && tabIndex != 2) {
            Toast.makeText(this, "No media found", Toast.LENGTH_SHORT).show()
        }
    }
}

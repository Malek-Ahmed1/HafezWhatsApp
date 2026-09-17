package com.kaboas.statusvault

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.kaboas.statusvault.data.MediaType
import com.kaboas.statusvault.fragments.HomeFragment
import com.kaboas.statusvault.fragments.MediaFragment

class MainActivity : AppCompatActivity() {

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkStoragePermission()

        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            switchFragment(HomeFragment())
        }
        findViewById<LinearLayout>(R.id.navVideos).setOnClickListener {
            switchFragment(MediaFragment.newInstance(MediaType.VIDEO))
        }
        findViewById<LinearLayout>(R.id.navPhotos).setOnClickListener {
            switchFragment(MediaFragment.newInstance(MediaType.IMAGE))
        }
        findViewById<LinearLayout>(R.id.navFavorites).setOnClickListener {
            switchFragment(MediaFragment.newInstanceFavorites())
        }

        if (savedInstanceState == null) {
            switchFragment(HomeFragment())
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun checkStoragePermission() {
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
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    fun openWhatsApp() {
        var intent = packageManager.getLaunchIntentForPackage("com.whatsapp")
        if (intent == null) {
            intent = packageManager.getLaunchIntentForPackage("com.whatsapp.w4b")
        }
        if (intent != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "WhatsApp is not installed", Toast.LENGTH_SHORT).show()
        }
    }
}

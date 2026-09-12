package com.hafez.whatsapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnOpenWhatsApp).setOnClickListener {
            openWhatsApp()
        }

        findViewById<Button>(R.id.btnSeeStatus).setOnClickListener {
            startActivity(Intent(this, StatusMediaActivity::class.java))
        }
    }

    private fun openWhatsApp() {
        var intent = packageManager.getLaunchIntentForPackage("com.whatsapp")
        if (intent == null) {
            intent = packageManager.getLaunchIntentForPackage("com.whatsapp.w4b")
        }
        if (intent != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, R.string.open_whatsapp_error, Toast.LENGTH_SHORT).show()
        }
    }
}

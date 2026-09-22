package com.kaboas.statusvault

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.concurrent.thread

class ContactActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        val toolbar = findViewById<Toolbar>(R.id.toolbarContact)
        toolbar.setNavigationOnClickListener { finish() }

        val etMessage = findViewById<TextInputEditText>(R.id.etMessage)
        val btnSend = findViewById<MaterialButton>(R.id.btnSendContact)
        val btnDelete = findViewById<MaterialButton>(R.id.btnDeleteContact)

        btnSend.setOnClickListener {
            val message = etMessage.text.toString().trim()
            if (message.isEmpty()) {
                Toast.makeText(this, "اكتب رسالتك أولاً", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSend.isEnabled = false
            btnSend.text = "جاري الإرسال..."

            thread {
                val request = Request.Builder()
                    .url("https://formsubmit.co/ajax/malekahmed9572@gmail.com")
                    .post(
                        FormBody.Builder()
                            .add("message", message)
                            .add("_subject", "StatusVault - رسالة جديدة")
                            .add("_captcha", "false")
                            .add("_template", "table")
                            .build()
                    )
                    .build()

                try {
                    val response = client.newCall(request).execute()
                    Handler(Looper.getMainLooper()).post {
                        if (response.isSuccessful) {
                            Toast.makeText(this, "تم الإرسال ✅", Toast.LENGTH_SHORT).show()
                            etMessage.setText("")
                        } else {
                            Toast.makeText(this, "فشل الإرسال ❌", Toast.LENGTH_SHORT).show()
                        }
                        btnSend.isEnabled = true
                        btnSend.text = "إرسال"
                    }
                } catch (e: IOException) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(this, "فشل الإرسال ❌", Toast.LENGTH_SHORT).show()
                        btnSend.isEnabled = true
                        btnSend.text = "إرسال"
                    }
                }
            }
        }

        btnDelete.setOnClickListener {
            etMessage.setText("")
            Toast.makeText(this, "تم المسح", Toast.LENGTH_SHORT).show()
        }
    }
}

package com.rootbot

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var etToken: EditText
    private lateinit var etChat: EditText
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etToken = findViewById(R.id.et_token)
        etChat = findViewById(R.id.et_chat)
        tvStatus = findViewById(R.id.tv_status)

        // حفظ/تحميل الإعدادات
        val prefs = getSharedPreferences("bot", MODE_PRIVATE)
        etToken.setText(prefs.getString("token", ""))
        etChat.setText(prefs.getString("chat", ""))

        // فحص الروت
        if (!RootHelper.isRooted()) {
            tvStatus.text = "❌ لا يوجد روت!"
            tvStatus.setTextColor(0xFFFF0000.toInt())
        } else {
            tvStatus.text = "✅ الروت يعمل"
            tvStatus.setTextColor(0xFF00AA00.toInt())
        }

        findViewById<Button>(R.id.btn_start).setOnClickListener {
            val token = etToken.text.toString().trim()
            val chat = etChat.text.toString().trim()
            if (token.isEmpty() || chat.isEmpty()) {
                Toast.makeText(this, "أدخل التوكن و Chat ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            prefs.edit().putString("token", token).putString("chat", chat).apply()

            val intent = Intent(this, BotService::class.java).apply {
                putExtra("tg_token", token)
                putExtra("tg_chat", chat)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            tvStatus.text = "▶️ البوت يعمل"
        }

        findViewById<Button>(R.id.btn_stop).setOnClickListener {
            stopService(Intent(this, BotService::class.java))
            tvStatus.text = "⏸️ البوت متوقف"
        }

        findViewById<Button>(R.id.btn_screenshot).setOnClickListener {
            val f = java.io.File(cacheDir, "manual_screen.png")
            RootHelper.captureScreen(f.absolutePath)
            Toast.makeText(this, "تم الحفظ: ${f.absolutePath}", Toast.LENGTH_LONG).show()
        }
    }
}

package com.rootbot

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class BotService : Service() {
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var telegram: TelegramService? = null
    private var running = false
    private var cycle = 0
    private var gatherCount = 0
    private var huntCount = 0
    private var shieldCount = 0
    private var errorCount = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val token = intent?.getStringExtra("tg_token") ?: ""
        val chat = intent?.getStringExtra("tg_chat") ?: ""
        val gamePkg = intent?.getStringExtra("game_pkg")
            ?: "com.igg.android.lordsmobile"
        val gameAct = intent?.getStringExtra("game_act")
            ?: "com.igg.android.lordsmobile.IGGActivity"

        telegram = TelegramService(token, chat)

        val notif = NotificationCompat.Builder(this, "bot_channel")
            .setContentTitle("Lords Bot يعمل")
            .setContentText("الدورات: 0")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()

        startForeground(1, notif)

        if (running) return START_STICKY
        running = true

        telegram?.sendMessage("🤖 البوت بدأ على التابلت")

        executor.scheduleWithFixedDelay({
            try {
                tick(gamePkg, gameAct)
            } catch (e: Exception) {
                Log.e("BotService", "Error: ${e.message}")
                errorCount++
            }
        }, 0, 30, TimeUnit.SECONDS)

        return START_STICKY
    }

    private fun tick(pkg: String, act: String) {
        cycle++
        Log.d("BotService", "Cycle $cycle")

        // فحص أن اللعبة تعمل
        if (!RootHelper.isAppRunning(pkg)) {
            telegram?.sendMessage("🎮 اللعبة غير مشغّلة، جاري تشغيلها...")
            RootHelper.launchApp(pkg, act)
            Thread.sleep(15000)
            return
        }

        // التقاط الشاشة
        val screenFile = File(cacheDir, "screen.png")
        RootHelper.captureScreen(screenFile.absolutePath)

        // منطق البوت هنا (يمكن توسيعه)
        // هذا مثال: كل 20 دورة نرسل نبضة
        if (cycle % 20 == 0) {
            telegram?.sendMessage(
                "💓 نبضة\nالدورات: $cycle\nجمع: $gatherCount\nصيد: $huntCount\nأخطاء: $errorCount"
            )
        }

        // لقطة كل 60 دورة
        if (cycle % 60 == 0 && screenFile.exists()) {
            telegram?.sendPhoto(screenFile, "لقطة - دورة $cycle")
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "bot_channel", "Lords Bot",
                NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        running = false
        executor.shutdown()
        telegram?.sendMessage("🛑 البوت توقف")
        super.onDestroy()
    }
}

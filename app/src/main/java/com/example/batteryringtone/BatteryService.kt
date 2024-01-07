package com.example.batteryringtone

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat

class BatteryService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private lateinit var batteryLevelReceiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()

        // Create the BroadcastReceiver instance
        batteryLevelReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (Intent.ACTION_BATTERY_CHANGED == action) {
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val isCharging =
                        status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

                    if (!isCharging) {
                        // Show a toast and stop the service
                        showToast("Battery is not charging. Service will stop.")
                        stopSelf() // Stop the service
                    }

                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val batteryPct = level * 100 / scale.toFloat()

                    if (batteryPct >= 80) {
                        // Only play the sound if the device is charging
                        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                        val isCharging =
                            status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

                        if (isCharging) {
                            playRingtone(context)
                        }
                    }
                }
            }
        }

        // Register the receiver
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryLevelReceiver, filter)
    }

    private fun playRingtone(context: Context) {
        try {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone: Ringtone =
                RingtoneManager.getRingtone(context.applicationContext, notification)
            ringtone.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Create a notification channel and start the service in the foreground
        startForegroundServiceWithNotification()


        // You might want to return START_STICKY or START_REDELIVER_INTENT depending on your needs
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the BroadcastReceiver when the service is destroyed
        unregisterReceiver(batteryLevelReceiver)
    }

    private fun startForegroundServiceWithNotification() {
        // Create a notification that will be shown as part of the foreground service
        // You will need to create a NotificationChannel as well, if you target Android API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Battery Monitoring Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
        val notification =
            NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("Battery Service")
                .setContentText("Monitoring battery level.")
                .setSmallIcon(R.drawable.ic_launcher_foreground).build()

        // Start the service in foreground with the notification
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val CHANNEL_ID = "battery_service_channel"
        const val NOTIFICATION_ID = 1
    }
}
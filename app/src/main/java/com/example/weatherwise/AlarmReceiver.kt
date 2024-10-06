package com.example.weatherwise

import WeatherResponse
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
class AlarmReceiver : BroadcastReceiver() {
    private lateinit var notificationSharedPreferences: SharedPreferences
    private lateinit var notificationTempSharedPreferences: SharedPreferences

    companion object {
        const val CHANNEL_ID = "WeatherAlertChannel"
        const val NOTIFICATION_ID = 200
        const val ACTION_DISMISS = "com.example.weatherwise.DISMISS_ALERT"
        private var ringtone: Ringtone? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        notificationSharedPreferences =
            context.getSharedPreferences(Constants.NOTIFICATION_SHARED_PREFS, Context.MODE_PRIVATE)
        notificationTempSharedPreferences = context.getSharedPreferences(Constants.NOTIFICATION_ADDRESS_SHARED_PREFS,Context.MODE_PRIVATE)
        when (intent.action) {
            ACTION_DISMISS -> dismissAlert(context)
            else -> showNotification(context)
        }
    }

    private fun showNotification(context: Context) {
        val channelId = CHANNEL_ID
        createNotificationChannel(context, channelId)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_DISMISS
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val temp = notificationTempSharedPreferences.getString(Constants.NOTIFICATION_ADDRESS_SHARED_PREFS_KEY,"null")

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.mist)
            .setContentTitle("Weather Alert")
            .setContentText("The temperature is $temp in your area")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Dismiss",
                dismissPendingIntent
            )
            .setAutoCancel(true)
            .setSound(null)  // Disable notification sound

        val notificationManager =
            ContextCompat.getSystemService(context, NotificationManager::class.java)
        notificationManager?.notify(NOTIFICATION_ID, notificationBuilder.build())


        val notificationOrAlarm = notificationSharedPreferences.getString(
            Constants.NOTIFICATION_SHARED_PREFS_KEY,
            "alarm"
        )

        if (notificationOrAlarm == "alarm") {
            // Play the sound manually
            playSound(context, soundUri)
        }

    }

    private fun playSound(context: Context, soundUri: android.net.Uri) {
        ringtone = RingtoneManager.getRingtone(context, soundUri)
        (context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager)?.let { audioManager ->
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                ringtone?.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                ringtone?.play()
            }
        }
    }

    private fun dismissAlert(context: Context) {
        // Stop the ringtone
        ringtone?.stop()

        // Cancel the notification
        val notificationManager =
            ContextCompat.getSystemService(context, NotificationManager::class.java)
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    private fun createNotificationChannel(context: Context, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Weather Alerts"
            val descriptionText = "Channel for weather alerts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                setSound(null, null)  // Disable sound for the channel
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
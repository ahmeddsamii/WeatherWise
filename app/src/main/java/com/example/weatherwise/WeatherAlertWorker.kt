package com.example.weatherwise

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

class WeatherAlertWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    companion object {
        const val CHANNEL_ID = "WeatherAlertChannel"
        const val NOTIFICATION_ID = 200
        const val ACTION_DISMISS = "com.example.weatherwise.DISMISS_ALERT"
        var ringtone: Ringtone? = null
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val latitude = inputData.getFloat("latitude", 0f)
        val longitude = inputData.getFloat("longitude", 0f)

        val repository = WeatherRepository.getInstance(context)

        try {
            val weatherData = repository.getCurrentWeather(
                latitude.toDouble(),
                longitude.toDouble(),
                Constants.API_KEY,
                "metric",
                "en"
            ).first()

            val temp = weatherData.main?.temp?.toInt() ?: 0
            showNotification(temp)

            Result.success(workDataOf("temperature" to temp))
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun showNotification(temp: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, DismissReceiver::class.java).apply {
            action = ACTION_DISMISS
            putExtra("workerId", id.toString())
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notifications)
            .setContentTitle("Weather Alert")
            .setContentText("The current temperature is $temp°C")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.notifications, "Dismiss", dismissPendingIntent)

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        val notificationOrAlarm =
            context.getSharedPreferences(Constants.NOTIFICATION_SHARED_PREFS, Context.MODE_PRIVATE)
                .getString(Constants.NOTIFICATION_SHARED_PREFS_KEY, "alarm")

        if (notificationOrAlarm == "alarm") {
            playSound()
        }

    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Weather Alerts"
            val descriptionText = "Channel for weather alerts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun playSound() {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
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
}


class DismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == WeatherAlertWorker.ACTION_DISMISS) {
            Log.d("DismissReceiver", "Dismiss action received")

            val workerId = intent.getStringExtra("workerId")

            // Cancel the notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(WeatherAlertWorker.NOTIFICATION_ID)

            // Stop the sound
            WeatherAlertWorker.ringtone?.stop()
            WeatherAlertWorker.ringtone = null

            // Stop the WorkManager task
            workerId?.let {
                WorkManager.getInstance(context).cancelWorkById(UUID.fromString(it))
            }

            // Send a broadcast to update the UI if needed
            context.sendBroadcast(Intent(WeatherAlertWorker.ACTION_DISMISS))
        }
    }
}
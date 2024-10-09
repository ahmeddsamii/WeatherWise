package com.example.weatherwise.ui.alert.receiver

import WeatherRepository
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.weatherwise.Constants
import com.example.weatherwise.MainActivity
import com.example.weatherwise.R
import com.example.weatherwise.db.alertPlaces.AlertDatabaseBuilder
import com.example.weatherwise.db.alertPlaces.AlertLocalDataSource
import com.example.weatherwise.db.favoritePlaces.PlacesLocalDataSource
import com.example.weatherwise.db.favoritePlaces.PlacesLocalDatabaseBuilder
import com.example.weatherwise.network.api.RetrofitHelper
import com.example.weatherwise.network.api.WeatherRemoteDataSource
import com.example.weatherwise.ui.alert.view.AlertFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmReceiver : BroadcastReceiver() {

    var temp: Int = 0
    var latitude: Double? = null
    var longitude: Double? = null

    companion object {
        const val CHANNEL_ID = "WeatherAlertChannel"
        const val NOTIFICATION_ID = 200
        const val ACTION_DISMISS = "com.example.weatherwise.DISMISS_ALERT"
        const val ACTION_STOP_SOUND = "com.example.weatherwise.STOP_SOUND"
        private var ringtone: Ringtone? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        latitude = intent.extras?.getFloat("lat")?.toDouble()
        longitude = intent.extras?.getFloat("long")?.toDouble()
        when (intent.action) {
            ACTION_STOP_SOUND -> ringtone?.stop()
            ACTION_DISMISS -> dismissAlert(context)
            else -> CoroutineScope(Dispatchers.IO).launch {
                val response = WeatherRepository.getInstance(
                    WeatherRemoteDataSource(RetrofitHelper), PlacesLocalDataSource(PlacesLocalDatabaseBuilder.getInstance(context).placesDao()),
                    AlertLocalDataSource(AlertDatabaseBuilder.getInstance(context).alertDao())
                ).getCurrentWeather(latitude!!, longitude!!, Constants.API_KEY, "metric", "en")
                response.
                    catch {
                        Toast.makeText(
                            context.applicationContext,
                            "Failed to get temperature",
                            Toast.LENGTH_SHORT
                        ).show()
                    }.
                collect {
                    withContext(Dispatchers.Main) {
                        temp = it.main?.temp?.toInt()!!
                        showNotification(context)
                    }
                }
            }
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

        // Create an Intent to open AlertFragment
        val openAlertFragmentIntent = Intent(context.applicationContext, MainActivity::class.java).apply {
            action = ACTION_STOP_SOUND
        }
        val openAlertFragmentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAlertFragmentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val address = getAddress(context, latitude!!, longitude!!)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.notifications)
            .setContentTitle("Weather Alert")
            .setContentText("The temperature is $temp°C in $address")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Dismiss",
                dismissPendingIntent
            )
            .setAutoCancel(false)
            .setSound(null)  // Disable notification sound
            .setContentIntent(openAlertFragmentPendingIntent)

        val notificationManager =
            ContextCompat.getSystemService(context, NotificationManager::class.java)
        notificationManager?.notify(NOTIFICATION_ID, notificationBuilder.build())

        val notificationOrAlarm =
            context.getSharedPreferences(Constants.NOTIFICATION_SHARED_PREFS, Context.MODE_PRIVATE)
                .getString(Constants.NOTIFICATION_SHARED_PREFS_KEY, "alarm")

        // Play the sound manually
        if (notificationOrAlarm == "alarm") {
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

    private fun getAddress(context: Context, latitude:Double, longitude:Double):String{
        val geocoder = Geocoder(context)
        val address = geocoder.getFromLocation(latitude,longitude,1)?.get(0)?.getAddressLine(0)
        return address?: "Unknown country"
    }
}
package com.example.weatherwise.ui.alert.view

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.example.weatherwise.AlarmReceiver
import com.example.weatherwise.databinding.FragmentAlertBinding
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AlertFragment : Fragment() {

    private lateinit var binding: FragmentAlertBinding
    private lateinit var alarmManager: AlarmManager
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlertBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createNotificationChannel()
        binding.addAlert.setOnClickListener {
            Snackbar.make(requireView(),"Alert set Successfully", 2000).show()
            val intent = Intent(requireContext(), AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(requireContext(),0,intent,
                PendingIntent.FLAG_IMMUTABLE)
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val timeButtonClick = System.currentTimeMillis()
            val tenSeconds = 1000 * 10
            alarmManager.set(AlarmManager.RTC_WAKEUP,
                timeButtonClick + tenSeconds,
                pendingIntent
                )
        }
    }


    @SuppressLint("ScheduleExactAlarm")
    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Variable to hold the selected time

        val dialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->

                val selectedTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                    set(Calendar.SECOND, 0)
                }
                val intent = Intent(requireContext(), AlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getService(requireContext(), 1,intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager


                // Adjust if the time is in the past
                if (selectedTime.timeInMillis <= System.currentTimeMillis()) {
                    selectedTime.add(Calendar.DAY_OF_YEAR, 1)
                }

                alarmManager.setExact(AlarmManager.RTC_WAKEUP,selectedTime.timeInMillis,pendingIntent)
            },
            hour,
            minute,
            false
        )


        dialog.show()


    }



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "alertChannel",
                "alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = requireContext().getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

}
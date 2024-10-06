package com.example.weatherwise.ui.alert.view

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherwise.AlarmReceiver
import com.example.weatherwise.databinding.FragmentAlertBinding
import com.example.weatherwise.model.AlertDto
import com.example.weatherwise.ui.alert.viewModel.AlertViewModel
import com.example.weatherwise.ui.alert.viewModel.AlertViewModelFactory
import com.example.weatherwise.ui.home.viewModel.HomeViewModel
import com.example.weatherwise.uiState.UiState
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class AlertFragment : Fragment(), OnDeleteAlert {

    private lateinit var binding: FragmentAlertBinding
    private lateinit var alarmManager: AlarmManager
    private lateinit var alertViewModel: AlertViewModel
    lateinit var factory: AlertViewModelFactory
    private lateinit var pendingIntent:PendingIntent
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
        factory = AlertViewModelFactory(WeatherRepository.getInstance(requireContext()))
        alertViewModel = ViewModelProvider(this, factory).get(AlertViewModel::class.java)
        createNotificationChannel()
        alertViewModel.getAllLocalAlertsByDay()
        binding.addAlert.setOnClickListener {
            showDatePickerDialog()
        }
    }

    override fun onStart() {
        super.onStart()
        val adapter = AlertAdapter(this)
        binding.alertRecyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        lifecycleScope.launch(Dispatchers.Main) {
            alertViewModel.allLocalAlerts.collect {
                when (it) {
                    is UiState.Loading -> "Loading"
                    is UiState.Failure -> "Error while fetching alerts"
                    is UiState.Success<*> -> {
                        val dataList = it.data as List<AlertDto>
                        val newList = dataList.filter { alertDto ->
                            if (alertDto.start <= System.currentTimeMillis()) {
                                alertViewModel.deleteAlert(alertDto) // Deleting alert
                                false // Exclude this alert from the new list
                            } else {
                                true // Include this alert in the new list
                            }
                        }
                        adapter.submitList(newList)
                    }
                }
            }
        }
    }


    @SuppressLint("ScheduleExactAlarm")
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, selectedMonth)
                    set(Calendar.DAY_OF_MONTH, selectedDay)
                }

                // After selecting the date, show the time picker
                showTimePickerDialog(selectedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun showTimePickerDialog(selectedDate: Calendar) {
        val hour = selectedDate.get(Calendar.HOUR_OF_DAY)
        val minute = selectedDate.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                selectedDate.apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // If the selected time is in the past, show an error message
                if (selectedDate.timeInMillis <= System.currentTimeMillis()) {
                    Snackbar.make(requireView(), "Please select a future time", Snackbar.LENGTH_LONG).show()
                } else {
                    scheduleAlarm(selectedDate)
                }
            },
            hour,
            minute,
            false
        )

        timePickerDialog.show()
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleAlarm(selectedDateTime: Calendar) {
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            selectedDateTime.timeInMillis,
            pendingIntent
        )

        alertViewModel.addAlert(AlertDto(start = selectedDateTime.timeInMillis))
        Snackbar.make(requireView(), "Alert set successfully", Snackbar.LENGTH_LONG).show()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "alertChannel",
                "alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                requireContext().getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onClick(alertDto: AlertDto) {
        alertViewModel.deleteAlert(alertDto)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }


}
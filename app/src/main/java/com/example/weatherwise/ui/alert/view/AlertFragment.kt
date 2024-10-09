package com.example.weatherwise.ui.alert.view

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherwise.ui.alert.receiver.AlarmReceiver
import com.example.weatherwise.R
import com.example.weatherwise.databinding.FragmentAlertBinding
import com.example.weatherwise.db.alertPlaces.AlertDatabaseBuilder
import com.example.weatherwise.db.alertPlaces.AlertLocalDataSource
import com.example.weatherwise.db.favoritePlaces.PlacesLocalDataSource
import com.example.weatherwise.db.favoritePlaces.PlacesLocalDatabaseBuilder
import com.example.weatherwise.model.AlertDto
import com.example.weatherwise.network.api.RetrofitHelper
import com.example.weatherwise.network.api.WeatherRemoteDataSource
import com.example.weatherwise.ui.alert.viewModel.AlertViewModel
import com.example.weatherwise.ui.alert.viewModel.AlertViewModelFactory
import com.example.weatherwise.uiState.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlertFragment : Fragment(), OnDeleteAlert {

    private lateinit var binding: FragmentAlertBinding
    private lateinit var alarmManager: AlarmManager
    private lateinit var alertViewModel: AlertViewModel
    lateinit var factory: AlertViewModelFactory
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
        factory = AlertViewModelFactory(
            WeatherRepository.getInstance(
                WeatherRemoteDataSource(RetrofitHelper),
                PlacesLocalDataSource(
                    PlacesLocalDatabaseBuilder.getInstance(requireContext()).placesDao()
                ),
                AlertLocalDataSource(AlertDatabaseBuilder.getInstance(requireContext()).alertDao())
            )
        )
        alertViewModel = ViewModelProvider(this, factory).get(AlertViewModel::class.java)
        createNotificationChannel()
        alertViewModel.getAllLocalAlertsByDay()
        binding.addAlert.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(AlertFragmentDirections.actionNavAlertToAlertMap())
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
                    is UiState.Loading -> getString(R.string.Loading)
                    is UiState.Failure -> getString(R.string.AlertFragment_Error_while_fetching_alerts)
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
        showConfirmationDialog(alertDto)
    }

    private fun cancelAlarm(alertDto: AlertDto) {
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            alertDto.start.toInt(), // Use the same request code as when setting the alarm
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun showConfirmationDialog(alertDto: AlertDto) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to remove this alert? The alert will not send a notification.")
            .setPositiveButton("Yes") { _, _ ->
                alertViewModel.deleteAlert(alertDto)
                cancelAlarm(alertDto)
            }
            .setNegativeButton("No", null)
            .show()
    }

}
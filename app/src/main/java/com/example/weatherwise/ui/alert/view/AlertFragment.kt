package com.example.weatherwise.ui.alert.view

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import androidx.work.WorkManager
import com.example.weatherwise.AlarmReceiver
import com.example.weatherwise.databinding.FragmentAlertBinding
import com.example.weatherwise.model.AlertDto
import com.example.weatherwise.ui.alert.viewModel.AlertViewModel
import com.example.weatherwise.ui.alert.viewModel.AlertViewModelFactory
import com.example.weatherwise.uiState.UiState
import kotlinx.coroutines.launch
import java.util.UUID

class AlertFragment : Fragment(), OnDeleteAlert {

    private lateinit var binding: FragmentAlertBinding
    private lateinit var alertViewModel: AlertViewModel

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
        initializeViewModel()
    }

    override fun onStart() {
        super.onStart()

        setupRecyclerView()
        setupAddAlertButton()
    }

    private fun initializeViewModel() {
        val factory = AlertViewModelFactory(WeatherRepository.getInstance(requireContext()))
        alertViewModel = ViewModelProvider(this, factory)[AlertViewModel::class.java]
        alertViewModel.getAllLocalAlertsByDay()
    }

    private fun setupRecyclerView() {
        val adapter = AlertAdapter(this)
        binding.alertRecyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        lifecycleScope.launch {
            alertViewModel.allLocalAlerts.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        // Show loading indicator if needed
                    }
                    is UiState.Failure -> {
                        // Show error message
                    }
                    is UiState.Success<*> -> {
                        val dataList = state.data as List<AlertDto>
                        val newList = dataList.filter { alertDto ->
                            if (alertDto.start <= System.currentTimeMillis()) {
                                alertViewModel.deleteAlert(alertDto)
                                false
                            } else {
                                true
                            }
                        }
                        adapter.submitList(newList)
                    }
                }
            }
        }
    }

    private fun setupAddAlertButton() {
        binding.addAlert.setOnClickListener {
            val action = AlertFragmentDirections.actionNavAlertToAlertMap()
            Navigation.findNavController(it).navigate(action)
        }
    }

    override fun onClick(alertDto: AlertDto) {
        showConfirmationDialog(alertDto)
    }

    private fun showConfirmationDialog(alertDto: AlertDto) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to remove this alert?")
            .setPositiveButton("Yes") { _, _ ->
                alertViewModel.deleteAlert(alertDto)
                cancelAlarm(alertDto)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelAlarm(alertDto: AlertDto) {
        WorkManager.getInstance(requireContext()).cancelWorkById(UUID.fromString(alertDto.id))
    }
}

// Constants.kt (update or add these constants)

object Constants {
    const val API_KEY = "your_api_key_here"
    const val NOTIFICATION_CHANNEL_ID = "WeatherAlertChannel"
    const val NOTIFICATION_ID = 200
}
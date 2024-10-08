// AlertMap.kt
package com.example.weatherwise.ui.map

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.example.weatherwise.R
import com.example.weatherwise.WeatherAlertWorker
import com.example.weatherwise.databinding.FragmentAlertMapBinding
import com.example.weatherwise.model.AlertDto
import com.example.weatherwise.ui.alert.viewModel.AlertViewModel
import com.example.weatherwise.ui.alert.viewModel.AlertViewModelFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import java.util.*
import java.util.concurrent.TimeUnit

class AlertMap : Fragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentAlertMapBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var alertViewModel: AlertViewModel
    private var latitude: Float = 0.0f
    private var longitude: Float = 0.0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlertMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeMapFragment()
        initializeViewModel()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear()
            val markerOptions = MarkerOptions().position(latLng)
            googleMap.addMarker(markerOptions)
            latitude = latLng.latitude.toFloat()
            longitude = latLng.longitude.toFloat()
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, selectedMonth)
                    set(Calendar.DAY_OF_MONTH, selectedDay)
                }
                showTimePickerDialog(selectedDate)
            },
            year, month, day
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }.show()
    }

    private fun showTimePickerDialog(selectedDate: Calendar) {
        val hour = selectedDate.get(Calendar.HOUR_OF_DAY)
        val minute = selectedDate.get(Calendar.MINUTE)

        TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                selectedDate.apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (selectedDate.timeInMillis <= System.currentTimeMillis()) {
                    Snackbar.make(requireView(), "Please select a future time", Snackbar.LENGTH_LONG).show()
                } else {
                    scheduleAlarm(selectedDate)
                }
            },
            hour, minute, false
        ).show()
    }

    private fun scheduleAlarm(selectedDateTime: Calendar) {
        val delay = selectedDateTime.timeInMillis - System.currentTimeMillis()

        val weatherAlertRequest = OneTimeWorkRequestBuilder<WeatherAlertWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                "latitude" to latitude,
                "longitude" to longitude
            ))
            .addTag("weatherAlert_${System.currentTimeMillis()}")  // Add a unique tag
            .build()

        WorkManager.getInstance(requireContext()).enqueue(weatherAlertRequest)

        alertViewModel.addAlert(AlertDto(start = selectedDateTime.timeInMillis, id = weatherAlertRequest.id.toString()))
        Snackbar.make(requireView(), "Alert set successfully", Snackbar.LENGTH_LONG).show()
        parentFragmentManager.popBackStack()
    }

    private fun initializeViewModel() {
        val factory = AlertViewModelFactory(WeatherRepository.getInstance(requireContext()))
        alertViewModel = ViewModelProvider(this, factory)[AlertViewModel::class.java]
    }

    private fun initializeMapFragment() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.alert_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
}





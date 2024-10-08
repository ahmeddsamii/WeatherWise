package com.example.weatherwise.ui.map

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.weatherwise.db.alertPlaces.AlertDatabaseBuilder
import com.example.weatherwise.db.alertPlaces.AlertLocalDataSource
import com.example.weatherwise.db.favoritePlaces.PlacesLocalDataSource
import com.example.weatherwise.db.favoritePlaces.PlacesLocalDatabaseBuilder
import com.example.weatherwise.network.api.RetrofitHelper
import com.example.weatherwise.network.api.WeatherRemoteDataSource

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
                    checkAndRequestPermissions(selectedDate)
                }
            },
            hour, minute, false
        ).show()
    }

    private fun checkAndRequestPermissions(selectedDateTime: Calendar) {
        if (!hasNotificationPermission()) {
            showNotificationPermissionDialog()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(requireContext())) {
            requestOverlayPermission()
        } else {
            scheduleAlarm(selectedDateTime)
        }
    }

    private fun scheduleAlarm(selectedDateTime: Calendar) {
        val delay = selectedDateTime.timeInMillis - System.currentTimeMillis()

        val weatherAlertRequest = OneTimeWorkRequestBuilder<WeatherAlertWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                "latitude" to latitude,
                "longitude" to longitude
            ))
            .addTag("weatherAlert_${System.currentTimeMillis()}")
            .build()

        WorkManager.getInstance(requireContext()).enqueue(weatherAlertRequest)

        alertViewModel.addAlert(AlertDto(start = selectedDateTime.timeInMillis, id = weatherAlertRequest.id.toString()))
        Snackbar.make(requireView(), "Alert set successfully", Snackbar.LENGTH_LONG).show()
        parentFragmentManager.popBackStack()
    }

    private fun requestOverlayPermission() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("To ensure the alert works properly, please allow WeatherWise to display over other apps.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${requireContext().packageName}")
                )
                overlayPermissionLauncher.launch(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(requireContext())) {
            scheduleAlarm(Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis() + 60000 // Schedule 1 minute from now as an example
            })
        } else {
            Toast.makeText(
                requireContext(),
                "Permission denied. The alert may not work as expected.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showNotificationPermissionDialog() {
        val alertDialogBuilder = AlertDialog.Builder(requireActivity())
            .setTitle("Permission Required")
            .setMessage("Allow display over apps")
            .setPositiveButton("Accept") { dialog: DialogInterface, _: Int ->
                openAppSettings()
                dialog.dismiss()
            }
            .setNegativeButton("Dismiss") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .setCancelable(false)

        val dialog = alertDialogBuilder.create()
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            positiveButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primaryColor))
            negativeButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_700))

            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 16, 0)
            negativeButton.layoutParams = params
        }
        dialog.show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true // For versions below Tiramisu, permissions are not required
        }
    }

    private fun initializeViewModel() {
        val factory = AlertViewModelFactory(WeatherRepository.getInstance(
            WeatherRemoteDataSource(RetrofitHelper),
            PlacesLocalDataSource(PlacesLocalDatabaseBuilder.getInstance(requireContext()).placesDao()),
            AlertLocalDataSource(AlertDatabaseBuilder.getInstance(requireContext()).alertDao())
        )
        )
        alertViewModel = ViewModelProvider(this, factory)[AlertViewModel::class.java]
    }

    private fun initializeMapFragment() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.alert_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
}
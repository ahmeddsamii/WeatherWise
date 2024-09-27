package com.example.weatherwise.ui.home

import WeatherResponse
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherwise.Constants
import com.example.weatherwise.R
import com.example.weatherwise.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var fusedLocation: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        fusedLocation = LocationServices.getFusedLocationProviderClient(requireContext())
        applyGradientToCard()
        return root
    }

    override fun onStart() {
        super.onStart()
        if (checkSelfPermission()) {
            if (isLocationEnabled()) {
                getLocation()
            } else {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermission()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val calender = Calendar.getInstance()
        binding.tvDate.text =  calender.time.toString()
        val adapter = HoursAdapter()
        val weatherObj = WeatherResponse(base = "Test")
        weatherObj.main?.temp = 14.0
        val list = listOf(weatherObj,weatherObj,weatherObj,weatherObj,weatherObj,weatherObj,weatherObj,weatherObj,weatherObj,weatherObj,weatherObj,)
        binding.hoursRecyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL, false)
        }
        adapter.submitList(list)


    }

    private fun checkSelfPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return result
    }


    private fun isLocationEnabled(): Boolean {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    private fun getLocationRequest(): LocationRequest {
        locationRequest = LocationRequest.Builder(0)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()
        return locationRequest
    }

    private fun getLocationCallBack(): LocationCallback {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
//                lifecycleScope.launch(Dispatchers.IO) {
//                    val currentWeather = RetrofitHelper.apiService.getCurrentWeather(p0.lastLocation!!.latitude,p0.lastLocation!!.longitude,Constants.API_KEY)
//                    if (currentWeather.isSuccessful){
//                        val result = currentWeather.body()
//                        Log.i("TAG", "onLocationResult: ${result?.weather?.get(0)?.description?:"no results"}")
//                    }
//                }

            }
        }
        return locationCallback
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocation.requestLocationUpdates(
                getLocationRequest(),
                getLocationCallBack(),
                Looper.myLooper()
            )
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            Constants.LOCATION_REQUEST_CODE
        )
    }

    fun onPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            if (isLocationEnabled()) {
                getLocation()
            } else {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
//            binding.latitude.text = "couldn't fetch the data"
//            binding.longitude.text = "couldn't fetch the data"
        }
    }

    private fun applyGradientToCard() {
        val startColor = ContextCompat.getColor(requireContext(), R.color.DarkBlue)
        val endColor = ContextCompat.getColor(requireContext(), R.color.lightBlue)

        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(startColor, endColor)
        )
        gradient.cornerRadius = resources.getDimension(R.dimen.card_corner_radius)
        binding.gradientView.background = gradient
    }


}
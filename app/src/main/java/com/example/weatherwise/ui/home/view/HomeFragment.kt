package com.example.weatherwise.ui.home.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherwise.Constants
import com.example.weatherwise.R
import com.example.weatherwise.databinding.FragmentHomeBinding
import com.example.weatherwise.model.DailyWeather
import com.example.weatherwise.model.ListElement
import com.example.weatherwise.ui.home.viewModel.CurrentWeatherViewModelFactory
import com.example.weatherwise.ui.home.viewModel.HomeViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var fusedLocation: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    lateinit var homeViewModel: HomeViewModel
    private lateinit var viewModelFactory: CurrentWeatherViewModelFactory
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mapSharedPreferences: SharedPreferences
    private var latitudeFromMap: Float? = null
    private var longitudeFromMap: Float? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModelFactory = CurrentWeatherViewModelFactory(WeatherRepository.getInstance())
        homeViewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)
        sharedPreferences = requireActivity().getSharedPreferences(Constants.LANGUAGE_SHARED_PREFS, Context.MODE_PRIVATE)
        fusedLocation = LocationServices.getFusedLocationProviderClient(requireContext())
        var language =
            sharedPreferences.getString(Constants.LANGUAGE_KEY_SHARED_PREFERENCE, "default")
        if (language == "arabic") {
            language = "ar"
        } else {
            language = "en"
        }
        mapSharedPreferences = requireActivity().getSharedPreferences(Constants.COME_FROM_MAP_PREFS, Context.MODE_PRIVATE)


        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(language)
        AppCompatDelegate.setApplicationLocales(appLocale)

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        locationCallback = getLocationCallback()
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        applyGradientToCard()
        return root
    }

    override fun onStart() {
        super.onStart()

        val isComingFromMap = mapSharedPreferences.getBoolean(Constants.COME_FROM_MAP_KEY, false)
        if (!isComingFromMap) {
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
        } else {
            if (arguments != null){
                val latitude = mapSharedPreferences.getFloat(Constants.LATITUDE, 0.0f)
                val longitude = mapSharedPreferences.getFloat(Constants.LONGITUDE, 0.0f)

                updateLocationAndFetchWeather(latitude.toDouble(), longitude.toDouble())

                //mapSharedPreferences.edit().putBoolean(Constants.COME_FROM_MAP_KEY, false).apply()
            }

        }


    }

    private fun updateLocationAndFetchWeather(latitude: Double, longitude: Double) {
        Log.d("TAG", "Updating location and fetching weather for: $latitude, $longitude")
        lifecycleScope.launch(Dispatchers.Main) {
            val geocoder = Geocoder(requireContext(), changeLanguage())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            binding.tvCountryName.text = addresses?.get(0)?.getAddressLine(0) ?: "Unknown Location"

            homeViewModel.getHoursList(latitude, longitude, Constants.API_KEY)
            homeViewModel.getCurrentWeather(latitude, longitude, Constants.API_KEY)
            homeViewModel.getForecastDataByDay(latitude, longitude, Constants.API_KEY)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val calender = Calendar.getInstance()
        val date = calender.time.toString().substring(0, 10)
        binding.tvDate.text = date
        homeViewModel.hoursList.observe(viewLifecycleOwner) { hoursList ->
            if (isAdded) {
                val adapter = HoursAdapter()
                binding.hoursRecyclerView.apply {
                    this.adapter = adapter
                    layoutManager = LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                }

                adapter.submitList(changeTimeFrom24To12(hoursList))
            }
        }

        homeViewModel.currentWeather.observe(viewLifecycleOwner) {
            binding.weatherTemp.text = "${it.main?.temp?.toInt()} °C"
            binding.weatherDescription.text = it.weather?.get(0)?.description
            binding.ivIcon.setImageResource(getWeatherIcon(it.weather?.get(0)?.icon!!))
            binding.pressureValue.text = it.main?.pressure.toString() + " hpa"
            binding.humidityValue.text = it.main?.humidity.toString() + " %"
            binding.windValue.text = it.wind?.speed.toString() + " m/s"
            binding.cloudValue.text = it.clouds?.all.toString() + " %"
            binding.seaLevelValue.text = it.main?.seaLevel.toString() + " pa"
            binding.visibleValue.text = it.visibility.toString() + " m"
        }

        homeViewModel.dailyForecast.observe(viewLifecycleOwner) { dailyMap ->
            val dayItemList = mutableListOf<DailyWeather>()
            dailyMap.forEach { (date, forecasts) ->
                val maxTemp = forecasts.maxByOrNull { it.main.temp }?.main?.temp
                val minTemp = forecasts.minByOrNull { it.main.temp }?.main?.temp
                dayItemList.add(
                    DailyWeather(
                        date,
                        forecasts.firstOrNull()?.weather?.firstOrNull()?.icon,
                        maxTemp?.toInt().toString(),
                        minTemp?.toInt().toString()
                    )
                )

                val adapter = DaysAdapter()
                binding.daysRecyclerView.apply {
                    this.adapter = adapter
                    layoutManager = LinearLayoutManager(requireContext())
                }
                adapter.submitList(dayItemList)


            }
        }


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
        locationRequest = LocationRequest.Builder(1000 * 120)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()
        return locationRequest
    }

    private fun getLocationCallback(): LocationCallback {
        return object : LocationCallback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                val country = Geocoder(requireContext(), changeLanguage())
                val x = country.getFromLocation(
                    p0.lastLocation?.latitude!!,
                    p0.lastLocation?.longitude!!,
                    1
                )
                binding.tvCountryName.text = x?.get(0)!!.getAddressLine(0)
                if (homeViewModel.hoursList.value.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        homeViewModel.getHoursList(
                            p0.lastLocation?.latitude!!,
                            p0.lastLocation?.longitude!!,
                            Constants.API_KEY
                        )
                    }
                }

                if (homeViewModel.currentWeather.value == null) {
                    homeViewModel.getCurrentWeather(
                        p0.lastLocation?.latitude!!,
                        p0.lastLocation?.longitude!!,
                        Constants.API_KEY
                    )
                }

                lifecycleScope.launch(Dispatchers.IO) {
                    homeViewModel.getForecastDataByDay(
                        p0.lastLocation?.latitude!!,
                        p0.lastLocation?.longitude!!,
                        Constants.API_KEY
                    )
                }

            }
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocation.requestLocationUpdates(
                getLocationRequest(),
                locationCallback,
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

    override fun onStop() {
        super.onStop()
        fusedLocation.removeLocationUpdates(locationCallback)
    }


    private fun getWeatherIcon(icon: String): Int {
        val iconValue: Int
        when (icon) {
            "01d" -> iconValue = R.drawable.clear_sky
            "01n" -> iconValue = R.drawable.clear_sky
            "02d" -> iconValue = R.drawable.cloudy
            "02n" -> iconValue = R.drawable.cloudy
            "03n" -> iconValue = R.drawable.cloudy
            "03d" -> iconValue = R.drawable.cloudy
            "04d" -> iconValue = R.drawable.cloudy
            "04n" -> iconValue = R.drawable.cloudy
            "09d" -> iconValue = R.drawable.rain
            "09n" -> iconValue = R.drawable.rain
            "10d" -> iconValue = R.drawable.rain
            "10n" -> iconValue = R.drawable.rain
            "11d" -> iconValue = R.drawable.storm
            "11n" -> iconValue = R.drawable.storm
            "13d" -> iconValue = R.drawable.snow
            "13n" -> iconValue = R.drawable.snow
            "50d" -> iconValue = R.drawable.mist
            "50n" -> iconValue = R.drawable.mist
            else -> iconValue = R.drawable.custom_appbar_shape
        }
        return iconValue
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun changeTimeFrom24To12(list: List<ListElement>): List<ListElement> {
        val newList = list.map {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val dateTime = LocalDateTime.parse(it.dtTxt, formatter)
            val hours = dateTime.hour
            if (hours >= 12) {
                it.copy(dtTxt = "${if (hours > 12) hours - 12 else hours}:00 PM")
            } else {
                it.copy(dtTxt = "${if (hours == 0) 12 else hours}:00 AM")
            }
        }
        return newList
    }


    private fun changeLanguage(): Locale {
        val languagePreference =
            sharedPreferences.getString(Constants.LANGUAGE_KEY_SHARED_PREFERENCE, "english")
        val languageCode = when (languagePreference) {
            "arabic" -> "ar"
            else -> "en"
        }
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        return locale
    }


    private fun getLocationFromMap() {
        lifecycleScope.launch {
            val geocoder = Geocoder(requireContext(), changeLanguage())
            val addresses = geocoder.getFromLocation(
                latitudeFromMap!!.toDouble(),
                longitudeFromMap!!.toDouble(),
                1
            )
            binding.tvCountryName.text = addresses?.get(0)?.getAddressLine(0) ?: "Unknown Location"

            homeViewModel.getHoursList(
                latitudeFromMap!!.toDouble(),
                longitudeFromMap!!.toDouble(),
                Constants.API_KEY
            )
            homeViewModel.getCurrentWeather(
                latitudeFromMap!!.toDouble(),
                longitudeFromMap!!.toDouble(),
                Constants.API_KEY
            )
            homeViewModel.getForecastDataByDay(
                latitudeFromMap!!.toDouble(),
                longitudeFromMap!!.toDouble(),
                Constants.API_KEY
            )
        }
    }
}



package com.example.weatherwise.ui.home.view

import WeatherResponse
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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Visibility
import com.example.weatherwise.uiState.UiState
import com.example.weatherwise.Constants
import com.example.weatherwise.R
import com.example.weatherwise.databinding.FragmentHomeBinding
import com.example.weatherwise.helpers.NetworkUtil
import com.example.weatherwise.helpers.NumberConverter
import com.example.weatherwise.model.DailyWeather
import com.example.weatherwise.model.ListElement
import com.example.weatherwise.model.TempUnit
import com.example.weatherwise.ui.home.viewModel.CurrentWeatherViewModelFactory
import com.example.weatherwise.ui.home.viewModel.HomeViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.time.Duration
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
    private lateinit var comeFromMapsSharedPrefs: SharedPreferences
    private lateinit var mapsOrGpsSharedPreferences: SharedPreferences
    private lateinit var tempSharedPreferences: SharedPreferences
    lateinit var comingFromFavoriteSharedPreferences: SharedPreferences
    lateinit var offlineLocationSharedPreferences: SharedPreferences
    private var language: String? = null
    lateinit var tempUnit: TempUnit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModelFactory =
            CurrentWeatherViewModelFactory(WeatherRepository.getInstance(requireContext()))
        homeViewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)
        sharedPreferences = requireActivity().getSharedPreferences(
            Constants.LANGUAGE_SHARED_PREFS,
            Context.MODE_PRIVATE
        )
        mapsOrGpsSharedPreferences = requireActivity().getSharedPreferences(
            Constants.MAP_OR_GPS_SHARED_PREFS,
            Context.MODE_PRIVATE
        )
        tempSharedPreferences = requireActivity().getSharedPreferences(
            Constants.TEMP_SHARED_PREFS,
            Context.MODE_PRIVATE
        )
        comingFromFavoriteSharedPreferences = requireActivity().getSharedPreferences(
            Constants.COMING_FROM_FAVORITE_MAP_SHARED_PREFS,
            Context.MODE_PRIVATE
        )
        offlineLocationSharedPreferences = requireActivity().getSharedPreferences(
            Constants.OFFLINE_LOCATION_SHARED_PREFS, Context.MODE_PRIVATE
        )
        fusedLocation = LocationServices.getFusedLocationProviderClient(requireContext())
        language =
            sharedPreferences.getString(Constants.LANGUAGE_KEY_SHARED_PREFERENCE, "default")
        if (language == "arabic") {
            language = Constants.ARABIC
        } else {
            language = Constants.ENGLISH
        }
        comeFromMapsSharedPrefs = requireActivity().getSharedPreferences(
            Constants.COME_FROM_MAP_PREFS,
            Context.MODE_PRIVATE
        )

        tempUnit =
            when (tempSharedPreferences.getString(Constants.TEMP_SHARED_PREFS_KEY, "kelvin")) {
                "celsius" -> TempUnit("metric", "°C")
                "fahrenheit" -> TempUnit("imperial", "°F")
                else -> TempUnit("standard", "°K")
            }


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



        val isComingFromMap = comeFromMapsSharedPrefs.getBoolean(Constants.COME_FROM_MAP_KEY, false)
        val gpsOrMap = mapsOrGpsSharedPreferences.getString(Constants.MAP_OR_GPS_KEY, "default")
        val isComingFromFavorite = comingFromFavoriteSharedPreferences.getString(
            Constants.COMING_FROM_FAVORITE_MAP_SHARED_PREFS_KEY,
            "false"
        ) == "true"

        when {
            isComingFromFavorite -> {
                val latitude = HomeFragmentArgs.fromBundle(requireArguments()).latitude
                val longitude = HomeFragmentArgs.fromBundle(requireArguments()).longitude
                updateLocationAndFetchWeather(latitude.toDouble(), longitude.toDouble())
                //reset the sharedPrefs to prevent crash
                comingFromFavoriteSharedPreferences.edit()
                    .putString(Constants.COMING_FROM_FAVORITE_MAP_SHARED_PREFS_KEY, "false").apply()
                mapsOrGpsSharedPreferences.edit().putString(Constants.MAP_OR_GPS_KEY,"not_map").apply()
            }

            isComingFromMap && gpsOrMap == "map" -> {
                val latitude = comeFromMapsSharedPrefs.getFloat(Constants.LATITUDE, 0.0f)
                val longitude = comeFromMapsSharedPrefs.getFloat(Constants.LONGITUDE, 0.0f)
                updateLocationAndFetchWeather(latitude.toDouble(), longitude.toDouble())
                comingFromFavoriteSharedPreferences.edit()
                    .putString(Constants.COMING_FROM_FAVORITE_MAP_SHARED_PREFS_KEY, "false").apply()
                mapsOrGpsSharedPreferences.edit().putString(Constants.MAP_OR_GPS_KEY,"map").apply()
            }

            else -> {
                comingFromFavoriteSharedPreferences.edit()
                    .putString(Constants.COMING_FROM_FAVORITE_MAP_SHARED_PREFS_KEY, "false").apply()
                mapsOrGpsSharedPreferences.edit().putString(Constants.MAP_OR_GPS_KEY,"not_map").apply()
                if (NetworkUtil.isInternetAvailable(requireContext())) {
                    binding.disablePermissionCardView.visibility = View.GONE
                    binding.disablePermissionConstraint.visibility = View.GONE
                    binding.textView2.visibility = View.GONE
                    binding.textView3.visibility = View.GONE
                    binding.btnAllow.visibility = View.GONE
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
//                    lifecycleScope.launch (Dis){  }
//                    makeUiVisible()
//                    val file = File(requireContext().cacheDir,"currentWeather")
//                    val fis = FileInputStream(file)
//                    val resultString = fis.readBytes().decodeToString()
//                    val currentWeather = Gson().fromJson(resultString,WeatherResponse::class.java)
//                    updateCurrentWeatherUi(currentWeather)
//                    Snackbar.make(requireView(), "there is no connection", 2000).show()

                }

            }
        }
    }

    private fun updateLocationAndFetchWeather(latitude: Double, longitude: Double) {
        Log.d("TAG", "Updating location and fetching weather for: $latitude, $longitude")
        lifecycleScope.launch(Dispatchers.Main) {
            val locale = Locale(language ?: "en")
            val geocoder = Geocoder(requireContext(), locale)
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            binding.tvCountryName.text = addresses?.get(0)?.getAddressLine(0) ?: "Unknown Location"


            homeViewModel.getHoursList(
                latitude,
                longitude,
                Constants.API_KEY,
                tempUnit.apiParam,
                language ?: "en",
            )
            homeViewModel.getCurrentWeather(
                latitude,
                longitude,
                Constants.API_KEY,
                tempUnit.apiParam,
                language ?: "en"
            )
            homeViewModel.getForecastDataByDay(
                latitude,
                longitude,
                Constants.API_KEY,
                tempUnit.apiParam,
                language ?: "en"
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val calender = Calendar.getInstance()
        val date = calender.time.toString().substring(0, 10)
        binding.tvDate.text = date


        binding.btnAllow.setOnClickListener {
            requestPermission()
        }


        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                homeViewModel.hoursList.collect { responseState ->
                    when (responseState) {
                        is UiState.Loading -> ""
                        is UiState.Failure -> ""
                        is UiState.Success<*> -> {
                            val hoursList = responseState.data as List<ListElement>
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
                    }
                }
            }
        }



        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                homeViewModel.currentWeather.collect { response ->
                    when (response) {
                        is UiState.Failure -> {
                            if (NetworkUtil.isInternetAvailable(requireContext())) {
                                binding.cardView.visibility = View.GONE
                                binding.daysRecyclerView.visibility = View.GONE
                                binding.hoursRecyclerView.visibility = View.GONE
                                binding.lastItemCardView.visibility = View.GONE
                                binding.progressbar.visibility = View.GONE
                                binding.tvDate.visibility = View.GONE
                                binding.tvCountryName.visibility = View.GONE
                                binding.disablePermissionCardView.visibility = View.GONE
                            }
                        }

                        is UiState.Success<*> -> {
                            binding.cardView.visibility = View.VISIBLE
                            binding.daysRecyclerView.visibility = View.VISIBLE
                            binding.hoursRecyclerView.visibility = View.VISIBLE
                            binding.lastItemCardView.visibility = View.VISIBLE
                            binding.progressbar.visibility = View.GONE
                            binding.tvDate.visibility = View.VISIBLE
                            binding.tvCountryName.visibility = View.VISIBLE
                            binding.disablePermissionCardView.visibility = View.GONE
                            binding.disablePermissionCardView.visibility = View.GONE
                            binding.disablePermissionConstraint.visibility = View.GONE
                            binding.textView2.visibility = View.GONE
                            binding.textView3.visibility = View.GONE
                            binding.btnAllow.visibility = View.GONE
                            val it = response.data as WeatherResponse
                            Log.i("TAG", "response is: $it")
                            updateCurrentWeatherUi(it)
                        }

                        else -> {
                            binding.cardView.visibility = View.GONE
                            binding.daysRecyclerView.visibility = View.GONE
                            binding.hoursRecyclerView.visibility = View.GONE
                            binding.lastItemCardView.visibility = View.GONE
                            binding.progressbar.visibility = View.VISIBLE
                            binding.tvDate.visibility = View.GONE
                            binding.tvCountryName.visibility = View.GONE
                            binding.disablePermissionCardView.visibility = View.GONE
                        }
                    }
                }
            }
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

                Log.i("TAG", "onViewCreated: MAX TEMP $maxTemp")

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
        locationRequest = LocationRequest.Builder(1000 * 180)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()
        return locationRequest
    }

    private fun getLocationCallback(): LocationCallback {
        return object : LocationCallback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                val latitude = p0.lastLocation?.latitude!!
                val longitude = p0.lastLocation?.longitude!!
                offlineLocationSharedPreferences.edit()
                    .putFloat(Constants.OFFLINE_LATITUDE, latitude.toFloat()).apply()
                offlineLocationSharedPreferences.edit()
                    .putFloat(Constants.OFFLINE_LONGITUDE, longitude.toFloat()).apply()


                val locale = Locale(language ?: "en")
                val country = Geocoder(requireContext(), locale)
                val x = country.getFromLocation(
                    p0.lastLocation?.latitude!!,
                    p0.lastLocation?.longitude!!,
                    1
                )
                binding.tvCountryName.text = "${x?.get(0)!!.countryName}, ${x?.get(0)!!.adminArea}"


                lifecycleScope.launch {
                    homeViewModel.getHoursList(
                        latitude,
                        longitude,
                        Constants.API_KEY,
                        tempUnit.apiParam,
                        language ?: "en"
                    )
                }
                homeViewModel.getCurrentWeather(
                    latitude,
                    longitude,
                    Constants.API_KEY,
                    tempUnit.apiParam,
                    language ?: "en"
                )


                lifecycleScope.launch(Dispatchers.IO) {
                    homeViewModel.getForecastDataByDay(
                        latitude,
                        longitude,
                        Constants.API_KEY,
                        "metric",
                        language ?: "en"
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isLocationEnabled()) {
                    binding.progressbar.visibility = View.VISIBLE
                    getLocation()
                }else{
                    showLocationSettingsDialog()
                }

            } else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED){
                binding.disablePermissionCardView.visibility = View.VISIBLE
                binding.disablePermissionConstraint.visibility = View.VISIBLE
                binding.textView2.visibility = View.VISIBLE
                binding.textView3.visibility = View.VISIBLE
                binding.btnAllow.visibility = View.VISIBLE
                binding.progressbar.visibility = View.GONE
            }
        }
    }

    private fun requestPermission() {
        binding.progressbar.visibility = View.GONE
        binding.disablePermissionConstraint.visibility = View.GONE
        binding.disablePermissionCardView.visibility = View.GONE
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            Constants.LOCATION_REQUEST_CODE
        )

    }


//    fun onPermissionResult(isGranted: Boolean) {
//        if (isGranted) {
//            if (isLocationEnabled()) {
//                getLocation()
//            } else {
//                showLocationSettingsDialog()
//            }
//        }
//    }

    fun showDisablePermissionUI() {
        binding.disablePermissionCardView.visibility = View.VISIBLE
        binding.disablePermissionConstraint.visibility = View.VISIBLE
        binding.tvDate.visibility = View.GONE
        binding.progressbar.visibility = View.GONE
        binding.tvCountryName.visibility = View.GONE
        binding.hoursRecyclerView.visibility = View.GONE
        binding.cardView.visibility = View.GONE
        binding.daysRecyclerView.visibility = View.GONE
        binding.lastItemCardView.visibility = View.GONE
    }

    private fun showLocationSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Location Services Not Enabled")
            .setMessage("Please enable location services to use this feature.")
            .setPositiveButton("Open Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                showDisablePermissionUI()
            }
            .show()
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


    fun changeLanguage(context: Context, languageCode: String? = null): Locale {
        if (languageCode != null) {
            // Set new language
            val localeList = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(localeList)
        }

        // Get current or newly set locale
        val currentLocaleList = AppCompatDelegate.getApplicationLocales()
        return if (!currentLocaleList.isEmpty) {
            currentLocaleList[0]!!
        } else {
            // Fallback to system locale if the app hasn't set a locale
            context.resources.configuration.locales[0]
        }
    }


    private fun updateCurrentWeatherUi(weatherResponse: WeatherResponse) {
        if (language == "ar") {
            binding.weatherTemp.text =
                NumberConverter.convertToArabicNumerals("${weatherResponse.main?.temp?.toInt()} ${tempUnit.symbol}")
            binding.weatherDescription.text = weatherResponse.weather?.get(0)?.description
            binding.ivIcon.setImageResource(getWeatherIcon(weatherResponse.weather?.get(0)?.icon!!))
            binding.pressureValue.text =
                NumberConverter.convertToArabicNumerals(weatherResponse.main?.pressure.toString() + " hpa")
            binding.humidityValue.text =
                NumberConverter.convertToArabicNumerals(weatherResponse.main?.humidity.toString() + " %")
            binding.windValue.text =
                NumberConverter.convertToArabicNumerals(weatherResponse.wind?.speed.toString() + " m/s")
            binding.cloudValue.text =
                NumberConverter.convertToArabicNumerals(weatherResponse.clouds?.all.toString() + " %")
            binding.seaLevelValue.text =
                NumberConverter.convertToArabicNumerals(weatherResponse.main?.seaLevel.toString() + " pa")
            binding.visibleValue.text =
                NumberConverter.convertToArabicNumerals(weatherResponse.visibility.toString() + " m")
        } else {
            binding.weatherTemp.text = "${weatherResponse.main?.temp?.toInt()} ${tempUnit.symbol}"
            binding.weatherDescription.text = weatherResponse.weather?.get(0)?.description
            binding.ivIcon.setImageResource(getWeatherIcon(weatherResponse.weather?.get(0)?.icon!!))
            binding.pressureValue.text = weatherResponse.main?.pressure.toString() + " hpa"
            binding.humidityValue.text = weatherResponse.main?.humidity.toString() + " %"
            binding.windValue.text = weatherResponse.wind?.speed.toString() + " m/s"
            binding.cloudValue.text = weatherResponse.clouds?.all.toString() + " %"
            binding.seaLevelValue.text = weatherResponse.main?.seaLevel.toString() + " pa"
            binding.visibleValue.text = weatherResponse.visibility.toString() + " m"
        }
    }

}
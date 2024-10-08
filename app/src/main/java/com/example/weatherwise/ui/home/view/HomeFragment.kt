package com.example.weatherwise.ui.home.view

import WeatherResponse
import android.Manifest
import android.annotation.SuppressLint
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
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherwise.uiState.UiState
import com.example.weatherwise.Constants
import com.example.weatherwise.R
import com.example.weatherwise.databinding.FragmentHomeBinding
import com.example.weatherwise.db.alertPlaces.AlertDatabaseBuilder
import com.example.weatherwise.db.alertPlaces.AlertLocalDataSource
import com.example.weatherwise.db.favoritePlaces.PlacesLocalDataSource
import com.example.weatherwise.db.favoritePlaces.PlacesLocalDatabaseBuilder
import com.example.weatherwise.helpers.NetworkUtil
import com.example.weatherwise.helpers.NumberConverter
import com.example.weatherwise.model.DailyWeather
import com.example.weatherwise.model.ListElement
import com.example.weatherwise.model.TempUnit
import com.example.weatherwise.network.api.RetrofitHelper
import com.example.weatherwise.network.api.WeatherRemoteDataSource
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
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset
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
    private lateinit var notificationTempSharedPreferences: SharedPreferences
    private lateinit var windSpeedSharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModelFactory =
            CurrentWeatherViewModelFactory(
                WeatherRepository.getInstance(
                    WeatherRemoteDataSource(RetrofitHelper),
                    PlacesLocalDataSource(PlacesLocalDatabaseBuilder.getInstance(requireContext()).placesDao()),
                    AlertLocalDataSource(AlertDatabaseBuilder.getInstance(requireContext()).alertDao())
                )
            )
        homeViewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)
        notificationTempSharedPreferences = requireActivity().getSharedPreferences(Constants.NOTIFICATION_ADDRESS_SHARED_PREFS, Context.MODE_PRIVATE)
        sharedPreferences = requireActivity().getSharedPreferences(
            Constants.LANGUAGE_SHARED_PREFS,
            Context.MODE_PRIVATE
        )

        windSpeedSharedPreferences = requireActivity().getSharedPreferences(Constants.WIND_SPEED_SHARED_PREFS, Context.MODE_PRIVATE)
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
                val latitude = HomeFragmentArgs.fromBundle(
                    requireArguments()
                ).latitude
                val longitude = HomeFragmentArgs.fromBundle(
                    requireArguments()
                ).longitude
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
                    if (NetworkUtil.isInternetAvailable(requireContext())){
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
                }else if(!NetworkUtil.isInternetAvailable(requireContext())){
                    readCurrentWeatherCache()
                    readHoursListCache()
                    readDayListCache()
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
                        is UiState.Loading -> Snackbar.make(requireView(),getString(R.string.Loading), 500).show()
                        is UiState.Failure -> Snackbar.make(requireView(),getString(R.string.Error_while_fetching_the_data), 500).show()
                        is UiState.Success<*> -> {
                            val hoursList = responseState.data as List<ListElement>
                            writeHoursListToCache(hoursList)
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
                                showUiCurrentWeatherFailureState()
                                Snackbar.make(requireView(), getString(R.string.Something_went_wrong),2000).show()
                            }
                            else{
                                Snackbar.make(requireView(), getString(R.string.No_internet_connection),2000).show()
                            }
                        }

                        is UiState.Success<*> -> {
                            showUiCurrentWeatherSuccessState()
                            val it = response.data as WeatherResponse
                            updateCurrentWeatherUi(it)
                            writeCurrentWeatherToCache(it)
                            notificationTempSharedPreferences.edit().putString(Constants.NOTIFICATION_ADDRESS_SHARED_PREFS_KEY, "${it.main?.temp?.toInt()!!} ${tempUnit.symbol}").apply()
                            Log.i("dataaaaaaaaa", "onViewCreated: $it")
                        }

                        is UiState.Loading -> {
                            showUiCurrentWeatherLoadingState()
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
                writeDayListToCache(dayItemList)

                val adapter = DaysAdapter()
                binding.daysRecyclerView.apply {
                    this.adapter = adapter
                    layoutManager = LinearLayoutManager(requireContext())
                }
                adapter.submitList(dayItemList)


            }
        }
    }

    private fun showUiCurrentWeatherLoadingState() {
        binding.progressbar.visibility = View.VISIBLE
        binding.tvDate.visibility = View.GONE
        binding.tvCountryName.visibility = View.GONE
        binding.cardView.visibility = View.GONE
        binding.daysRecyclerView.visibility = View.GONE
        binding.hoursRecyclerView.visibility = View.GONE
        binding.lastItemCardView.visibility = View.GONE
        binding.tvDate.visibility = View.GONE
        binding.tvCountryName.visibility = View.GONE
        binding.disablePermissionConstraint.visibility = View.GONE
        binding.textView2.visibility = View.GONE
        binding.textView3.visibility = View.GONE
        binding.btnAllow.visibility = View.GONE
    }

    private fun showUiCurrentWeatherFailureState() {
        binding.cardView.visibility = View.GONE
        binding.daysRecyclerView.visibility = View.GONE
        binding.hoursRecyclerView.visibility = View.GONE
        binding.lastItemCardView.visibility = View.GONE
        binding.progressbar.visibility = View.GONE
        binding.tvDate.visibility = View.GONE
        binding.tvCountryName.visibility = View.GONE
    }

    private fun showUiCurrentWeatherSuccessState() {
        binding.cardView.visibility = View.VISIBLE
        binding.daysRecyclerView.visibility = View.VISIBLE
        binding.hoursRecyclerView.visibility = View.VISIBLE
        binding.lastItemCardView.visibility = View.VISIBLE
        binding.progressbar.visibility = View.GONE
        binding.tvDate.visibility = View.VISIBLE
        binding.tvCountryName.visibility = View.VISIBLE
        binding.disablePermissionConstraint.visibility = View.GONE
        binding.textView2.visibility = View.GONE
        binding.textView3.visibility = View.GONE
        binding.btnAllow.visibility = View.GONE
    }

    private fun writeCurrentWeatherToCache(weatherResponse: WeatherResponse){
        lifecycleScope.launch(Dispatchers.IO) {
            val resultAsString = Gson().toJson(weatherResponse)
            val file = File(requireContext().cacheDir, "currentWeather")
            val fos = FileOutputStream(file)
            fos.write(resultAsString.toByteArray(Charset.defaultCharset()))
        }
    }

    private fun writeHoursListToCache(hoursList: List<ListElement>) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val resultAsString = Gson().toJson(hoursList)
                val file = File(requireContext().cacheDir, "hoursList")
                FileOutputStream(file).use { fos ->
                    fos.write(resultAsString.toByteArray(Charset.defaultCharset()))
                }
            } catch (e: Exception) {
                Log.e("CacheError", "Error writing hours list to cache", e)
            }
        }
    }

    private fun readHoursListCache(){
        lifecycleScope.launch(Dispatchers.IO) {
            binding.progressbar.visibility = View.GONE
            val file = File(requireContext().cacheDir,"hoursList")
            if (file.exists()){
                val fis = FileInputStream(file)
                val resultString = fis.readBytes().decodeToString()
                val hoursList: List<ListElement> = Gson().fromJson<List<ListElement>?>(resultString, object : TypeToken<List<ListElement>>() {}.type).map {
                        element ->
                    element.copy(
                        dtTxt = element.dtTxt.substring(10, element.dtTxt.lastIndex)
                    )
                }
                Log.i("TAG", "readHoursListCache: $hoursList")
                withContext(Dispatchers.Main){
                    binding.hoursRecyclerView.visibility = View.VISIBLE
                    showHoursListOnUi(hoursList)
                }
            }else{
                Snackbar.make(requireView(),getString(R.string.no_cached_file_for_hours_list),2000).show()
            }
        }
    }

    private fun writeDayListToCache(dayList: List<DailyWeather>) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val resultAsString = Gson().toJson(dayList)
                val file = File(requireContext().cacheDir, "dayList")
                FileOutputStream(file).use { fos ->
                    fos.write(resultAsString.toByteArray(Charset.defaultCharset()))
                }
            } catch (e: Exception) {
                Log.e("CacheError", "Error writing hours dayList to cache", e)
            }
        }
    }

    private fun readDayListCache(){
        lifecycleScope.launch(Dispatchers.IO) {
            binding.progressbar.visibility = View.GONE
            val file = File(requireContext().cacheDir,"dayList")
            if (file.exists()){
                val fis = FileInputStream(file)
                val resultString = fis.readBytes().decodeToString()
                val dayList: List<DailyWeather> = Gson().fromJson<List<DailyWeather>?>(resultString, object : TypeToken<List<DailyWeather>>() {}.type)
                withContext(Dispatchers.Main){
                    binding.daysRecyclerView.visibility = View.VISIBLE
                    showDaysListOnUi(dayList)
                }
            }else{
                Snackbar.make(requireView(),getString(R.string.no_cached_file_for_day_list),2000).show()
            }
        }
    }

    private fun showDaysListOnUi(dayList:List<DailyWeather>){
        val adapter = DaysAdapter()
        binding.daysRecyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        adapter.submitList(dayList)
    }


    private fun showHoursListOnUi(hoursList:List<ListElement>){
        val adapter = HoursAdapter()
        binding.hoursRecyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL, false)
        }
        adapter.submitList(hoursList)

    }

    private fun readCurrentWeatherCache(){
        binding.progressbar.visibility = View.GONE
        val file = File(requireContext().cacheDir,"currentWeather")
        if (file.exists()){
            val fis = FileInputStream(file)
            val resultString = fis.readBytes().decodeToString()
            val currentWeatherObj = Gson().fromJson(resultString,WeatherResponse::class.java)
            binding.cardView.visibility = View.VISIBLE
            binding.lastItemCardView.visibility = View.VISIBLE
            updateCurrentWeatherUi(currentWeatherObj)
        }else{
            Snackbar.make(requireView(),getString(R.string.no_cached_file_current_weather),2000).show()
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


                val locale = Locale(language ?: "en")
                val country = Geocoder(requireContext(), locale)
                val x = country.getFromLocation(
                    p0.lastLocation?.latitude!!,
                    p0.lastLocation?.longitude!!,
                    1
                )
                val address = "${x?.get(0)!!.countryName}, ${x[0]!!.adminArea}"
                binding.tvCountryName.text = address


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

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isLocationEnabled()) {
                    getLocation()
                }else{
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }

            } else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED){
                binding.disablePermissionCardView.visibility = View.VISIBLE
                binding.disablePermissionConstraint.visibility = View.VISIBLE
                binding.textView2.visibility = View.VISIBLE
                binding.textView3.visibility = View.VISIBLE
                binding.btnAllow.visibility = View.VISIBLE
                binding.progressbar.visibility = View.GONE
                binding.cardView.visibility = View.GONE
                binding.lastItemCardView.visibility = View.GONE
            }
        }
    }

    private fun requestPermission() {
        binding.disablePermissionConstraint.visibility = View.GONE
        binding.disablePermissionCardView.visibility = View.GONE
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            Constants.LOCATION_REQUEST_CODE
        )

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



    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun updateCurrentWeatherUi(weatherResponse: WeatherResponse) {
        val windUnit = windSpeedSharedPreferences.getString(Constants.WIND_SPEED_SHARED_PREFS_KEY, "meter")
        val windSpeed = when(windUnit){
            "meter" -> "${weatherResponse.wind?.speed} m/s"
            else -> String.format("%.2f", (weatherResponse.wind?.speed)?.times(2.236936)) + " M/h"
        }
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
                NumberConverter.convertToArabicNumerals(windSpeed)
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
            binding.windValue.text = windSpeed
            binding.cloudValue.text = weatherResponse.clouds?.all.toString() + " %"
            binding.seaLevelValue.text = weatherResponse.main?.seaLevel.toString() + " pa"
            binding.visibleValue.text = weatherResponse.visibility.toString() + " m"
        }
    }

}
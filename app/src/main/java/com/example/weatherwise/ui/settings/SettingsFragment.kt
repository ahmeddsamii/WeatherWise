package com.example.weatherwise.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.navigation.Navigation
import com.example.weatherwise.Constants
import com.example.weatherwise.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var languageSharedPreferenceies: SharedPreferences
    private lateinit var mapOrGpsSharedPreferences: SharedPreferences
    private lateinit var tempSharedPreference:SharedPreferences
    private lateinit var windSpeedSharedPreferences: SharedPreferences
    private lateinit var notificationSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        windSpeedSharedPreferences = requireActivity().getSharedPreferences(Constants.WIND_SPEED_SHARED_PREFS, Context.MODE_PRIVATE)
        tempSharedPreference = requireActivity().getSharedPreferences(Constants.TEMP_SHARED_PREFS, Context.MODE_PRIVATE)
        languageSharedPreferenceies = requireActivity().getSharedPreferences(Constants.LANGUAGE_SHARED_PREFS, Context.MODE_PRIVATE)
        mapOrGpsSharedPreferences = requireActivity().getSharedPreferences(Constants.MAP_OR_GPS_SHARED_PREFS, Context.MODE_PRIVATE)
        notificationSharedPreferences = requireActivity().getSharedPreferences(Constants.NOTIFICATION_SHARED_PREFS, Context.MODE_PRIVATE)
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.englishRB.isChecked = true
        binding.arabicRB.setOnClickListener {
            languageSharedPreferenceies.edit().putString(Constants.LANGUAGE_KEY_SHARED_PREFERENCE, "arabic").apply()
            changeLanguage("ar")
            restartActivity()
        }
        binding.englishRB.setOnClickListener {
            languageSharedPreferenceies.edit().putString(Constants.LANGUAGE_KEY_SHARED_PREFERENCE, "english").apply()
            changeLanguage("en")
            restartActivity()
            binding.englishRB.isChecked = true
        }

        binding.celsius.setOnClickListener {
            tempSharedPreference.edit().putString(Constants.TEMP_SHARED_PREFS_KEY,"celsius").apply()
            binding.celsius.isChecked = true
            restartActivity()
        }

        binding.fahrenheit.setOnClickListener {
            tempSharedPreference.edit().putString(Constants.TEMP_SHARED_PREFS_KEY, "fahrenheit").apply()
            binding.fahrenheit.isChecked = true
            restartActivity()
        }

        binding.kelvin.setOnClickListener {
            tempSharedPreference.edit().putString(Constants.TEMP_SHARED_PREFS_KEY, "kelvin").apply()
            binding.kelvin.isChecked = true
            restartActivity()
        }


        binding.usingMap.setOnClickListener {
            mapOrGpsSharedPreferences.edit().putString(Constants.MAP_OR_GPS_KEY,"map").apply()
            val action = SettingsFragmentDirections.actionNavSettingsToMapFragment()
            Navigation.findNavController(requireView()).navigate(action)
            binding.usingMap.isChecked = true
        }

        binding.usingGPS.setOnClickListener {
            mapOrGpsSharedPreferences.edit().putString(Constants.MAP_OR_GPS_KEY,"not_map").apply()
            binding.usingGPS.isChecked = true
        }

        binding.meterPerSecond.setOnClickListener {
            windSpeedSharedPreferences.edit().putString(Constants.WIND_SPEED_SHARED_PREFS_KEY,"meter").apply()
        }
        binding.milePerHour.setOnClickListener {
            windSpeedSharedPreferences.edit().putString(Constants.WIND_SPEED_SHARED_PREFS_KEY, "mile").apply()
        }


        binding.notification.setOnClickListener {
            notificationSharedPreferences.edit().putString(Constants.NOTIFICATION_SHARED_PREFS_KEY, "notification").apply()
        }

        binding.alarm.setOnClickListener {
            notificationSharedPreferences.edit().putString(Constants.NOTIFICATION_SHARED_PREFS_KEY, "alarm").apply()
        }
    }

    override fun onStart() {
        super.onStart()
        val language =  languageSharedPreferenceies.getString(Constants.LANGUAGE_KEY_SHARED_PREFERENCE,"default")
        //val gpsOrMap = mapOrGpsSharedPreferences.getString(Constants.MAP_OR_GPS_KEY, "default")
        if (language == "arabic"){
            binding.arabicRB.isChecked = true
            binding.englishRB.isChecked = false
        }else{
            binding.arabicRB.isChecked = false
            binding.englishRB.isChecked = true
        }

        val gpsOrMap = mapOrGpsSharedPreferences.getString(Constants.MAP_OR_GPS_KEY,"default")
        if (gpsOrMap == "map") binding.usingMap.isChecked = true else binding.usingGPS.isChecked = true


        val temp = tempSharedPreference.getString(Constants.TEMP_SHARED_PREFS_KEY, "kelvin")
        if (temp == "celsius"){
            binding.celsius.isChecked = true
        }else if(temp == "fahrenheit"){
            binding.fahrenheit.isChecked = true
        }else{
            binding.kelvin.isChecked = true
        }

        val windSpeed = windSpeedSharedPreferences.getString(Constants.WIND_SPEED_SHARED_PREFS_KEY, "meter")

        if (windSpeed == "meter"){
            binding.meterPerSecond.isChecked = true
        }else{
            binding.milePerHour.isChecked = true
        }


        val notificationOrAlarm = notificationSharedPreferences.getString(Constants.NOTIFICATION_SHARED_PREFS_KEY, "alarm")

        if (notificationOrAlarm == "notification"){
            binding.notification.isChecked = true
        }else if (notificationOrAlarm == "alarm"){
            binding.alarm.isChecked = true
        }



    }



    private fun changeLanguage(language: String) {
        val languageCode = when (language) {
            "ar" -> "arabic"
            "en" -> "english"
            else -> "english"
        }
        languageSharedPreferenceies.edit().putString(Constants.LANGUAGE_KEY_SHARED_PREFERENCE, languageCode).apply()
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(language)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    private fun restartActivity() {
        activity?.let {
            val intent = it.intent
            it.finish()
            startActivity(intent)
        }
    }
}
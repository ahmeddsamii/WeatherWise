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
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mapOrGpsSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        sharedPreferences = requireActivity().getSharedPreferences(Constants.LANGUAGE_SHARED_PREFS, Context.MODE_PRIVATE)
        mapOrGpsSharedPreferences = requireActivity().getSharedPreferences(Constants.MAP_OR_GPS_SHARED_PREFS, Context.MODE_PRIVATE)

        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.englishRB.isChecked = true
        binding.arabicRB.setOnClickListener {
            sharedPreferences.edit().putString(Constants.LANGUAGE_KEY_SHARED_PREFERENCE, "arabic").apply()
            changeLanguage("ar")
            restartActivity()
        }
        binding.englishRB.setOnClickListener {
            sharedPreferences.edit().putString(Constants.LANGUAGE_KEY_SHARED_PREFERENCE, "english").apply()
            changeLanguage("en")
            restartActivity()
            binding.englishRB.isChecked = true
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
    }

    override fun onStart() {
        super.onStart()
        val language =  sharedPreferences.getString(Constants.LANGUAGE_KEY_SHARED_PREFERENCE,"default")
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



    }



    private fun changeLanguage(language: String) {
        val languageCode = when (language) {
            "ar" -> "arabic"
            "en" -> "english"
            else -> "english"
        }
        sharedPreferences.edit().putString(Constants.LANGUAGE_KEY_SHARED_PREFERENCE, languageCode).apply()
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
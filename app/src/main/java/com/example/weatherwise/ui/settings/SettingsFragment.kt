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
import com.example.weatherwise.Constants
import com.example.weatherwise.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment() {
    lateinit var binding: FragmentSettingsBinding
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)

        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.arabicRB.setOnClickListener {
            sharedPreferences.edit().putString(Constants.LANGUAGE_KEY_SHARED_PREFERENCE, "arabic").apply()
            changeLanguage("ar")
            restartActivity()
        }
        binding.englishRB.setOnClickListener {
            sharedPreferences.edit().putString(Constants.LANGUAGE_KEY_SHARED_PREFERENCE, "english").apply()
            changeLanguage("en")
            restartActivity()
        }
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
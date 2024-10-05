package com.example.weatherwise.initalFragment

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.weatherwise.Constants
import com.example.weatherwise.R
import com.example.weatherwise.databinding.FragmentInitalBinding
import com.example.weatherwise.ui.settings.SettingsFragmentDirections
import kotlinx.coroutines.flow.combine


class InitalFragment : DialogFragment() {
    lateinit var binding:FragmentInitalBinding
    lateinit var mapOrGpsSharedPreferences:SharedPreferences
    private lateinit var notificationOrAlarmSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapOrGpsSharedPreferences = requireActivity().getSharedPreferences(Constants.MAP_OR_GPS_SHARED_PREFS, Context.MODE_PRIVATE)
        notificationOrAlarmSharedPreferences = requireActivity().getSharedPreferences(Constants.NOTIFICATION_SHARED_PREFS,Context.MODE_PRIVATE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
            )

            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInitalBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val check = mapOrGpsSharedPreferences.getString(Constants.MAP_OR_GPS_KEY,"default")
        if (check == "map" || check == "not_map"){
            Handler.createAsync(Looper.getMainLooper()).post {
                val action = InitalFragmentDirections.actionInitalFragmentToNavHome(0.0f, 0.0f)
                findNavController().navigate(action)
            }
        }


        binding.okButton.setOnClickListener {
            if (binding.mapOption.isChecked) {
                mapOrGpsSharedPreferences.edit().putString(Constants.MAP_OR_GPS_KEY,"map").apply()
                val action = InitalFragmentDirections.actionInitalFragmentToMapFragment()
                findNavController().navigate(action)
            } else {
                mapOrGpsSharedPreferences.edit().putString(Constants.MAP_OR_GPS_KEY,"not_map").apply()
                val action = InitalFragmentDirections.actionInitalFragmentToNavHome(0.0f, 0.0f)
                findNavController().navigate(action)
            }

            if (binding.alarmSwitch.isChecked){
                notificationOrAlarmSharedPreferences.edit().putString(Constants.NOTIFICATION_SHARED_PREFS_KEY,"alarm").apply()
            }else{
                notificationOrAlarmSharedPreferences.edit().putString(Constants.NOTIFICATION_SHARED_PREFS_KEY,"notification").apply()
            }
            dismiss()
        }


    }


}
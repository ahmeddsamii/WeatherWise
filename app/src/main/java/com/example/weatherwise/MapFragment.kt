package com.example.weatherwise

import android.content.Context
import android.content.SharedPreferences
import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.weatherwise.databinding.BottomSheetDialogBinding
import com.example.weatherwise.databinding.FragmentMapBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MapFragment : Fragment(), OnMapReadyCallback {
    lateinit var binding:FragmentMapBinding
    lateinit var googleMap: GoogleMap
    lateinit var bottomSheetBinding: BottomSheetDialogBinding
    lateinit var mapSharedPreferences:SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment

        mapFragment.getMapAsync(this)
         mapSharedPreferences= requireActivity().getSharedPreferences(Constants.COME_FROM_MAP_PREFS, Context.MODE_PRIVATE)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap.setOnMapClickListener { latLng ->

            googleMap.clear()


            val markerOptions = MarkerOptions().position(latLng)
            googleMap.addMarker(markerOptions)

            // Get the latitude and longitude
            val latitude = latLng.latitude
            val longitude = latLng.longitude

            showBottomSheet(latitude,longitude)


            val message = "Lat: $latitude, Lng: $longitude"
            //Toast.makeText(context, message, Toast.LENGTH_SHORT).show()


        }
    }


    private fun showBottomSheet(latitude: Double, longitude: Double) {
        val dialog = BottomSheetDialog(requireContext())
        bottomSheetBinding = BottomSheetDialogBinding.inflate(layoutInflater)
        dialog.setContentView(bottomSheetBinding.root)
        val geocoder = Geocoder(requireContext())
        lifecycleScope.launch(Dispatchers.IO) {
            val address = geocoder.getFromLocation(latitude, longitude, 1)?.get(0)?.getAddressLine(0)
            withContext(Dispatchers.Main) {
                bottomSheetBinding.cityName.text = address.toString()
                dialog.show()
                bottomSheetBinding.btnSave.setOnClickListener {
                    mapSharedPreferences.edit().apply {
                        putBoolean(Constants.COME_FROM_MAP_KEY, true)
                        putFloat(Constants.LATITUDE,latitude.toFloat())
                        putFloat(Constants.LONGITUDE,longitude.toFloat())
                        apply()
                    }
                    val action = MapFragmentDirections.actionMapFragmentToNavHome(latitude.toFloat(), longitude.toFloat())
                    Navigation.findNavController(requireView()).navigate(action)

                    dialog.dismiss()
                }
            }
        }
    }

}
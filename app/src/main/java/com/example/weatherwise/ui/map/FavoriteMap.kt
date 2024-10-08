package com.example.weatherwise.ui.map

import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.weatherwise.R
import com.example.weatherwise.databinding.BottomSheetDialogBinding
import com.example.weatherwise.databinding.FragmentFavoriteMapBinding
import com.example.weatherwise.db.alertPlaces.AlertDatabaseBuilder
import com.example.weatherwise.db.alertPlaces.AlertLocalDataSource
import com.example.weatherwise.db.favoritePlaces.PlacesLocalDataSource
import com.example.weatherwise.db.favoritePlaces.PlacesLocalDatabaseBuilder
import com.example.weatherwise.model.FavoritePlace
import com.example.weatherwise.network.api.RetrofitHelper
import com.example.weatherwise.network.api.WeatherRemoteDataSource
import com.example.weatherwise.ui.favorite.viewModel.FavoriteViewModel
import com.example.weatherwise.ui.favorite.viewModel.FavoriteViewModelFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FavoriteMap : Fragment(),OnMapReadyCallback {
    lateinit var binding:FragmentFavoriteMapBinding
    lateinit var googleMap: GoogleMap
    lateinit var bottomSheetBinding: BottomSheetDialogBinding
    lateinit var favoriteViewModel: FavoriteViewModel
    lateinit var factory: FavoriteViewModelFactory


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoriteMapBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.favorite_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        factory = FavoriteViewModelFactory(WeatherRepository.getInstance(
            WeatherRemoteDataSource(RetrofitHelper),
            PlacesLocalDataSource(PlacesLocalDatabaseBuilder.getInstance(requireContext()).placesDao()),
            AlertLocalDataSource(AlertDatabaseBuilder.getInstance(requireContext()).alertDao())
        )
        )
        favoriteViewModel = ViewModelProvider(this, factory).get(FavoriteViewModel::class.java)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap.setOnMapClickListener { latLng ->

            googleMap.clear()


            val markerOptions = MarkerOptions().position(latLng)
            googleMap.addMarker(markerOptions)


            val latitude = latLng.latitude
            val longitude = latLng.longitude

            showBottomSheet(latitude,longitude)

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
                    favoriteViewModel.addFavoritePlace(FavoritePlace(address.toString(),latitude,longitude))
                    dialog.dismiss()
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

}
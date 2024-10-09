package com.example.weatherwise.ui.favorite.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherwise.Constants
import com.example.weatherwise.R
import com.example.weatherwise.databinding.FragmentFavoriteBinding
import com.example.weatherwise.db.alertPlaces.AlertDatabaseBuilder
import com.example.weatherwise.db.alertPlaces.AlertLocalDataSource
import com.example.weatherwise.db.favoritePlaces.PlacesLocalDataSource
import com.example.weatherwise.db.favoritePlaces.PlacesLocalDatabaseBuilder
import com.example.weatherwise.model.FavoritePlace
import com.example.weatherwise.network.api.RetrofitHelper
import com.example.weatherwise.network.api.WeatherRemoteDataSource
import com.example.weatherwise.ui.favorite.viewModel.FavoriteViewModel
import com.example.weatherwise.ui.favorite.viewModel.FavoriteViewModelFactory
import com.example.weatherwise.uiState.UiState
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoriteFragment : Fragment(), OnFavoriteDeleteListener, OnCardViewClicked {

    private lateinit var binding: FragmentFavoriteBinding
    private lateinit var factory: FavoriteViewModelFactory
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var adapter: FavoriteAdapter
    lateinit var comingFromFavoriteSharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        comingFromFavoriteSharedPreferences = requireActivity().getSharedPreferences(Constants.COMING_FROM_FAVORITE_MAP_SHARED_PREFS, Context.MODE_PRIVATE)
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        observeFavoritePlaces()
        setupFabClickListener(view)
    }

    private fun setupViewModel() {
        factory = FavoriteViewModelFactory(WeatherRepository.getInstance(
            WeatherRemoteDataSource(RetrofitHelper),
            PlacesLocalDataSource(PlacesLocalDatabaseBuilder.getInstance(requireContext()).placesDao()),
            AlertLocalDataSource(AlertDatabaseBuilder.getInstance(requireContext()).alertDao())
        )
        )
        favoriteViewModel = ViewModelProvider(this, factory).get(FavoriteViewModel::class.java)
    }

    private fun setupRecyclerView() {
        adapter = FavoriteAdapter(this, this)
        binding.favoritePlacesRecyclerView.apply {
            this.adapter = this@FavoriteFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeFavoritePlaces() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED){
                favoriteViewModel.allLocalFavoritePlaces.collect{state->
                    when(state){
                        is UiState.Loading -> Snackbar.make(requireView(),getString(R.string.fetching_favorite_places_please_wait), 2000).show()
                        is UiState.Failure -> Snackbar.make(requireView(),getString(R.string.Something_went_wrong), 1000).show()
                        is UiState.Success<*> -> {
                            val list = state.data as List<FavoritePlace>
                            if (list.isEmpty()){
                                Snackbar.make(requireView(),getString(R.string.there_are_no_favorite_places), 1000).show()
                            }
                            adapter.submitList(list)
                        }
                    }
                }
            }
        }
    }

    private fun setupFabClickListener(view: View) {
        binding.fabAdd.setOnClickListener {
            val action =
                com.example.weatherwise.ui.favorite.view.FavoriteFragmentDirections.actionNavFavoriteToFavoriteMap2()
            Navigation.findNavController(view).navigate(action)
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.IO) {
            favoriteViewModel.getAllLocalFavoritePlaces()
        }
    }

    override fun onClick(favoritePlace: FavoritePlace) {
        showDeleteConfirmationDialog(favoritePlace)
    }

    override fun onCardClick(favoritePlace: FavoritePlace) {
        comingFromFavoriteSharedPreferences.edit().putString(Constants.COMING_FROM_FAVORITE_MAP_SHARED_PREFS_KEY,"true").apply()
        val action =
            com.example.weatherwise.ui.favorite.view.FavoriteFragmentDirections.actionNavFavoriteToNavHome(
                favoritePlace.latitude.toFloat(),
                favoritePlace.longitude.toFloat()
            )
        Navigation.findNavController(requireView()).navigate(action)
    }

    private fun showDeleteConfirmationDialog(favoritePlace: FavoritePlace) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.Confirm_Deletion))
            .setMessage(getString(R.string.Are_you_sure_you_want_to_remove_this_favorite_place))
            .setPositiveButton(R.string.Yes) { _, _ ->
                favoriteViewModel.removeFavoritePlace(favoritePlace)
            }
            .setNegativeButton(R.string.No, null)
            .show()
    }
}
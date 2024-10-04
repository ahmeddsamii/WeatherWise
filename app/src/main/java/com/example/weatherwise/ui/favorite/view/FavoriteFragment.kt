package com.example.weatherwise.ui.favorite.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherwise.Constants
import com.example.weatherwise.databinding.FragmentFavoriteBinding
import com.example.weatherwise.model.FavoritePlace
import com.example.weatherwise.ui.favorite.viewModel.FavoriteViewModel
import com.example.weatherwise.ui.favorite.viewModel.FavoriteViewModelFactory
import com.example.weatherwise.uiState.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoriteFragment : Fragment(),OnFavoriteDeleteListener, OnCardViewClicked {

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

        factory = FavoriteViewModelFactory(WeatherRepository.getInstance(requireContext()))
        favoriteViewModel = ViewModelProvider(this, factory).get(FavoriteViewModel::class.java)

        setupRecyclerView()
        observeFavoritePlaces()
        setupFabClickListener(view)
    }

    private fun setupRecyclerView() {
        adapter = FavoriteAdapter(this, this)
        binding.favoritePlacesRecyclerView.apply {
            this.adapter = this@FavoriteFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeFavoritePlaces() {
//        favoriteViewModel.allLocalFavoritePlaces.observe(viewLifecycleOwner) { places ->
//            adapter.submitList(places)
//        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED){
                favoriteViewModel.allLocalFavoritePlaces.collect{state->
                    when(state){
                        is UiState.Loading -> ""
                        is UiState.Failure -> ""
                        is UiState.Success<*> -> {
                            val list = state.data as List<FavoritePlace>
                            adapter.submitList(list)
                        }
                    }
                }
            }
        }
    }

    private fun setupFabClickListener(view: View) {
        binding.fabAdd.setOnClickListener {
            val action = FavoriteFragmentDirections.actionNavFavoriteToFavoriteMap2()
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
        favoriteViewModel.removeFavoritePlace(favoritePlace)
    }

    override fun onCardClick(favoritePlace: FavoritePlace) {
        comingFromFavoriteSharedPreferences.edit().putString(Constants.COMING_FROM_FAVORITE_MAP_SHARED_PREFS_KEY,"true").apply()
        val action = FavoriteFragmentDirections.actionNavFavoriteToNavHome(favoritePlace.latitude.toFloat(), favoritePlace.longitude.toFloat())
        Navigation.findNavController(requireView()).navigate(action)
    }
}
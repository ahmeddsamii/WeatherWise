package com.example.weatherwise

import Clouds
import Coord
import Main
import Sys
import Weather
import WeatherRepository
import WeatherResponse
import Wind
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.weatherwise.db.alertPlaces.AlertLocalDataSource
import com.example.weatherwise.db.alertPlaces.FakeAlertLocalDataSource
import com.example.weatherwise.db.favoritePlaces.FakePlaceLocalDataSource
import com.example.weatherwise.db.favoritePlaces.PlacesLocalDataSource
import com.example.weatherwise.model.City
import com.example.weatherwise.model.ListElement
import com.example.weatherwise.model.WeatherForecastResponse
import com.example.weatherwise.network.api.FakeRemoteDataSource
import com.example.weatherwise.repositories.FakeRepository
import com.example.weatherwise.repositories.IWeatherRepository
import com.example.weatherwise.ui.home.viewModel.HomeViewModel
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)

class HomeViewModelTest {
    lateinit var fakeRepo:FakeRepository
    lateinit var current:WeatherResponse
    lateinit var weatherResponse:WeatherForecastResponse
    lateinit var viewModel:HomeViewModel

    @Before
    fun setup(){

        fakeRepo = FakeRepository()
        viewModel = HomeViewModel(fakeRepo)
        current = WeatherResponse(
            id = 501,
            coord = Coord(10.0, 60.8),
            weather = listOf(Weather(1L, "dlkfjsdlk", "desc", "dlfkjkdsl")),
            base = "stations",
            main = Main(285.95, 285.74, 284.94, 287.76, 1009, 94, 1009, 942),
            visibility = 8412,
            wind = Wind(2.87, 167, 5.81),
            clouds = Clouds(100),
            dt = 1728380785,
            sys = Sys(2, 2044440, "IT", 1728364943, 1728405860),
            timezone = 7200,
            name = "Zocca",
            cod = 200
        )

        weatherResponse = WeatherForecastResponse(
            "cod", 10L, 8L, emptyList<ListElement>(), City(
                1L,
                "city",
                com.example.weatherwise.model.Coord(10.0, 60.8),
                "fdlksjf",
                1L,
                2L,
                3L,
                4L
            )
        )


    }


    @Test
    fun getCurrentWeather_Success ()= runBlocking {
        // Given
        val latitude = 0.0
        val longitude = 0.0


        // When
        val result = viewModel.getForecastDataByDay(latitude, longitude,Constants.API_KEY,"metric","en")

        // Then
        assertThat(result, not(nullValue()))
    }
    @Test
    fun getForecast_Success () = runBlocking {

        val latitude = 0.0
        val longitude = 0.0



        val result = viewModel.getForecastDataByDay(latitude,longitude,Constants.API_KEY,"metric", "en")

        assertThat(result, not(nullValue()))

    }

}
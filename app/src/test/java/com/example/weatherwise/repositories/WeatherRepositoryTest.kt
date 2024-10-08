package com.example.weatherwise.repositories

import Clouds
import Coord
import Main
import Sys
import Weather
import WeatherRepository
import WeatherResponse
import Wind
import com.example.weatherwise.Constants
import com.example.weatherwise.db.alertPlaces.AlertDao
import com.example.weatherwise.db.alertPlaces.AlertLocalDataSource
import com.example.weatherwise.db.alertPlaces.FakeAlertLocalDataSource
import com.example.weatherwise.db.favoritePlaces.FakePlaceLocalDataSource
import com.example.weatherwise.db.favoritePlaces.PlacesLocalDataSource
import com.example.weatherwise.model.AlertDto
import com.example.weatherwise.model.City
import com.example.weatherwise.model.FavoritePlace
import com.example.weatherwise.model.ListElement
import com.example.weatherwise.model.WeatherForecastResponse
import com.example.weatherwise.network.api.FakeRemoteDataSource
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.Before
import org.junit.Test
import java.util.UUID

class WeatherRepositoryTest {
    lateinit var remoteDataSource: FakeRemoteDataSource
    lateinit var alertLocalDataSource: FakeAlertLocalDataSource
    lateinit var placeLocalDataSource: FakePlaceLocalDataSource
    lateinit var repository: WeatherRepository
    lateinit var current: WeatherResponse

    @Before
    fun setup() {
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

        var weatherResponse = WeatherForecastResponse(
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
        remoteDataSource = FakeRemoteDataSource(current, weatherResponse)
        alertLocalDataSource = FakeAlertLocalDataSource()
        placeLocalDataSource = FakePlaceLocalDataSource()
        repository = WeatherRepository.getInstance(
            remoteDataSource,
            placeLocalDataSource,
            alertLocalDataSource
        )
    }


    @Test
    fun getCurrentWeatherOfDay_LongAndLat_CurrentDayWeatherResponse() = runBlocking {

        //When
        var result = repository.getCurrentWeather(0.0, 0.0, Constants.API_KEY, "metric", "en")
            .first() // leeeh ?!
        assertNotNull(result)

        //
        assertEquals(current, result)
    }

    @Test
    fun insertFavLocation_FavouriteLocationItem_addsItemToRepository() = runBlocking {
        val newPlace = FavoritePlace("ddd", 10.0, 80.0)
        val insertResult = repository.addPlace(newPlace).first()

        // Check if insertion was successful
        assertEquals(1, insertResult)

        // Check if the item is in the repository
//        val allFavorites = repository.getAllLocalFavoritePlaces().first()
//        assertTrue(allFavorites.contains(newPlace))
    }
}
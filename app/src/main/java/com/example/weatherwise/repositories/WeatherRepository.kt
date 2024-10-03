import android.content.Context
import com.example.weatherwise.db.PlacesLocalDataSource
import com.example.weatherwise.model.FavoritePlace
import com.example.weatherwise.model.WeatherForecastResponse
import com.example.weatherwise.network.api.RetrofitHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import retrofit2.Retrofit

class WeatherRepository private constructor(val retrofit: RetrofitHelper,val placesLocalDataSource: PlacesLocalDataSource) {

   companion object{
       private var INSTANCE: WeatherRepository? = null
       fun getInstance(context: Context):WeatherRepository{
           return INSTANCE?: synchronized(this){
               val instance = WeatherRepository(RetrofitHelper, PlacesLocalDataSource.getInstance(context))
                INSTANCE=instance
               instance
           }
       }
   }


    suspend fun getCurrentWeather(lat: Double, long: Double, apiKey: String, unit: String, lang: String): Flow<WeatherResponse> {
        return flow {
            while (true){
                val response = retrofit.apiService.getCurrentWeather(lat, long, apiKey, unit, lang)
                emit(response)
                delay(200)
            }
        }
    }

    suspend fun getWeatherForecast(lat:Double, long:Double, apiKey:String,unit:String,lang:String): Flow<WeatherForecastResponse>{
        return flow {
            emit(retrofit.apiService.getWeatherForecast(lat,long,apiKey,unit,lang))
        }
    }


    suspend fun addPlace(place: FavoritePlace):Long{
        return placesLocalDataSource.PlacesDao().addPlace(place)
    }

    suspend fun removePlace(place: FavoritePlace):Int{
        return placesLocalDataSource.PlacesDao().deletePlace(place)
    }

    fun getAllLocalFavoritePlaces():Flow<List<FavoritePlace>>{
        return placesLocalDataSource.PlacesDao().getAllLocalPlaces()
    }
}
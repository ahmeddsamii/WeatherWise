import android.content.Context
import com.example.weatherwise.db.PlacesLocalDataSource
import com.example.weatherwise.model.WeatherForecastResponse
import com.example.weatherwise.network.api.RetrofitHelper
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


    suspend fun getCurrentWeather(lat:Double, long:Double, apiKey:String,unit:String, lang:String): Response<WeatherResponse> {
       return retrofit.apiService.getCurrentWeather(lat,long,apiKey,unit,lang)
    }

    suspend fun getWeatherForecast(lat:Double, long:Double, apiKey:String,unit:String,lang:String): Response<WeatherForecastResponse>{
        return retrofit.apiService.getWeatherForecast(lat,long,apiKey,unit,lang)
    }
}
import com.example.weatherwise.model.WeatherForecastResponse
import com.example.weatherwise.network.api.RetrofitHelper
import retrofit2.Response

class WeatherRepository private constructor() {
    private val apiService = RetrofitHelper.apiService

   companion object{
       private var INSTANCE: WeatherRepository? = null
       fun getInstance():WeatherRepository{
           return INSTANCE?: synchronized(this){
               val instance = WeatherRepository()
                INSTANCE=instance
               instance
           }
       }
   }


    suspend fun getCurrentWeather(lat:Double, long:Double, apiKey:String, lang:String): Response<WeatherResponse> {
       return apiService.getCurrentWeather(lat,long,apiKey, lang = lang)
    }

    suspend fun getWeatherForecast(lat:Double, long:Double, apiKey:String, lang:String): Response<WeatherForecastResponse>{
        return apiService.getWeatherForecast(lat,long,apiKey, lang=lang)
    }
}
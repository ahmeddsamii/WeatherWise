import com.example.weatherwise.db.alertPlaces.AlertDatabaseBuilder
import com.example.weatherwise.db.alertPlaces.AlertLocalDataSource
import com.example.weatherwise.db.alertPlaces.IAlertLocalDataSource
import com.example.weatherwise.db.favoritePlaces.IPlacesLocalDataSource
import com.example.weatherwise.db.favoritePlaces.PlacesLocalDataSource
import com.example.weatherwise.model.AlertDto
import com.example.weatherwise.model.FavoritePlace
import com.example.weatherwise.model.WeatherForecastResponse
import com.example.weatherwise.network.api.IWeatherRemoteDataSource
import com.example.weatherwise.network.api.RetrofitHelper
import com.example.weatherwise.network.api.WeatherRemoteDataSource
import com.example.weatherwise.repositories.IWeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WeatherRepository private constructor(val weatherRemoteDataSource: IWeatherRemoteDataSource, val localDataSource: IPlacesLocalDataSource, val alertLocalDataSource: IAlertLocalDataSource) :
    IWeatherRepository {

   companion object{
       private var INSTANCE: WeatherRepository? = null
       fun getInstance(weatherRemoteDataSource: IWeatherRemoteDataSource, localDataSource: IPlacesLocalDataSource, alertLocalDataSource: IAlertLocalDataSource):WeatherRepository{
           return INSTANCE?: synchronized(this){
               val instance = WeatherRepository(weatherRemoteDataSource,localDataSource, alertLocalDataSource)
                INSTANCE=instance
               instance
           }
       }
   }


    override suspend fun getCurrentWeather(lat: Double, long: Double, apiKey: String, unit: String, lang: String): Flow<WeatherResponse> {
        return flow {
            emit(weatherRemoteDataSource.getCurrentWeather(lat,long,apiKey,unit,lang))
        }
    }

    override suspend fun getWeatherForecast(lat:Double, long:Double, apiKey:String, unit:String, lang:String): Flow<WeatherForecastResponse>{
        return flow {
            emit(weatherRemoteDataSource.getWeatherForecast(lat,long,apiKey,unit,lang))
        }
    }


    override suspend fun addPlace(place: FavoritePlace):Flow<Long>{
        return flow {
            emit( localDataSource.addPlace(place))
        }

    }

    override suspend fun removePlace(place: FavoritePlace):Flow<Int>{
        return flow {
            emit(localDataSource.removePlace(place))
        }

    }

    override fun getAllLocalFavoritePlaces():Flow<List<FavoritePlace>>{
        return flow {
           emit(localDataSource.getAllLocalFavoritePlaces())
        }
    }


    override fun addAlert(alertDto: AlertDto):Flow<Long>{
        return flow {
            emit(alertLocalDataSource.addAlert(alertDto))
        }
    }


    override fun deleteAlert(alertDto: AlertDto):Flow<Int>{
        return flow {
            emit(alertLocalDataSource.deleteAlert(alertDto))
        }
    }


    override fun getLocalAlertsByDate():Flow<List<AlertDto>>{
        return flow {
            emit(alertLocalDataSource.getLocalAlertsByDate())
        }
    }
}
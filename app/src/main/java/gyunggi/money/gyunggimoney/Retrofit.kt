package gyunggi.money.gyunggimoney

import com.example.gyunggimoney.BuildConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

object Retrofits {
    private fun createRetrofit(): Retrofit {

        val gson: Gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder().baseUrl(BuildConfig.BaseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }


    fun getStoreService(): GetStoreService {
        return createRetrofit().create(GetStoreService::class.java)
    }
}

data class Store(
    var location: String,
    var storeName:String,
    var storeLocationDoro:String,
    var storeLocation:String,
    var storeCategory:String,
    var postalCode: String,
    var latitude: String,
    var longitude: String
)

interface GetStoreService {
    @POST(BuildConfig.ServerKey)
    suspend fun getStore(
        @Body params: HashMap<String,Any>
    ): Response<String>
}
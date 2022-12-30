package com.example.gyunggimoney

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

object Retrofits {
    private fun createRetrofit(baseUrl: String): Retrofit {

        val gson: Gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val yongWon = "http://172.30.1.54:8080/"

    fun getStoreService(): GetStoreService {
        return createRetrofit(yongWon).create(GetStoreService::class.java)
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
    @POST("localcurrency/.idea/apis/index.php")
    suspend fun getStore(
        @Body params: HashMap<String,Any>
    ): Response<String>
}
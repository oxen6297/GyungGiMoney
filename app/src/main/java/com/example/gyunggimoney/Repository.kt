package com.example.gyunggimoney

import retrofit2.Call
import retrofit2.Response

class Repository {
    suspend fun getStore(hashMap: HashMap<String,Any>): Response<String> {
        return Retrofits.getStoreService().getStore(hashMap)
    }
}
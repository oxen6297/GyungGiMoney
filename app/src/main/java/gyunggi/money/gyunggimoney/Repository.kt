package gyunggi.money.gyunggimoney

import retrofit2.Response

class Repository {
    suspend fun getStore(hashMap: HashMap<String,Any>): Response<String> {
        return Retrofits.getStoreService().getStore(hashMap)
    }
}
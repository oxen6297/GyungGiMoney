package com.example.gyunggimoney

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class MainViewModel(private val repository: Repository) : ViewModel() {

    private val _storeResponse: MutableLiveData<Response<String>> = MutableLiveData()
    val storeResponse: LiveData<Response<String>>
        get() = _storeResponse

    val spinnerItem: MutableLiveData<String> = MutableLiveData()

    fun getStore(hashMap: HashMap<String,Any>,context: Context,dialog: LoadingDialog) {
        dialog.show()
        viewModelScope.launch {
            try {
                val response = repository.getStore(hashMap)
                _storeResponse.value = response
            } catch (e: Exception) {
                Log.d("getStoreError", e.toString())
                Toast.makeText(context,"불러오기에 실패하였습니다. 잠시 후 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
            }
        }
    }
}
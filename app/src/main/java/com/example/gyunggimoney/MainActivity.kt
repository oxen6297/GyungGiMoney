package com.example.gyunggimoney

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.gyunggimoney.databinding.ActivityMainBinding
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels { MainViewModelFactory(Repository()) }
    private var mBackWait: Long = 0
    private val storeList = mutableListOf<Store>()
    private val recyclerViewAdapter = RecyclerViewAdapter(storeList)
    private lateinit var dialog: LoadingDialog
    private var mInterstitialAd: InterstitialAd? = null


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.vm = mainViewModel
        binding.lifecycleOwner = this
        setSpinner()

        MobileAds.initialize(this)
        dialog = LoadingDialog(this)

        MobileAds.initialize(this)
        binding.adView.loadAd(AdRequest.Builder().build())

        val adRequest = AdRequest.Builder().build()


        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d("TAG", "Ad was dismissed.")
            }

            override fun onAdShowedFullScreenContent() {
                Log.d("TAG", "Ad showed fullscreen content.")
                mInterstitialAd = null
            }
        }


        binding.recyclerview.setHasFixedSize(true)
        binding.recyclerview.adapter = recyclerViewAdapter

        val spLocationPosition: Int = SharedPreferencesManager.getInt(this, "location", 0)
        binding.siGunGu.setSelection(spLocationPosition)

        binding.run {
            siGunGu.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    mainViewModel.spinnerItem.value = siGunGu.selectedItem.toString()

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

            saveBtn.setOnClickListener {

                checkBoxChecked(siGunGu.selectedItemPosition)
                getSoreList(dialog)
                storeList.clear()
            }

            binding.swipeRefresh.setOnRefreshListener {
                binding.swipeRefresh.isRefreshing = false
                getSoreList(dialog)
            }

            recyclerViewAdapter.setItemClickListener(object :
                RecyclerViewAdapter.OnItemClickListener {
                override fun onClick(v: View, position: Int) {
                    InterstitialAd.load(
                        this@MainActivity,
                        "ca-app-pub-3940256099942544/1033173712",
                        adRequest,
                        object : InterstitialAdLoadCallback() {
                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                mInterstitialAd = null
                                Log.d("fullAdError",adError.toString())
                            }

                            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                mInterstitialAd = interstitialAd
                                interstitialAd.show(this@MainActivity)
                            }
                        })

                    val intent = Intent(this@MainActivity, DetailStoreActivity::class.java)
                    intent.putExtra("longitude", storeList[position].longitude)
                    intent.putExtra("latitude", storeList[position].latitude)
                    intent.putExtra("storeName", storeList[position].storeName)
                    startActivity(intent)
                }
            })
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getSoreList(dialog: LoadingDialog) {
        val areaHash = HashMap<String, Any>()
        areaHash["SIGUNNM"] = mainViewModel.spinnerItem.value.toString()
        if (mainViewModel.spinnerItem.value.toString() == "시/군/구") {
            Toast.makeText(this, "지역을 선택해주세요", Toast.LENGTH_SHORT).show()
        } else {
            mainViewModel.storeResponse.observe(this@MainActivity) {
                if (it.isSuccessful) {

                    val res = it.body()
                    val storeInfo = JSONObject(res.toString())
                    val storeArray = storeInfo.optJSONArray("storeList")
                    var i = 0

                    if (storeArray != null) {
                        while (i < storeArray.length()) {
                            val jsonObject = storeArray.getJSONObject(i)
                            val storeName = jsonObject.getString("CMPNMNM")
                            val storeCategory = jsonObject.getString("INDUTYPENM")
                            val storeLocationDoro = jsonObject.getString("REFINEROADNMADDR")
                            val storeLocation = jsonObject.getString("REFINELOTNOADDR")
                            val location = jsonObject.getString("SIGUNNM")
                            val postalCode = jsonObject.getString("POSTALCODE")
                            val latitude = jsonObject.getString("LATITUDE")
                            val longitude = jsonObject.getString("LONGITUDE")

                            storeList.add(
                                Store(
                                    location,
                                    storeName,
                                    storeLocationDoro,
                                    storeLocation,
                                    storeCategory,
                                    postalCode,
                                    latitude,
                                    longitude
                                )
                            )
                            i++
                        }
                    }
                    recyclerViewAdapter.notifyDataSetChanged()
                }
                dialog.dismiss()
            }
            mainViewModel.getStore(areaHash, this@MainActivity, dialog)
        }
    }

    private fun setSpinner() {
        val areaAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.gyungiList,
            R.layout.spinner_drop_down_item
        )
        areaAdapter.setDropDownViewResource(R.layout.spinner_item_style)
        binding.siGunGu.adapter = areaAdapter
    }

    @SuppressLint("CommitPrefEdits")
    private fun checkBoxChecked(position: Int) {
        if (binding.saveCheckBox.isChecked) {
            SharedPreferencesManager.putInt(this, "location", position)
        }
    }

    override fun onBackPressed() {
        // 뒤로가기 버튼 클릭
        if (System.currentTimeMillis() - mBackWait >= 2000) {
            mBackWait = System.currentTimeMillis()
            Toast.makeText(this, "뒤로가기를 한번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
        } else {
            finishAffinity()
        }
    }
}
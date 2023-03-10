package gyunggi.money.gyunggimoney

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gyunggimoney.BuildConfig
import com.example.gyunggimoney.R
import com.example.gyunggimoney.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels { MainViewModelFactory(Repository()) }
    private var mBackWait: Long = 0
    private var storeList = mutableListOf<Store>()
    private var recyclerViewAdapter = RecyclerViewAdapter(storeList)
    private lateinit var dialog: LoadingDialog
    private var mInterstitialAd: InterstitialAd? = null

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.vm = mainViewModel
        binding.lifecycleOwner = this
        setSpinner()

        val linearLayoutManagerWrapper =
            LinearLayoutManagerWrapper(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerview.layoutManager = linearLayoutManagerWrapper

        MobileAds.initialize(this)
        dialog = LoadingDialog(this)

        MobileAds.initialize(this)
        binding.adView.loadAd(AdRequest.Builder().build())

        binding.recyclerview.adapter = recyclerViewAdapter

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

        val spLocationPosition: Int = SharedPreferencesManager.getInt(this, "location", 0)
        binding.siGunGu.setSelection(spLocationPosition)

        binding.run {
            shopSearchBtn.setOnClickListener {
                searchEdit.text.clear()
            }
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
                if (!NetworkStateManager.checkNetworkState(this@MainActivity)) {
                    Log.d("networkState", "not access to Network")
                    Toast.makeText(this@MainActivity, "????????? ?????? ????????? ??????????????????", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    checkBoxChecked(siGunGu.selectedItemPosition)
                    getSoreList(dialog)
                    searchEdit.text.clear()
                }
            }

            recyclerViewAdapter.setItemClickListener(object :
                RecyclerViewAdapter.OnItemClickListener {
                override fun onClick(v: View, position: Int) {
                    InterstitialAd.load(
                        this@MainActivity,
                        BuildConfig.AD_API_KEY,
                        adRequest,
                        object : InterstitialAdLoadCallback() {
                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                mInterstitialAd = null
                                Log.d("fullAdError", adError.toString())
                            }

                            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                mInterstitialAd = interstitialAd
                                interstitialAd.show(this@MainActivity)
                            }
                        })
                    val intent = Intent(this@MainActivity, DetailStoreActivity::class.java)
                    intent.putExtra(
                        "longitude",
                        recyclerViewAdapter.storeList!![position].longitude
                    )
                    intent.putExtra("latitude", recyclerViewAdapter.storeList!![position].latitude)
                    intent.putExtra(
                        "storeName",
                        recyclerViewAdapter.storeList!![position].storeName
                    )
                    startActivity(intent)
                }
            })

            searchEdit.doAfterTextChanged {
                try {
                    recyclerViewAdapter.filter.filter(it.toString())
                } catch (e: Exception) {
                    Toast.makeText(
                        this@MainActivity,
                        "????????? ????????????????????? ?????? ??????????????????.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getSoreList(dialog: LoadingDialog) {
        val areaHash = HashMap<String, Any>()
        areaHash["SIGUNNM"] = mainViewModel.spinnerItem.value.toString()
        if (mainViewModel.spinnerItem.value.toString() == "???/???/???") {
            Toast.makeText(this, "????????? ??????????????????", Toast.LENGTH_SHORT).show()
        } else {
            mainViewModel.storeResponse.observe(this@MainActivity) {
                if (it.isSuccessful) {

                    val res = it.body()
                    val storeInfo = JSONObject(res.toString())
                    val storeArray = storeInfo.optJSONArray("storeList")
                    var i = 0

                    if (storeArray != null) {
                        storeList.clear()
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
                }
                recyclerViewAdapter.notifyDataSetChanged()
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
        // ???????????? ?????? ??????
        if (System.currentTimeMillis() - mBackWait >= 2000) {
            mBackWait = System.currentTimeMillis()
            Toast.makeText(this, "??????????????? ?????? ??? ????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show()
        } else {
            finishAffinity()
        }
    }

    class LinearLayoutManagerWrapper : LinearLayoutManager {
        constructor(context: Context) : super(context)

        constructor(context: Context, orientation: Int, reverseLayout: Boolean) : super(
            context,
            orientation,
            reverseLayout
        ) {
        }

        constructor(
            context: Context,
            attrs: AttributeSet,
            defStyleAttr: Int,
            defStyleRes: Int
        ) : super(context, attrs, defStyleAttr, defStyleRes) {
        }

        override fun supportsPredictiveItemAnimations(): Boolean {
            return false
        }
    }

}
package com.example.gyunggimoney

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class DetailStoreActivity : AppCompatActivity(),OnMapReadyCallback {
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_store)
        val mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(p0: GoogleMap) {

        val latitude = intent.getStringExtra("latitude").toString().toDouble()
        val longitude = intent.getStringExtra("longitude").toString().toDouble()
        val storeName = intent.getStringExtra("storeName").toString()

        mMap = p0
        val marker = LatLng(latitude, longitude)
        val position = CameraPosition.builder().target(marker).zoom(18f).build()
        mMap.addMarker(MarkerOptions().position(marker).title(storeName))?.showInfoWindow()
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker))
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position))
    }
}
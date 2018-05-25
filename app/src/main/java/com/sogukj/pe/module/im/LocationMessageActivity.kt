package com.sogukj.pe.module.im

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import kotlinx.android.synthetic.main.activity_location_message.*
import org.jetbrains.anko.find

class LocationMessageActivity : BaseActivity(), View.OnClickListener {
    private var msgLatitude = -1.0
    private var msgLongitude = -1.0
    private var msgAddress = ""
    private lateinit var map: AMap

    companion object {
        fun start(context: Context, latitude: Double, longitude: Double, address: String) {
            val intent = Intent(context, LocationMessageActivity::class.java)
            intent.putExtra(LocationExtras.LATITUDE, latitude)
            intent.putExtra(LocationExtras.LONGITUDE, longitude)
            intent.putExtra(LocationExtras.ADDRESS, address)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_message)
        Utils.setWindowStatusBarColor(this, R.color.white)
        mAddressMap.onCreate(savedInstanceState)
        msgLatitude = intent.getDoubleExtra(LocationExtras.LATITUDE, -1.0)
        msgLongitude = intent.getDoubleExtra(LocationExtras.LONGITUDE, -1.0)
        msgAddress = intent.getStringExtra(LocationExtras.ADDRESS)
        initMap()
        locationBtn.setOnClickListener(this)
        back.setOnClickListener(this)
    }

    private fun initMap() {
        map = mAddressMap.map
        val uiSettings = map.uiSettings
        uiSettings.isZoomControlsEnabled = false
        uiSettings.isMyLocationButtonEnabled = false
        uiSettings.logoPosition = AMapOptions.LOGO_POSITION_BOTTOM_RIGHT

        val options = MarkerOptions()
        options.position(LatLng(msgLatitude, msgLongitude))
                .title(msgAddress)
                .draggable(false)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location_blue_point))
                .setFlat(true)
                .visible(true)
                .infoWindowEnable(true)
        map.setInfoWindowAdapter(object :AMap.InfoWindowAdapter{
            override fun getInfoContents(p0: Marker?): View {
                val view = layoutInflater.inflate(R.layout.layout_map_infowindow, null)
                view.find<TextView>(R.id.address).text = p0?.title
                return view
            }

            override fun getInfoWindow(p0: Marker?): View? {
                return null
            }
        })
        val marker = map.addMarker(options)
        marker.showInfoWindow()
        val latlng = LatLng(msgLatitude, msgLongitude)
        val camera = CameraUpdateFactory.newCameraPosition(CameraPosition(latlng, 17f, 0f, 0f))
        map.animateCamera(camera)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.locationBtn->{
                val latlng = LatLng(msgLatitude, msgLongitude)
                val camera = CameraUpdateFactory.newCameraPosition(CameraPosition(latlng, map.cameraPosition.zoom, 0f, 0f))
                map.animateCamera(camera)
            }
            R.id.back->{
                finish()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        mAddressMap.onResume()
    }

    override fun onPause() {
        super.onPause()
        mAddressMap.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mAddressMap.onDestroy()
    }
}

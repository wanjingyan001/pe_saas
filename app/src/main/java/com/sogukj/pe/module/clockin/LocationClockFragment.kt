package com.sogukj.pe.module.clockin

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult

import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_location_clock.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.ctx

class LocationClockFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_location_clock

    private lateinit var map: AMap
    private lateinit var mLocationClient: AMapLocationClient
    private lateinit var mLocationOption: AMapLocationClientOption
    //逆地理编码（坐标转地址）
    private lateinit var geocoderSearch: GeocodeSearch

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        waichudaka.setOnClickListener {
            var mDialog = MaterialDialog.Builder(ctx)
                    .theme(Theme.LIGHT)
                    .canceledOnTouchOutside(true)
                    .customView(R.layout.dialog_clock, false).build()
            mDialog.show()
            //mDialog.getWindow().setDimAmount(1f)

            val tvAddr = mDialog.find<TextView>(R.id.address)
            val tvCancel = mDialog.find<TextView>(R.id.cancel)
            val tvConfirm = mDialog.find<TextView>(R.id.confirm)
            tvCancel.setOnClickListener {
                if (mLocationClient != null && mLocationClient.isStarted) {
                    mLocationClient.stopLocation()
                }
                mDialog.dismiss()
            }
            tvConfirm.setOnClickListener {

            }

            // 以下是高德地图定位
            kotlin.run {
                val imMap = mDialog.find<MapView>(R.id.imMap)
                imMap.onCreate(savedInstanceState)
                map = imMap.map
                map.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
                    override fun onCameraChangeFinish(position: CameraPosition?) {
                        position?.let {
                            val query = RegeocodeQuery(LatLonPoint(it.target.latitude, it.target.longitude), 200f, GeocodeSearch.AMAP)
                            geocoderSearch.getFromLocationAsyn(query)
                        }
                    }

                    override fun onCameraChange(position: CameraPosition?) {
                    }
                })
                val uiSettings = map.uiSettings
                uiSettings.isZoomControlsEnabled = false
                uiSettings.isMyLocationButtonEnabled = false
                uiSettings.logoPosition = AMapOptions.LOGO_POSITION_BOTTOM_RIGHT

                geocoderSearch = GeocodeSearch(ctx)
                geocoderSearch.setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {
                    // 获取坐标转地址的解析结果
                    override fun onRegeocodeSearched(result: RegeocodeResult?, rCode: Int) {
                        //解析result获取地址描述信息
                        if (rCode == 1000) {
                            result?.let {
                                val address = it.regeocodeAddress
                                address.apply {
                                }
                            }
                        }
                    }

                    override fun onGeocodeSearched(p0: GeocodeResult?, p1: Int) {
                    }
                })

                mLocationClient = AMapLocationClient(baseActivity!!.application)
                mLocationClient.setLocationListener(object : AMapLocationListener {
                    override fun onLocationChanged(location: AMapLocation?) {
                        if (location != null) {
                            if (location.errorCode == 0) {
//                info {
//                    "定位结果来源:${location.locationType}\n" +
//                            "经度:${location.longitude}\n" +
//                            "纬度:${location.latitude}\n" +
//                            "精度信息:${location.accuracy}\n" +
//                            "地址:${location.address}\n" +
//                            "国家:${location.country}\n" +
//                            "省:${location.province}\n" +
//                            "城市:${location.city}\n" +
//                            "AOI信息:${location.aoiName}\n" +
//                            "建筑物Id:${location.buildingId}\n" +
//                            "楼层:${location.floor}\n" +
//                            "GPS的当前状态:${location.gpsAccuracyStatus}"
//
//                }
                                val latlng = LatLng(location.latitude, location.longitude)
                                val camera = CameraUpdateFactory.newCameraPosition(CameraPosition(latlng, 17f, 0f, 0f))
                                map.animateCamera(camera)

                                // 设置当前地图显示为当前位置
                                var markerOptions = MarkerOptions()
                                markerOptions.position(LatLng(location.latitude, location.longitude))
                                markerOptions.title("当前位置")
                                markerOptions.visible(true)
                                var bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon_location_blue_point))
                                markerOptions.icon(bitmapDescriptor)
                                map.addMarker(markerOptions)

                                tvAddr.text = location.address
                            } else {
                                AnkoLogger("AmapError").error {
                                    "location Error, ErrCode: ${location.errorCode}, errInfo:${location.errorInfo}"
                                }
                            }
                        }
                    }
                })
                mLocationOption = AMapLocationClientOption()
                //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。4
                mLocationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                //单次定位
                mLocationOption.isOnceLocation = true
                //设置是否返回地址信息（默认返回地址信息）
                mLocationOption.isNeedAddress = true
                //设置是否允许模拟位置,默认为true，允许模拟位置
                mLocationOption.isMockEnable = false
                //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
                mLocationOption.httpTimeOut = 20000
                mLocationClient.setLocationOption(mLocationOption)
                //启动定位
                mLocationClient.startLocation()
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

    }
}

package com.sogukj.pe.module.im

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.core.ServiceSettings
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.bumptech.glide.Glide
import com.netease.nim.uikit.api.model.location.LocationProvider
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import kotlinx.android.synthetic.main.activity_imlocation.*
import org.jetbrains.anko.*


class IMLocationActivity : BaseActivity(), AMap.OnCameraChangeListener, AMapLocationListener, View.OnClickListener, GeocodeSearch.OnGeocodeSearchListener, PoiSearch.OnPoiSearchListener {
    private lateinit var map: AMap
    //声明AMapLocationClient对象
    private lateinit var mLocationClient: AMapLocationClient
    //声明AMapLocationClientOption对象
    private lateinit var mLocationOption: AMapLocationClientOption
    //定位蓝点
    private lateinit var myLocationStyle: MyLocationStyle
    //逆地理编码（坐标转地址）
    private lateinit var geocoderSearch: GeocodeSearch
    //我所在的位置
    private var cacheLatitude = -1.0
    private var cacheLongitude = -1.0
    //当前地图中心点位置
    private var currentLatitude = -1.0
    private var currentLongitude = -1.0
    //当前地址
    private var currentAddress = ""
    //当前城市(用于POI搜索)
    private var currentCity: String? = null
    private var page = 0
    //用于判断是用户手动滑动了地图还是通过点击poi列表移动了地图
    private var isUserTouch = true

    private var currentPoiPosition = 0

    private lateinit var poiAdapter: RecyclerAdapter<PoiItem>

    companion object {
        lateinit var callback: LocationProvider.Callback
        private val deepType = "汽车服务,汽车销售,汽车维修,餐饮服务,购物服务,生活服务,体育休闲服务,医疗保健服务,住宿服务,风景名胜,商务住宅,政府机构及社会团体,科教文化服务,交通设施服务,金融保险服务,公司企业,公共设施"
        fun start(context: Context, callback: LocationProvider.Callback) {
            this.callback = callback
            context.startActivity(Intent(context, IMLocationActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imlocation)
        Utils.setWindowStatusBarColor(this, R.color.white)
        imMap.onCreate(savedInstanceState)
        initMap()
        initLocation()
        initGeocode()
        initPoiList()
        locationBtn.setOnClickListener(this)
        search.setOnClickListener(this)
        confirm.setOnClickListener(this)
        back.setOnClickListener(this)
        confirm.isEnabled = false
    }

    private fun initMap() {
        map = imMap.map
        map.setOnCameraChangeListener(this)
        val uiSettings = map.uiSettings
        uiSettings.isZoomControlsEnabled = false
        uiSettings.isMyLocationButtonEnabled = false
        uiSettings.logoPosition = AMapOptions.LOGO_POSITION_BOTTOM_RIGHT
        ServiceSettings.getInstance().language = ServiceSettings.CHINESE
        map.setOnMapTouchListener {
            isUserTouch = true
        }
    }
//      定位自己的位置
//    private fun initLocationStyle() {
//        myLocationStyle = MyLocationStyle()
//        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
//        myLocationStyle.anchor(cacheLatitude.toFloat(), cacheLongitude.toFloat())
//        myLocationStyle.showMyLocation(false)
//        map.myLocationStyle = myLocationStyle
//        map.isMyLocationEnabled = true
//        map.setOnMyLocationChangeListener(this)
//    }

    private fun initLocation() {
        mLocationClient = AMapLocationClient(application)
        mLocationClient.setLocationListener(this)
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

    private fun initGeocode() {
        geocoderSearch = GeocodeSearch(this)
        geocoderSearch.setOnGeocodeSearchListener(this)
    }

    private fun initPoiList() {
        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        iv_loading?.visibility = View.VISIBLE

        poiAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            val itemView = _adapter.getView(R.layout.item_poi_list, parent)
            object : RecyclerHolder<PoiItem>(itemView) {
                val currentFlag = itemView.find<ImageView>(R.id.currentLocation)
                val poiName = itemView.find<TextView>(R.id.poiName)
                val poiAddress = itemView.find<TextView>(R.id.poiAddress)
                override fun setData(view: View, data: PoiItem, position: Int) {
                    if (currentPoiPosition == position) {
                        currentFlag.visibility = View.VISIBLE
                    } else {
                        currentFlag.visibility = View.INVISIBLE
                    }
                    if (0 == position){
                        poiName.text = "当前位置"
                    }else{
                        poiName.text = data.title
                    }
                    poiAddress.text = "${data.cityName}${data.adName}${data.snippet}"
                }
            }
        }
        poiAdapter.onItemClick = { v, position ->
            isUserTouch = false
            currentPoiPosition = position
            poiAdapter.notifyDataSetChanged()
            val poiItem = poiAdapter.dataList[position]
            currentLatitude = poiItem.latLonPoint.latitude
            currentLongitude = poiItem.latLonPoint.longitude
            currentAddress = "${poiItem.cityName}${poiItem.adName}${poiItem.title}"
            val camera = CameraUpdateFactory.newCameraPosition(
                    CameraPosition(
                            LatLng(currentLatitude, currentLongitude),
                            map.cameraPosition.zoom, 0f, 0f
                    )
            )
            map.animateCamera(camera)
        }
        refresh.apply {
            isEnableRefresh = false
            isEnableLoadMore = true
            isEnableAutoLoadMore = true
            isEnableOverScrollBounce = false
            isEnableScrollContentWhenLoaded = false
            setEnableFooterTranslationContent(false)
            setRefreshFooter(ClassicsFooter(ctx), 0, 0)
            setDisableContentWhenLoading(true)
            setOnLoadMoreListener {
                page += 1
                iv_loading?.visibility = View.VISIBLE
                searchPoi()
            }
        }
        poiList.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = poiAdapter
            addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
        }
    }


    override fun onResume() {
        super.onResume()
        imMap.onResume()
    }

    override fun onPause() {
        super.onPause()
        imMap.onPause()
    }

    override fun onStop() {
        super.onStop()
        mLocationClient.stopLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        imMap.onDestroy()
        mLocationClient.onDestroy()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.locationBtn -> {
                isUserTouch = true
                currentPoiPosition = 0
                val latlng = LatLng(cacheLatitude, cacheLongitude)
                val camera = CameraUpdateFactory.newCameraPosition(CameraPosition(latlng, map.cameraPosition.zoom, 0f, 0f))
                map.animateCamera(camera)
            }
            R.id.confirm -> {
//                saveLocationImage()
                currentAddress = if (currentAddress.isEmpty()) "该位置信息暂无" else currentAddress
                callback.onSuccess(currentLongitude, currentLatitude, currentAddress)
                finish()
            }
            R.id.search -> {
                currentCity?.let {
                    AddressSearchActivity.start(this, it)
                }
            }
            R.id.back -> {
                finish()
            }
        }
    }

    private fun saveLocationImage() {
        if (null != imMap){
            imMap.map.getMapScreenShot(object : AMap.OnMapScreenShotListener{
                override fun onMapScreenShot(p0: Bitmap?) {

                }

                override fun onMapScreenShot(p0: Bitmap?, p1: Int) {
                    Utils.saveImageFromMap(p0,"pe_img_location.jpg",this@IMLocationActivity,p1)
                }

            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && resultCode == Extras.RESULTCODE && data != null) {
            val poiItem = data.getParcelableExtra<PoiItem>(Extras.DATA)
            currentPoiPosition = 0
            currentLatitude = poiItem.latLonPoint.latitude
            currentLongitude = poiItem.latLonPoint.longitude
            currentAddress = "${poiItem.cityName}${poiItem.adName}${poiItem.title}"
            val latlng = LatLng(currentLatitude, currentLongitude)
            val camera = CameraUpdateFactory.newCameraPosition(CameraPosition(latlng, map.cameraPosition.zoom, 0f, 0f))
            map.animateCamera(camera)
            searchPoi()
        }
    }

    /**
     * 地图滑动切换位置后,得到新的位置
     */
    override fun onCameraChangeFinish(p0: CameraPosition?) {
        p0?.let {
            //手动更换位置后,page置为0
            if (isUserTouch) {
                page = 0
                currentPoiPosition = 0
                poiList.smoothScrollToPosition(0)
            }
            currentLatitude = it.target.latitude
            currentLongitude = it.target.longitude
            searchGeoCode(it.target)
        }
    }

    override fun onCameraChange(p0: CameraPosition?) {
    }


    /**
     * 获取定位结果
     */
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
                confirm.isEnabled = true
                cacheLatitude = location.latitude
                cacheLongitude = location.longitude
                val latlng = LatLng(location.latitude, location.longitude)
                val camera = CameraUpdateFactory.newCameraPosition(CameraPosition(latlng, 17f, 0f, 0f))
                map.animateCamera(camera)
            } else {
                AnkoLogger("AmapError").error {
                    "location Error, ErrCode: ${location.errorCode}, errInfo:${location.errorInfo}"
                }
            }
        }
    }


    private fun searchGeoCode(latlng: LatLng) {
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是高德坐标系还是GPS原生坐标系
        val query = RegeocodeQuery(LatLonPoint(latlng.latitude, latlng.longitude), 200f, GeocodeSearch.AMAP)
        geocoderSearch.getFromLocationAsyn(query)
    }

    private fun getPoiQuery(city: String, page: Int): PoiSearch.Query {
        val query = PoiSearch.Query("", deepType, city)
        query.pageSize = 10
        query.pageNum = page
        return query
    }

    private fun searchPoi() {
        currentCity?.let {
            val search = PoiSearch(this, getPoiQuery(it, page))
            search.bound = PoiSearch.SearchBound(LatLonPoint(currentLatitude, currentLongitude), 200)
            search.setOnPoiSearchListener(this)
            search.searchPOIAsyn()
        }
    }

    /**
     * 获取坐标转地址的解析结果
     */
    override fun onRegeocodeSearched(result: RegeocodeResult?, rCode: Int) {
        //解析result获取地址描述信息
        if (rCode == 1000) {
            result?.let {
                val address = it.regeocodeAddress
                address.apply {
                    currentCity = city
                    currentAddress = formatAddress
                    if (isUserTouch) {
                        searchPoi()
                    }
                }
            }
        }
    }

    override fun onGeocodeSearched(p0: GeocodeResult?, p1: Int) {
    }

    override fun onPoiItemSearched(p0: PoiItem?, p1: Int) {

    }

    override fun onPoiSearched(result: PoiResult?, rCode: Int) {
        //解析result获取POI信息
        if (rCode == 1000) {
            result?.let {
                it.pois.forEach {
                    info { "名称:${it.title}===>地址:${it.snippet}" }
                }
                refresh.finishLoadMore()
                if (page == 0) {
                    poiAdapter.dataList.clear()
                }
                poiAdapter.dataList.addAll(it.pois)
                poiAdapter.notifyDataSetChanged()
                iv_loading.visibility = View.INVISIBLE
            }
        }
    }
}

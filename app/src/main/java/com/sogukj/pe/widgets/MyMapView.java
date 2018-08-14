package com.sogukj.pe.widgets;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.bigkoo.pickerview.utils.PickerViewAnimateUtil;
import com.sogukj.pe.R;
import com.sogukj.pe.baselibrary.utils.DateUtils;
import com.sogukj.pe.bean.LocationRecordBean;
import com.sogukj.pe.module.clockin.LocationActivity;
import com.sogukj.pe.service.ApproveService;
import com.sogukj.pe.service.Payload;
import com.sogukj.service.SoguApi;

import org.angmarch.views.NiceSpinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MyMapView extends View {

    private ViewGroup decorView;
    private ViewGroup rootView;
    private Context mContext;
    private AMap map;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    //逆地理编码（坐标转地址）
    private GeocodeSearch geocoderSearch;

    private TextView tvAddr, tvCancel, tvConfirm;

    private NiceSpinner niceSpinner;

    public MyMapView(Context context) {
        super(context);
        mContext = context;
    }

    public MyMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public MyMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    private void init(Bundle savedInstanceState) {
        if (decorView == null) {
            decorView = ((Activity) mContext).getWindow().getDecorView().findViewById(android.R.id.content);
        }
        rootView = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.layout_mapview, decorView, false);
        rootView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rootView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        tvAddr = rootView.findViewById(R.id.address);
        tvCancel = rootView.findViewById(R.id.cancel);
        tvConfirm = rootView.findViewById(R.id.confirm);

        niceSpinner = rootView.findViewById(R.id.nice_spinner);
        LocationRecordBean.LocationCellBean bean = new LocationRecordBean.LocationCellBean();
        bean.setId(0);
        bean.setTitle("空");
        bean.setTime("");
        mList.add(0, bean);
        ArrayList<String> dstList = new ArrayList<>();
        for (int i = 0; i < mList.size(); i++) {
            LocationRecordBean.LocationCellBean cell = mList.get(i);
            try {
                dstList.add(cell.getAdd_time().split(" ")[0] + "  " + cell.getTitle());
            } catch (Exception e) {
                // 第一个cell  addtime是null
                dstList.add(cell.getTitle());
            }
        }
        niceSpinner.attachDataSource(dstList);
        niceSpinner.setSelectedIndex(0);
        niceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //LocationRecordBean.LocationCellBean cell = mList.get(position);
                //Log.e("onItemSelected", cell.getTitle());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tvCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLocationClient != null && mLocationClient.isStarted()) {
                    mLocationClient.stopLocation();
                }
                dismiss(false);
            }
        });
        tvConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                locationDaKa();
            }
        });

        // 以下是高德地图定位
        MapView imMap = rootView.findViewById(R.id.imMap);
        imMap.onCreate(savedInstanceState);
        map = imMap.getMap();
        map.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(cameraPosition.target.latitude, cameraPosition.target.longitude), 200f, GeocodeSearch.AMAP);
                geocoderSearch.getFromLocationAsyn(query);
            }
        });
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setMyLocationButtonEnabled(false);
        //uiSettings.logoPosition = AMapOptions.LOGO_POSITION_BOTTOM_RIGHT

        geocoderSearch = new GeocodeSearch(mContext);
        geocoderSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                // 获取坐标转地址的解析结果
                if (i == 1000) {
                    //解析result获取地址描述信息
                }
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
            }
        });

        mLocationClient = new AMapLocationClient(mContext);
        mLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation location) {
                if (location != null) {
                    if (location.getErrorCode() == 0) {
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
//                      }
                        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
                        CameraUpdate camera = CameraUpdateFactory.newCameraPosition(new CameraPosition(latlng, 17f, 0f, 0f));
                        map.animateCamera(camera);

                        // 设置当前地图显示为当前位置
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
                        markerOptions.title("当前位置");
                        markerOptions.visible(true);
                        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon_location_blue_point));
                        markerOptions.icon(bitmapDescriptor);
                        map.addMarker(markerOptions);

                        mLocation = location;

                        tvAddr.setText(location.getAddress());
                    }
                }
            }
        });
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。4
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //单次定位
        mLocationOption.setOnceLocation(true);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否允许模拟位置,默认为true，允许模拟位置
        mLocationOption.setMockEnable(false);
        //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        mLocationOption.setHttpTimeOut(20000);
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    public Animation getInAnimation() {
        int res = PickerViewAnimateUtil.getAnimationResource(Gravity.BOTTOM, true);
        return AnimationUtils.loadAnimation(mContext, res);
    }

    public Animation getOutAnimation() {
        int res = PickerViewAnimateUtil.getAnimationResource(Gravity.BOTTOM, false);
        return AnimationUtils.loadAnimation(mContext, res);
    }

    public void show(Bundle mBundle, ArrayList<LocationRecordBean.LocationCellBean> list, onFinishListener mListener) {
        this.mListener = mListener;
        mList = new ArrayList<>(list);
        init(mBundle);
        if (rootView.getParent() == null) {
            decorView.addView(rootView);
            rootView.startAnimation(getInAnimation());
        }
    }

    private onFinishListener mListener;
    private ArrayList<LocationRecordBean.LocationCellBean> mList;

    private void dismiss(final boolean isRefresh) {
        if (rootView.getParent() != null) {
            Animation out = getOutAnimation();
            out.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    decorView.removeView(rootView);
                    if (isRefresh) {
                        if (mListener != null) {
                            mListener.onFinish();
                        }
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            rootView.startAnimation(out);
        }
    }

    private AMapLocation mLocation;

    private void locationDaKa() {
        LocationRecordBean.LocationCellBean cell = mList.get(niceSpinner.getSelectedIndex());
        Log.e("onItemSelected", cell.getTitle());
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        final String dateStr = format.format(new Date());
        int stamp = Integer.parseInt(DateUtils.getTimestamp(dateStr, "yyyy/MM/dd HH:mm:ss"));
        SoguApi.Companion.getService(((Activity) mContext).getApplication(), ApproveService.class)
                .outCardSubmit(stamp, tvAddr.getText().toString(), mLocation.getLongitude() + "", mLocation.getLatitude() + "", cell.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Payload<Object>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Payload<Object> listPayload) {
                        View inflate = LayoutInflater.from(mContext).inflate(R.layout.layout_locate_success, null);
                        final MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                                .customView(inflate, false)
                                .cancelable(true)
                                .canceledOnTouchOutside(true)
                                .build();
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.show();

                        TextView mTvTime = (TextView) dialog.findViewById(R.id.time);
                        TextView mTvLocate = (TextView) dialog.findViewById(R.id.locate);
                        TextView tvCancel = (TextView) dialog.findViewById(R.id.cancel);
                        TextView tvConfirm = (TextView) dialog.findViewById(R.id.confirm);

                        mTvTime.setText(dateStr.split(" ")[1].substring(0, 5));
                        mTvLocate.setText(mLocation.getAddress());
                        tvCancel.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        tvConfirm.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                dismiss(true);
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        ((LocationActivity) mContext).showCustomToast(R.drawable.icon_toast_fail, "网络请求出错，无法定位打卡");
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public interface onFinishListener {
        void onFinish();
    }
}

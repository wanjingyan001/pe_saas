package com.sogukj.pe.module.im;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.netease.nim.uikit.api.model.location.LocationProvider;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.netease.nim.uikit.common.util.log.LogUtil;

/**
 *
 * @author admin
 * @date 2018/2/6
 */
public class NimDemoLocationProvider implements LocationProvider {
    @Override
    public void requestLocation(final Context context, Callback callback) {
        if (!NimLocationManager.isLocationEnable(context)) {
            final EasyAlertDialog alertDialog = new EasyAlertDialog(context);
            alertDialog.setMessage("位置服务未开启");
            alertDialog.addNegativeButton("取消", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                    new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.addPositiveButton("设置", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                    new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            try {
                                context.startActivity(intent);
                            } catch (Exception e) {
                                LogUtil.e("LOC", "start ACTION_LOCATION_SOURCE_SETTINGS error");
                            }

                        }
                    });
            alertDialog.show();
            return;
        }
        IMLocationActivity.Companion.start(context,callback);
    }

    @Override
    public void openMap(Context context, double longitude, double latitude, String address) {
        LocationMessageActivity.Companion.start(context, latitude, longitude, address);
    }
}
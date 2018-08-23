package com.netease.nim.uikit.business.session.viewholder;

import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.ui.imageview.MsgThumbImageView;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.common.util.media.ImageUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.msg.attachment.LocationAttachment;

/**
 * Created by zhoujianghua on 2015/8/7.
 */
public class MsgViewHolderLocation extends MsgViewHolderBase {

    public MsgViewHolderLocation(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    public MsgThumbImageView mapView;

    public TextView addressText;

    @Override
    protected int getContentResId() {
        return R.layout.nim_message_item_location;
    }

    @Override
    protected void inflateContentView() {
        mapView = (MsgThumbImageView) view.findViewById(R.id.message_item_location_image);
        addressText = (TextView) view.findViewById(R.id.message_item_location_address);
    }

    @Override
    protected void bindContentView() {
        final LocationAttachment location = (LocationAttachment) message.getAttachment();
        addressText.setText(location.getAddress());

        int[] bound = ImageUtil.getBoundWithLength(getLocationDefEdge(), R.drawable.nim_cus_location, true);
        int width = bound[0];
        int height = bound[1];

        setLayoutParams(width, height, mapView);
        setCusLayoutParams(width, (int) (0.38 * height), addressText,height);

        mapView.loadAsResource(R.drawable.nim_cus_location, R.drawable.nim_message_item_round_bg);
        if (isReceivedMessage()) {
            setGravity(mapView, Gravity.LEFT);
            contentContainer.setBackground(null);
        } else {
            setGravity(mapView, Gravity.RIGHT);
            contentContainer.setBackground(null);
        }
    }

    private void setCusLayoutParams(int width, int height, TextView addressText, int margin) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) addressText.getLayoutParams();
        params.width = width;
        params.height = height;
        params.topMargin = margin;
        addressText.setLayoutParams(params);
    }

    @Override
    protected void onItemClick() {
        if (NimUIKitImpl.getLocationProvider() != null) {
            LocationAttachment location = (LocationAttachment) message.getAttachment();
            NimUIKitImpl.getLocationProvider().openMap(context, location.getLongitude(), location.getLatitude(), location.getAddress());
        }
    }

    public static int getLocationDefEdge() {
        return (int) (0.5 * ScreenUtil.screenWidth);
    }
}

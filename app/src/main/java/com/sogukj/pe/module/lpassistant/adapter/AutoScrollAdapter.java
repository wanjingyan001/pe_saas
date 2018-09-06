package com.sogukj.pe.module.lpassistant.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.sogukj.pe.R;
import com.sogukj.pe.baselibrary.utils.Utils;
import com.sogukj.pe.bean.PolicyBannerInfo;

import java.util.List;

/**
 * Created by CH-ZH on 2018/9/5.
 */
public class AutoScrollAdapter extends PagerAdapter {
    private Context mContext;
    private List<PolicyBannerInfo.BannerInfo> mList;
    private ClickItemListener mClickItemListener;
    private int mImageWidth;
    private int mImageHeight;
    private double mAspectRatio = 2.08d;

    public interface ClickItemListener {
        void onPagerItemClick(PolicyBannerInfo.BannerInfo item, int position);
    }

    public AutoScrollAdapter(Context context, List list) {
        this.mContext = context;
        this.mList = list;
    }

    public void replaceData(@Nullable List list) {
        if (list != null) {
            mList = list;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mList != null && mList.size() > 1) {
            return Integer.MAX_VALUE;
        }
        return 1;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        if (mList == null || mList.size() <= 0) {
            return null;
        }

        View bannerItem = LayoutInflater.from(mContext).inflate(R.layout.auto_viewpager_item, null);
        ImageView ivImage = (ImageView) bannerItem.findViewById(R.id.banner_image);

        position %= mList.size();
        if (position < 0) {
            position = mList.size() + position;
        }
        final int pos = position;
        PolicyBannerInfo.BannerInfo currentItem = mList.get(position);

        String imageUrl = "";
        if (currentItem != null) {
            mImageWidth = Utils.getScreenWidth(mContext);
            mImageHeight = (int)(mImageWidth / mAspectRatio);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mImageWidth, mImageHeight);
            ivImage.setLayoutParams(params);
            container.setLayoutParams(params);
            Glide.with(mContext).load(currentItem.getImage()).into(ivImage);

            ivImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PolicyBannerInfo.BannerInfo item = mList.get(pos);
                    if (mClickItemListener != null) {
                        mClickItemListener.onPagerItemClick(item, pos);
                    }
                }
            });

            container.addView(bannerItem, 0, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }
        return bannerItem;
    }

    public List<PolicyBannerInfo.BannerInfo> getList() {
        return mList;
    }

    public void setClickItemListener(ClickItemListener clickItemListener) {
        mClickItemListener = clickItemListener;
    }
}

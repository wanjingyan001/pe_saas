package com.sogukj.pe.module.hotpost;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.sogukj.pe.baselibrary.utils.Utils;

/**
 * Created by CH-ZH on 2018/9/6.
 */
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private Context mContext;
    private int spanCount;
    public GridSpacingItemDecoration(Context context,int spanCount) {
        this.mContext = context;
        this.spanCount = spanCount;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int column = position % spanCount; // 0,1,2,3
        int itemCount = parent.getAdapter().getItemCount();
        int remainder = itemCount % spanCount;
        Log.e("TAG","itemCount =====" + itemCount + "   remainder ====" + remainder);
        outRect.top = Utils.dip2px(mContext,15f);
        outRect.bottom = Utils.dip2px(mContext,15f);
        outRect.left = Utils.dip2px(mContext,10f);
        outRect.right = Utils.dip2px(mContext,10f);
    }
}

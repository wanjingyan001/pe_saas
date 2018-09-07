package com.sogukj.pe.module.hotpost.adapter;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sogukj.pe.R;

import java.util.List;

/**
 * Created by CH-ZH on 2018/9/6.
 */
public abstract class BaseLoadingAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "BaseLoadingAdapter";

    //header
    private static final int TYPE_HEADER_ITEM = 0;
    //normal
    private static final int TYPE_NORMAL_ITEM = 1;
    //footer
    private static final int TYPE_FOOTER_ITEM = 2;

    private FooterViewHolder mFooterViewHolder;
    //瀑布流
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;
    //数据集
    private List<T> mTs;
    private RecyclerView mRecyclerView;
    public BaseLoadingAdapter(RecyclerView recyclerView, List<T> ts) {

        mTs = ts;

        mRecyclerView = recyclerView;

        setSpanCount(recyclerView);

        //notifyLoading();
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     */
    private boolean canScrollDown(RecyclerView recyclerView) {
        return ViewCompat.canScrollVertically(recyclerView, 1);
    }

    /**
     * 设置加载item占据一行
     *
     * @param recyclerView recycleView
     */
    private void setSpanCount(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        if (layoutManager == null) {
            Log.e(TAG, "LayoutManager 为空,请先设置 recycleView.setLayoutManager(...)");
        }

        //网格布局
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int type = getItemViewType(position);
                    if (type == TYPE_NORMAL_ITEM) {
                        return 1;
                    } else {
                        return gridLayoutManager.getSpanCount();
                    }
                }
            });
        }

        //瀑布流布局
        if (layoutManager instanceof StaggeredGridLayoutManager) {
            mStaggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
        }
    }

    /**
     * 创建viewHolder
     *
     * @param parent viewGroup
     * @return viewHolder
     */
    public abstract RecyclerView.ViewHolder onCreateNormalViewHolder(ViewGroup parent);

    /**
     * 绑定viewHolder
     *
     * @param holder   viewHolder
     * @param position position
     */
    public abstract void onBindNormalViewHolder(RecyclerView.ViewHolder holder, int position);


    public abstract RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent);

    public abstract void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position);
    /**
     * 显示提示
     */
    private class FooterViewHolder extends RecyclerView.ViewHolder{
        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }
    @Override
    public int getItemViewType(int position) {
        T t = mTs.get(position);
        int type = -1;
        if (position == 0) {
            type = TYPE_HEADER_ITEM;
        }else if(position == mTs.size()-1) {
            type = TYPE_FOOTER_ITEM;
        }else{
            type = TYPE_NORMAL_ITEM;
        }
        return type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_NORMAL_ITEM) {
            return onCreateNormalViewHolder(parent);
        } else if(viewType == TYPE_HEADER_ITEM) {
            return onCreateHeaderViewHolder(parent);
        }else {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.bottom_hint,parent,false);
            mFooterViewHolder = new FooterViewHolder(view);
            return mFooterViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (type == TYPE_NORMAL_ITEM) {
            onBindNormalViewHolder(holder, position);
        } else if(type == TYPE_HEADER_ITEM) {
            onBindHeaderViewHolder(holder,position);
        }
    }

    @Override
    public int getItemCount() {
        if(mTs != null && mTs.size() > 0) {
            return mTs.size();
        }
        return 0;
    }
}

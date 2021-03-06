package com.sogukj.pe.baselibrary.widgets;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.SectionEntity;

import java.util.List;

/**
 * Created by admin on 2018/6/20.
 */

public abstract class BaseSectionDragAdapter<T extends SectionEntity, K extends BaseViewHolder> extends BaseItemDraggableAdapter<T, K> {
    protected int mSectionHeadResId;
    protected static final int SECTION_HEADER_VIEW = 0x00000444;

    public BaseSectionDragAdapter(int layoutResId, int sectionHeadResId, @Nullable List<T> data) {
        super(layoutResId, data);
        mSectionHeadResId = sectionHeadResId;
    }

    @Override
    protected int getDefItemViewType(int position) {
        return mData.get(position).isHeader ? SECTION_HEADER_VIEW : 0;
    }

    @Override
    protected K onCreateDefViewHolder(ViewGroup parent, int viewType) {
        if (viewType == SECTION_HEADER_VIEW) {
            return createBaseViewHolder(getItemView(mSectionHeadResId, parent));
        }

        return super.onCreateDefViewHolder(parent, viewType);
    }
    @Override
    protected boolean isFixedViewType(int type) {
        return super.isFixedViewType(type) || type == SECTION_HEADER_VIEW;
    }

    @Override
    public void onBindViewHolder(K holder, int position) {
        switch (holder.getItemViewType()) {
            case SECTION_HEADER_VIEW:
                setFullSpan(holder);
                convertHead(holder, getItem(position - getHeaderLayoutCount()));
                break;
            default:
                super.onBindViewHolder(holder, position);
                break;
        }
    }

    protected abstract void convertHead(K helper, T item);

}

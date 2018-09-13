package com.sogukj.pe.module.dataSource

import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.HotPostInfo

/**
 * Created by admin on 2018/9/13.
 */
class HotPostAdapter(data:List<HotPostInfo>): BaseMultiItemQuickAdapter<HotPostInfo, BaseViewHolder>(data) {

    init {
        addItemType(HotPostInfo.header, R.layout.item_hotpost_list)
        addItemType(HotPostInfo.item,R.layout.item2_hotpost_list)
    }

    override fun convert(helper: BaseViewHolder, item: HotPostInfo) {
        val image = helper.getView<ImageView>(R.id.iv_image)
        val title = helper.getView<TextView>(R.id.tv_title)
        Glide.with(helper.itemView.context).load(item.icon)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(Utils.dpToPx(helper.itemView.context,6))))
                .thumbnail(0.5f)
                .into(image)
        title.text = item.name
    }
}
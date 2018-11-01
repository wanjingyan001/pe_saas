package com.sogukj.pe.module.im.clouddish

import android.app.Activity
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.bean.CloudLevel1
import com.sogukj.pe.bean.CloudLevel2

/**
 * Created by CH-ZH on 2018/10/25.
 */
class CloudExpandableAdapter : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
    private var mAct : Activity? = null
    constructor(data: MutableList<MultiItemEntity>,act: Activity):super(data){
        mAct = act
        addItemType(Extras.TYPE_LEVEL_0, R.layout.item_expandable_company)
        addItemType(Extras.TYPE_LEVEL_1, R.layout.item_expandable_dir)
    }

    override fun convert(holder: BaseViewHolder, item: MultiItemEntity?) {
        when(holder.itemViewType){
            Extras.TYPE_LEVEL_0 -> {
                val level0Item = item as CloudLevel1
                if (null == level0Item) return
                holder.setText(R.id.tv_title,level0Item.title)
                if (level0Item.isExpanded){
                    holder.setImageResource(R.id.iv_expanded, R.drawable.icon_pro_expanded)
                }else{
                    holder.setImageResource(R.id.iv_expanded, R.drawable.icon_pro_hide)
                }
                holder.itemView.setOnClickListener {
                    val position = holder.adapterPosition
                    if (level0Item.isExpanded){
                        collapse(position)
                    }else{
                        expand(position)
                    }
                }
            }

            Extras.TYPE_LEVEL_1 -> {
                val level1Item = item as CloudLevel2
                if (null == level1Item) return
                holder.setText(R.id.tv_summary,level1Item.name)
                if (level1Item.size.isNullOrEmpty()){
                    holder.setText(R.id.tv_time,"暂无文件")
                }else{
                    holder.setText(R.id.tv_time,level1Item.time)
                }
                holder.setText(R.id.tv_size,level1Item.size)
                holder.itemView.clickWithTrigger {

                }
            }

        }
    }

}
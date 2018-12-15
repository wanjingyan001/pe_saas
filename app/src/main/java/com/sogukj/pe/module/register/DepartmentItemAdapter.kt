package com.sogukj.pe.module.register

import android.app.Activity
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.Depart0Item
import com.sogukj.pe.bean.Depart1Item

/**
 * Created by CH-ZH on 2018/12/14.
 */
class DepartmentItemAdapter : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
    private var act : Activity ? = null
    constructor(data: MutableList<MultiItemEntity>,act: Activity):super(data){
        this.act = act
        addItemType(Extras.TYPE_LEVEL_0, R.layout.item_department1)
        addItemType(Extras.TYPE_LEVEL_1, R.layout.item_department2)
    }
    override fun convert(holder: BaseViewHolder, item: MultiItemEntity) {
        when(holder.itemViewType){
            Extras.TYPE_LEVEL_0 -> {
                val depart0Item = item as Depart0Item
                val position = holder.adapterPosition
                if (position == 0){
                    holder.setGone(R.id.view_divider,false)
                }else{
                    holder.setGone(R.id.view_divider,true)
                }
                holder.setText(R.id.departmentName,depart0Item.name)
                holder.itemView.setOnClickListener {
                    //查看
                }
            }
            Extras.TYPE_LEVEL_1 ->{
                val depart1Item = item as Depart1Item
                holder.setText(R.id.departmentName,depart1Item.name)
                holder.itemView.setOnClickListener {
                    //查看
                }
            }
        }
    }
}
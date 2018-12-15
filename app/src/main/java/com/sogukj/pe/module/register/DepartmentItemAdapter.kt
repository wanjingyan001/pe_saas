package com.sogukj.pe.module.register

import android.app.Activity
import android.widget.ImageView
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
                if (depart0Item.isCanSelect){
                    holder.setGone(R.id.iv_select,true)
                    holder.setGone(R.id.view_gaps,true)
                }else{
                    holder.setGone(R.id.iv_select,false)
                    holder.setGone(R.id.view_gaps,false)
                }
                val iv_select = holder.getView<ImageView>(R.id.iv_select)
                iv_select.isSelected = depart0Item.isSelected
                holder.setText(R.id.departmentName,depart0Item.name)
                holder.addOnClickListener(R.id.item_view)
            }
            Extras.TYPE_LEVEL_1 ->{
                val depart1Item = item as Depart1Item
                holder.setText(R.id.departmentName,depart1Item.name)
                if (depart1Item.isCanSelect){
                    holder.setGone(R.id.iv_select,true)
                }else{
                    holder.setGone(R.id.iv_select,false)
                }
                val iv_select = holder.getView<ImageView>(R.id.iv_select)
                iv_select.isSelected = depart1Item.isSelected
                holder.addOnClickListener(R.id.item_view)
            }
        }
    }
}
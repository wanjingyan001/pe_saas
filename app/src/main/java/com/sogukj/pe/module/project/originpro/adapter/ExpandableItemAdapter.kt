package com.sogukj.pe.module.project.originpro.adapter

import android.app.Activity
import android.widget.ImageView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.Extras.TYPE_FILE
import com.sogukj.pe.Extras.TYPE_LEVEL_0
import com.sogukj.pe.Extras.TYPE_LEVEL_1
import com.sogukj.pe.R
import com.sogukj.pe.bean.Level0Item
import com.sogukj.pe.bean.Level1Item
import com.sogukj.pe.bean.Level2Item
import com.sogukj.pe.module.other.OnlinePreviewActivity
import com.sogukj.pe.peUtils.FileTypeUtils
import org.jetbrains.anko.imageResource

/**
 * Created by CH-ZH on 2018/9/20.
 */
class ExpandableItemAdapter : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
    private var type : Int = 0
    private var mAct : Activity ? = null
    constructor(data: MutableList<MultiItemEntity>,type:Int,act: Activity):super(data){
        this.type = type
        mAct = act
        addItemType(TYPE_LEVEL_0, R.layout.item_expandable_lv0)
        addItemType(TYPE_LEVEL_1, R.layout.item_expandable_lv1)
        if (type == 0){
            addItemType(TYPE_FILE, R.layout.item_expandable_lv2)
        }else if (type == 1){
            addItemType(TYPE_FILE, R.layout.item_expandable_file)
        }
    }

    override fun convert(holder: BaseViewHolder, item: MultiItemEntity?) {
        when(holder.itemViewType){
            TYPE_LEVEL_0 -> {
                val level0Item = item as Level0Item
                if (null == level0Item) return
                holder.setText(R.id.tv_title,level0Item.title)
                if (level0Item.isExpanded){
                    holder.setImageResource(R.id.iv_expanded,R.drawable.icon_pro_expanded)
                }else{
                    holder.setImageResource(R.id.iv_expanded,R.drawable.icon_pro_hide)
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

            TYPE_LEVEL_1 -> {
                val level1Item = item as Level1Item
                if (null == level1Item) return
                holder.setText(R.id.tv_sub_title,level1Item.subName)
            }

            TYPE_FILE -> {
                val level2Item = item as Level2Item
                if (null == level2Item) return
                if (type == 0){
                    if (level2Item.type == -1){
                        holder.setGone(R.id.pdfIcon,false)
                        holder.setGone(R.id.iv_delete,false)
                        holder.setGone(R.id.pdfName,false)

                        holder.setGone(R.id.tv_add_file,true)
                        holder.setGone(R.id.view,true)
                    }else{
                        val file = level2Item.file
                        if (null == file) return
                        holder.setGone(R.id.pdfIcon,true)
                        holder.setGone(R.id.iv_delete,true)
                        holder.setGone(R.id.pdfName,true)

                        holder.setGone(R.id.tv_add_file,false)
                        holder.setGone(R.id.view,false)
                        holder.setText(R.id.pdfName,file.file_name)

                        holder.addOnClickListener(R.id.iv_delete)
                        val pdfIcon = holder.getView<ImageView>(R.id.pdfIcon)
                        pdfIcon.imageResource = FileTypeUtils.getFileType(file.file_name).icon
                    }
                    holder.addOnClickListener(R.id.tv_add_file)

                }else if (type == 1){
                    if (level2Item.type == -1){
                        holder.setGone(R.id.tv_empty,true)
                        holder.setGone(R.id.view,true)
                        holder.setGone(R.id.pdfIcon,false)
                        holder.setGone(R.id.pdfName,false)
                        holder.setGone(R.id.time,false)
                        holder.setGone(R.id.iv_right,false)
                    }else{
                        val file = level2Item.file
                        if (null == file) return
                        holder.setGone(R.id.tv_empty,false)
                        holder.setGone(R.id.view,false)
                        holder.setGone(R.id.pdfIcon,true)
                        holder.setGone(R.id.pdfName,true)
                        holder.setGone(R.id.time,true)
                        holder.setGone(R.id.iv_right,true)

                        holder.setText(R.id.pdfName,file.file_name)
                        holder.setText(R.id.time,file.date)
                        val pdfIcon = holder.getView<ImageView>(R.id.pdfIcon)
                        pdfIcon.imageResource = FileTypeUtils.getFileType(file.file_name).icon
                        holder.itemView.setOnClickListener {
                            //预览页面
                            OnlinePreviewActivity.start(mAct!!,file.preview,file.file_name)
                        }
                    }
                }

            }
        }
    }

}
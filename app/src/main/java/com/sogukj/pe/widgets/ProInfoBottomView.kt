package com.sogukj.pe.widgets

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectFileInfo
import com.sogukj.pe.peUtils.FileTypeUtils
import com.sogukj.pe.peUtils.FileUtil
import org.jetbrains.anko.imageResource

/**
 * Created by CH-ZH on 2018/9/18.
 */
class ProInfoBottomView : LinearLayout {
    private var mContext : Context ? = null
    private var mRootView : View? = null
    private var iv_expanded : ImageView? = null
    private var file_list : RecyclerView ? = null
    private var tv_add_file : TextView? = null
    lateinit var adapter: AddFileAdapter
    private var fileInfos = ArrayList<ProjectFileInfo>()
    constructor(context: Context) : super(context){
        mContext = context
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mContext = context
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mContext = context
        initView()
    }

    private fun initView() {
        mRootView = View.inflate(mContext, R.layout.layout_pro_info,this)
        iv_expanded = mRootView!!.findViewById(R.id.iv_expanded)
        file_list = mRootView!!.findViewById(R.id.file_list)
        tv_add_file = mRootView!!.findViewById(R.id.tv_add_file)

        adapter = AddFileAdapter(mContext!!,fileInfos)
        file_list!!.layoutManager = LinearLayoutManager(mContext)
        file_list!!.adapter = adapter

        iv_expanded!!.setOnClickListener {
            if (file_list!!.visibility == View.VISIBLE){
                file_list!!.visibility = View.GONE
                tv_add_file!!.visibility = View.GONE
                iv_expanded!!.setImageResource(R.drawable.icon_pro_hide)
            }else{
                file_list!!.visibility = View.VISIBLE
                tv_add_file!!.visibility = View.VISIBLE
                iv_expanded!!.setImageResource(R.drawable.icon_pro_expanded)
            }
        }
    }

    /**
     * 添加文件
     */
    open fun addFileListener(listener: OnClickListener){
        tv_add_file!!.setOnClickListener(listener)
    }

    /**
     * 添加文件
     */
    open fun addFileData(info : ProjectFileInfo){
        if (null != adapter){
            adapter.addData(fileInfos.size,info)
        }
    }

    inner class AddFileAdapter(val context: Context,val fileInfos:ArrayList<ProjectFileInfo>) : RecyclerView.Adapter<AddFileAdapter.ViewHolder>(){
        fun addData(position: Int,info : ProjectFileInfo){
            fileInfos.add(position,info)
            notifyItemInserted(position)
        }
        fun removeData(position: Int){
            fileInfos.removeAt(position)
            notifyItemRemoved(position)
            notifyDataSetChanged()

        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_add_file,null,false))
        }

        override fun getItemCount(): Int {
            return fileInfos.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.fitData(fileInfos[position],position)
            holder.bindListener(position)
        }

        inner class ViewHolder(itemView : View):RecyclerView.ViewHolder(itemView){
            val iv_image = itemView.findViewById<ImageView>(R.id.iv_image)
            val tv_name = itemView.findViewById<TextView>(R.id.tv_name)
            val iv_delete = itemView.findViewById<ImageView>(R.id.iv_delete)

            fun fitData(projectFileInfo: ProjectFileInfo, position: Int) {
                if (null != projectFileInfo){
                    if (null != projectFileInfo.file){
                        if (null != FileUtil.getFileType(projectFileInfo.file!!.absolutePath)) {
                            Glide.with(context)
                                    .load(projectFileInfo.file!!.absolutePath)
                                    .thumbnail(0.1f)
                                    .apply(RequestOptions()
                                            .centerCrop()
                                            .error(R.drawable.icon_pic))
                                    .into(iv_image)
                        } else {
                            iv_image.imageResource = FileTypeUtils.getFileType(projectFileInfo.file).icon
                        }
                    }

                    tv_name.text = projectFileInfo.name
                }

            }

            fun bindListener(position: Int) {
                iv_delete.setOnClickListener {
                    removeData(position)
                }
            }
        }
    }
}
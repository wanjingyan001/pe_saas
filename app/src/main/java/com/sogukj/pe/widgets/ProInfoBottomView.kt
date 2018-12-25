package com.sogukj.pe.widgets

import android.app.ProgressDialog
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
import com.sogukj.pe.App
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.bean.ProjectApproveInfo
import com.sogukj.pe.peUtils.FileTypeUtils
import com.sogukj.pe.peUtils.ToastUtil
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.imageResource
import java.io.File

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
    private var fileInfos = ArrayList<ProjectApproveInfo.ApproveFile>()
    private var tv_create : TextView ? = null
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

    open fun setCreateTextView(tv_create : TextView){
        this.tv_create = tv_create
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
    open fun addFileData(info: ProjectApproveInfo.ApproveFile, file: File){
        uploadFileToOss(file,info)
    }

    private fun uploadFileToOss(file: File, info: ProjectApproveInfo.ApproveFile) {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("*/*"), file))
                .addFormDataPart("type",1.toString()) //1 项目文件 2审批文件
        val body = builder.build()
        showProgress("正在上传")
        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .uploadProFile(body)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val fileBean = payload.payload
                            if (null != adapter){
                                info.file_name = fileBean!!.file_name
                                info.size = fileBean.size
                                info.url = fileBean.url
                                info.filePath = fileBean.filePath
                                adapter.addData(fileInfos.size,info)
                            }
                            ToastUtil.showSuccessToast("上传成功",mContext!!)
                        }else{
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail,payload.message,mContext!!)
                        }
                        hideProgress()
                    }

                    onError {
                        it.printStackTrace()
                        ToastUtil.showCustomToast(R.drawable.icon_toast_fail,"上传失败",mContext!!)
                        hideProgress()
                    }
                }
    }
    private var isClickCreate = false
    open fun setFileData(files: List<ProjectApproveInfo.ApproveFile>){
        if (null != adapter){
            fileInfos.clear()
            fileInfos.addAll(files)
            adapter.notifyDataSetChanged()
        }
    }

    open fun getFileData():List<ProjectApproveInfo.ApproveFile>{
        return fileInfos
    }

    open fun setClickCreate(isClick:Boolean){
        this.isClickCreate = isClick
    }
    open fun isClickCreat():Boolean{
        return isClickCreate
    }
    inner class AddFileAdapter(val context: Context,val fileInfos:ArrayList<ProjectApproveInfo.ApproveFile>) : RecyclerView.Adapter<AddFileAdapter.ViewHolder>(){
        fun addData(position: Int,info : ProjectApproveInfo.ApproveFile){
            fileInfos.add(position,info)
            notifyItemInserted(position)
        }
        fun removeData(position: Int){
            fileInfos.removeAt(position)
            notifyItemRemoved(position)
            notifyDataSetChanged()
            if (fileInfos.size > 0){
                isClickCreate = true
                if (null != tv_create){
                    tv_create!!.setBackgroundResource(R.drawable.bg_create_pro)
                }
            }else{
                isClickCreate = false
                if (null != tv_create){
                    tv_create!!.setBackgroundResource(R.drawable.bg_create_gray)
                }
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_add_file,null,false))
        }

        override fun getItemCount(): Int {
            return fileInfos.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.fitData(fileInfos[position],position)
            holder.bindListener(fileInfos[position],position)
            if (fileInfos.size > 0){
                isClickCreate = true
                if (null != tv_create){
                    tv_create!!.setBackgroundResource(R.drawable.bg_create_pro)
                }
            }else{
                isClickCreate = false
                if (null != tv_create){
                    tv_create!!.setBackgroundResource(R.drawable.bg_create_gray)
                }
            }
        }

        inner class ViewHolder(itemView : View):RecyclerView.ViewHolder(itemView){
            val iv_image = itemView.findViewById<ImageView>(R.id.iv_image)
            val tv_name = itemView.findViewById<TextView>(R.id.tv_name)
            val iv_delete = itemView.findViewById<ImageView>(R.id.iv_delete)

            fun fitData(projectFileInfo: ProjectApproveInfo.ApproveFile, position: Int) {
                if (null != projectFileInfo){
                    iv_image.imageResource = FileTypeUtils.getFileType(projectFileInfo.file_name).icon
                    tv_name.text = projectFileInfo.file_name
                }
            }

            fun bindListener(projectFileInfo: ProjectApproveInfo.ApproveFile, position: Int) {
                iv_delete.setOnClickListener {
                    if (null != projectFileInfo.file){
                        removeDataFroOss(projectFileInfo,position)
                    }else{
                        removeDataFromNet(projectFileInfo,position)
                    }
                }
            }

            private fun removeDataFroOss(projectFileInfo: ProjectApproveInfo.ApproveFile, position: Int) {
                SoguApi.getService(App.INSTANCE,OtherService::class.java)
                        .deleteProjectOssFile(projectFileInfo.filePath)
                        .execute {
                            onNext { payload ->
                                if (payload.isOk){
                                    removeData(position)
                                }else{
                                    ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, mContext!!)
                                }
                            }

                            onError {
                                it.printStackTrace()
                                ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "文件删除失败", mContext!!)
                            }
                        }
            }

            private fun removeDataFromNet(projectFileInfo: ProjectApproveInfo.ApproveFile, position: Int) {
                SoguApi.getService(App.INSTANCE,OtherService::class.java)
                        .deleteProjectFile(projectFileInfo.file_id!!)
                        .execute {
                            onNext { payload ->
                                if(payload.isOk){
                                    removeData(position)
                                }else{
                                    ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, mContext!!)
                                }
                            }

                            onError {
                                it.printStackTrace()
                                ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "文件删除失败", mContext!!)
                            }
                        }
            }
        }
    }

    var progressDialog: ProgressDialog? = null
    fun showProgress(msg: String) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(mContext)
        }
        progressDialog?.setMessage(msg)
        progressDialog?.show()
    }

    fun hideProgress() {
        if (progressDialog != null) {
            progressDialog?.dismiss()
        }
    }
}
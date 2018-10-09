package com.sogukj.pe.widgets

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.sogukj.pe.App
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.FileDataBean
import com.sogukj.pe.bean.ProjectApproveInfo
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.module.fileSelector.FileMainActivity
import com.sogukj.pe.module.main.ContactsActivity
import com.sogukj.pe.module.project.originpro.ProjectApprovalShowActivity
import com.sogukj.pe.module.project.originpro.ProjectApprovalShowActivity.Companion.REQ_LXH_FILE
import com.sogukj.pe.module.project.originpro.ProjectApprovalShowActivity.Companion.REQ_SELECT_FILE
import com.sogukj.pe.module.project.originpro.ProjectApprovalShowActivity.Companion.REW_SELECT_TIME
import com.sogukj.pe.module.project.originpro.SelectTimeActivity
import com.sogukj.pe.peUtils.FileTypeUtils
import com.sogukj.pe.peUtils.ToastUtil
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by CH-ZH on 2018/9/21.
 */

class BuildProjectDialog {
    private var recycle_view: RecyclerView? = null
    private var rv : RecyclerView ? = null
    private var mAct: Activity? = null
    lateinit var adapter: AddFileAdapter
    lateinit var lxhAdapter : AddFileAdapter
    lateinit var memberAdapter : MeetingMemberAdapter
    lateinit var allocationAdapter: MeetingMemberAdapter
    private var fileInfos = ArrayList<ProjectApproveInfo.ApproveFile>()
    private var lxhFileInfos = ArrayList<ProjectApproveInfo.ApproveFile>()
    private var memberInfos = ArrayList<UserBean>()
    private var allocationInfos = ArrayList<UserBean>()
    private var project: ProjectBean ? = null
    lateinit var calendar: CalendarDingDing
    var selectDate: Date? = null
    private var type = -1
    fun showAgreeBuildProDialog(context: Activity, project: ProjectBean?) {
        mAct = context
        this.project = project
        type = 0
        val build = MaterialDialog.Builder(context)
                .theme(Theme.DARK)
                .customView(R.layout.layout_agree_build, false)
                .canceledOnTouchOutside(false)
                .build()
        build.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val iv_close = build.find<ImageView>(R.id.iv_close)
        val ed_agree = build.find<EditText>(R.id.ed_agree)
        recycle_view = build.find<RecyclerView>(R.id.rv)
        val tv_add_file = build.find<TextView>(R.id.tv_add_file)
        val tv_cancel = build.find<TextView>(R.id.tv_cancel)
        val tv_agree = build.find<TextView>(R.id.tv_agree)

        adapter = AddFileAdapter(mAct!!, fileInfos)
        recycle_view!!.layoutManager = LinearLayoutManager(mAct)
        recycle_view!!.adapter = adapter

        iv_close.setOnClickListener {
            if (build.isShowing) {
                build.dismiss()
            }
        }

        tv_add_file.setOnClickListener {
            //添加文件
            FileMainActivity.start(mAct!!, 1, requestCode = REQ_SELECT_FILE)
        }

        tv_cancel.setOnClickListener {
            if (build.isShowing) {
                build.dismiss()
            }
        }

        tv_agree.setOnClickListener {
            //同意通过
            commitApprove(ed_agree.textStr,build)
        }
        build.show()
        setRecycleLayout(fileInfos.size)
    }

    fun allocationApprove(context: Activity, project: ProjectBean?){
        mAct = context
        this.project = project
        val build = MaterialDialog.Builder(context)
                .theme(Theme.DARK)
                .customView(R.layout.layout_allocation_build, false)
                .canceledOnTouchOutside(false)
                .build()
        build.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val iv_close = build.find<ImageView>(R.id.iv_close)
        val rv = build.find<RecyclerView>(R.id.rv)
        val tv_cancel = build.find<TextView>(R.id.tv_cancel)
        val tv_agree = build.find<TextView>(R.id.tv_agree)

        allocationInfos.clear()
        allocationInfos.add(UserBean())
        allocationAdapter = MeetingMemberAdapter(context,allocationInfos,1)
        rv.layoutManager = GridLayoutManager(context,7)
        rv.adapter = allocationAdapter


        iv_close.setOnClickListener {
            if (build.isShowing) {
                build.dismiss()
            }
        }
        tv_cancel.setOnClickListener {
            if (build.isShowing) {
                build.dismiss()
            }
        }

        tv_agree.setOnClickListener {
            //分配审批
            if (null != allocationAdapter && null != allocationAdapter.getData()
            && allocationAdapter.getData().size > 1){
                commitAllocation(build)
            }
        }
        build.show()
    }

    private fun commitAllocation(build: MaterialDialog) {
        if (null == project) return
        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .commitAllocationApprove(allocationAdapter.getData()[0].uid!!,project!!.company_id!!,project!!.floor!!)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            if (build.isShowing) {
                                build.dismiss()
                            }
                        }else{
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, mAct!!)
                        }
                    }

                    onError {
                        it.printStackTrace()
                        ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "分配审批失败", mAct!!)
                    }
                }
    }

    private fun commitApprove(content: String, build: MaterialDialog) {
        if (null == project) return
        val files = ArrayList<FileDataBean>()
        for (file in fileInfos){
            val fileBean = FileDataBean()
            fileBean.filepath = file.filePath
            fileBean.filename = file.file_name
            fileBean.size = file.size
            files.add(fileBean)
        }
        val map = HashMap<String,Any>()
        map.put("company_id",project!!.company_id!!)
        map.put("floor",project!!.floor!!)
        map.put("type",1) //-1=>’否决’,1=>’同意通过’,2=> 同意上储备,3=>同意退出
        map.put("content",content)
        map.put("current",0)
        map.put("files",files)

        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .commitApprove(map)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            if (build.isShowing) {
                                build.dismiss()
                            }
                            (mAct as ProjectApprovalShowActivity).getApprevoRecordInfo()
                        }else{
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, mAct!!)
                        }
                    }

                    onError {
                        it.printStackTrace()
                        ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "审批失败", mAct!!)
                    }
                }
    }
    private var tv_time : TextView ? = null
    private var date : Date ? = null
    fun showAgreeBuildLxh(context: Activity, project: ProjectBean?) {
        mAct = context
        this.project = project
        type = 1
        val build = MaterialDialog.Builder(context)
                .theme(Theme.DARK)
                .customView(R.layout.layout_agree_build_lxh, false)
                .canceledOnTouchOutside(false)
                .build()
        build.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val iv_close = build.find<ImageView>(R.id.iv_close)
        val ed_agree = build.find<EditText>(R.id.ed_agree)
        rv = build.find<RecyclerView>(R.id.rv)
        val rv_member = build.find<RecyclerView>(R.id.rv_member)
        val tv_add_file = build.find<TextView>(R.id.tv_add_file)
        val tv_cancel = build.find<TextView>(R.id.tv_cancel)
        val tv_agree = build.find<TextView>(R.id.tv_agree)
        tv_time = build.find<TextView>(R.id.tv_time)
        val iv_time = build.find<ImageView>(R.id.iv_time)
        lxhAdapter = AddFileAdapter(mAct!!, lxhFileInfos)
        rv!!.layoutManager = LinearLayoutManager(mAct)
        rv!!.adapter = lxhAdapter

        memberInfos.clear()
        memberInfos.add(UserBean())
        memberAdapter = MeetingMemberAdapter(context,memberInfos,0)
        rv_member.layoutManager = GridLayoutManager(context,7)
        rv_member.adapter = memberAdapter

        if (null != selectDate){
            tv_time!!.setText(Utils.getTime(selectDate, "yyyy-MM-dd HH:mm"))
        }else{
            tv_time!!.setText(Utils.getTime(Date(), "yyyy-MM-dd HH:mm"))
        }
        calendar = CalendarDingDing(context)
        iv_close.setOnClickListener {
            if (build.isShowing) {
                build.dismiss()
            }
        }

        tv_add_file.setOnClickListener {
            //添加文件
            FileMainActivity.start(mAct!!, 1, requestCode = REQ_LXH_FILE)
        }

        tv_cancel.setOnClickListener {
            if (build.isShowing) {
                build.dismiss()
            }
        }

        tv_agree.setOnClickListener {
            if (build.isShowing) {
                build.dismiss()
            }
            //同意通过
            commitLxhApprove(ed_agree.textStr,build)
        }

        iv_time.setOnClickListener {
            //选择时间
            if (null == date){
                date = Date()
            }
           SelectTimeActivity.start(mAct,date!!,REW_SELECT_TIME)
        }

        tv_time!!.setOnClickListener {
            //选择时间
            if (null == date){
                date = Date()
            }
            SelectTimeActivity.start(mAct,date!!,REW_SELECT_TIME)
        }

        build.show()
        setLxhRecycleLayout(lxhFileInfos.size)
    }

    private fun commitLxhApprove(content: String, build: MaterialDialog?) {
        if (null == project) return
        val files = ArrayList<FileDataBean>()
        var copier : String ? = null
        for (file in lxhFileInfos){
            val fileBean = FileDataBean()
            fileBean.filepath = file.filePath
            fileBean.filename = file.file_name
            fileBean.size = file.size
            files.add(fileBean)
        }
        val map = HashMap<String,Any>()
        map.put("company_id",project!!.company_id!!)
        map.put("floor",project!!.floor!!)
        map.put("type",2) //-1=>’否决’,1=>’同意通过’,2=> 同意上储备,3=>同意退出
        map.put("content",content)
        map.put("current",0)
        map.put("files",files)
        if (null != date){
            map.put("meeting_time",date!!.time/1000)
        }else{
            map.put("meeting_time",Date().time/1000)
        }

        if (null != memberAdapter){
            val members = memberAdapter.getData()
            if (members.size > 1){
                for (i in 0 .. members.size - 2){
                    copier += "${members[i].uid},"
                }
            }
        }
        map.put("copier",copier!!)
        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .commitApprove(map)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            if (build!!.isShowing) {
                                build.dismiss()
                            }
                            (mAct as ProjectApprovalShowActivity).getApprevoRecordInfo()
                        }else{
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, mAct!!)
                        }
                    }

                    onError {
                        it.printStackTrace()
                        ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "审批失败", mAct!!)
                    }
                }
    }

    open fun setTime(date: Date?) {
        this.date = date
        if (null != date){
            tv_time!!.setText(Utils.getTime(date, "yyyy-MM-dd HH:mm"))
        }else{
            tv_time!!.setText(Utils.getTime(Date(), "yyyy-MM-dd HH:mm"))
        }
    }


    /**
     * 添加文件
     * type : 0 -> 同意通过 1 -> 同意上立项会
     */
    open fun addFileData(info: ProjectApproveInfo.ApproveFile, file: File,type : Int) {
        uploadFileToOss(file, info,type)
    }

    private fun uploadFileToOss(file: File, info: ProjectApproveInfo.ApproveFile,type : Int) {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("*/*"), file))
                .addFormDataPart("type",2.toString()) //1 项目文件 2审批文件
        val body = builder.build()
        showProgress("正在上传")
        SoguApi.getService(App.INSTANCE, OtherService::class.java)
                .uploadProFile(body)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            val fileBean = payload.payload
                            if (type == 0){
                                if (null != adapter) {
                                    info.file_name = fileBean!!.file_name
                                    info.size = fileBean!!.size
                                    info.url = fileBean!!.url
                                    info.filePath = fileBean!!.filePath
                                    adapter.addData(fileInfos.size, info)
                                    setRecycleLayout(fileInfos.size)
                                }
                            }else if (type == 1){
                                if (null != lxhAdapter) {
                                    info.file_name = fileBean!!.file_name
                                    info.size = fileBean!!.size
                                    info.url = fileBean!!.url
                                    info.filePath = fileBean!!.filePath
                                    lxhAdapter.addData(lxhFileInfos.size, info)
                                    setLxhRecycleLayout(lxhFileInfos.size)
                                }
                            }

                            ToastUtil.showSuccessToast("上传成功", mAct!!)
                        } else {
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, mAct!!)
                        }
                        hideProgress()
                    }

                    onError {
                        it.printStackTrace()
                        ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "上传失败", mAct!!)
                        hideProgress()
                    }
                }
    }

    private fun setLxhRecycleLayout(size: Int) {
        if (null != rv) {
            rv!!.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    //设置recyclerView高度
                    val layoutParams = rv!!.layoutParams
                    rv!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val height = Utils.dip2px(mAct, 300f)
                    if (rv!!.height > height) {
                        layoutParams.height = height
                    } else {
                        layoutParams.height = Utils.dip2px(mAct, 40f)*size
                    }
                    rv!!.layoutParams = layoutParams

                }
            })
        }
    }

    private fun setRecycleLayout(size: Int) {
        if (null != recycle_view) {
            recycle_view!!.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    //设置recyclerView高度
                    val layoutParams = recycle_view!!.layoutParams
                    recycle_view!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val height = Utils.dip2px(mAct, 350f)
                    if (recycle_view!!.height > height) {
                        layoutParams.height = height
                    } else {
                        layoutParams.height = Utils.dip2px(mAct, 40f)*size
                    }
                    recycle_view!!.layoutParams = layoutParams

                }
            })
        }
    }

    open fun setFileData(files: List<ProjectApproveInfo.ApproveFile>) {
        if (null != adapter) {
            fileInfos.clear()
            fileInfos.addAll(files)
            adapter.notifyDataSetChanged()
        }
    }

    open fun getFileData(): List<ProjectApproveInfo.ApproveFile> {
        return fileInfos
    }

    open fun setMemberData(members:List<UserBean>){
        if (null != memberAdapter){
            memberAdapter.setData(members)
        }
    }

    open fun setAllocationData(infos : List<UserBean>){
        if (null != allocationAdapter){
            if (infos.size > 2){
                ToastUtil.showCustomToast(R.drawable.icon_toast_common,"分配审批人员不能超过一个",mAct!!)
                return
            }
            allocationAdapter.setData(infos)
        }
    }
    inner class MeetingMemberAdapter(val context:Context,val members:ArrayList<UserBean>,val type:Int):RecyclerView.Adapter<MeetingMemberAdapter.ViewHolder>(){

        fun setData(infos : List<UserBean>){
            members.clear()
            members.addAll(infos)
            notifyDataSetChanged()
        }
        fun removeData(position: Int) {
            members.removeAt(position)
            notifyItemRemoved(position)
            notifyDataSetChanged()
        }

        fun getData() : ArrayList<UserBean>{
            return members
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_add_member, null, false))
        }

        override fun getItemCount(): Int {
            return members.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.fitData(position,members[position])
            holder.bindListener(position)
        }

        inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
            val icon = itemView.findViewById<ImageView>(R.id.icon)
            val name = itemView.findViewById<TextView>(R.id.name)
            val delete = itemView.findViewById<ImageView>(R.id.delete)
            fun fitData(position: Int, userBean: UserBean) {
                if (null == userBean) return
                if (null == userBean.uid){
                    name.text = "添加"
                    delete.visibility = View.GONE
                }else{
                    name.text = userBean.name
                    delete.visibility = View.VISIBLE
                    Glide.with(context).load(userBean.url).into(icon)
                }
            }

            fun bindListener(position: Int) {
                itemView.setOnClickListener {
                    if (position == members.size - 1){
                        //添加人员
                        val infos = ArrayList<UserBean>()
                        if (members.size > 1){
                            for (i in 0 .. members.size - 2){
                                infos.add(members[i])
                            }
                        }
                        if (type == 0){
                            ContactsActivity.startWithDefault(mAct!!, infos, false, false, null, ProjectApprovalShowActivity.REQ_ADD_MEMBER)
                        }else if (type == 1){
                            ContactsActivity.startWithDefault(mAct!!, infos, false, false, null, ProjectApprovalShowActivity.REQ_ADD_MEMBER)
                        }
                    }else{
                        removeData(position)
                    }
                }

            }
        }
    }

    inner class AddFileAdapter(val context: Context, val infos: ArrayList<ProjectApproveInfo.ApproveFile>) : RecyclerView.Adapter<AddFileAdapter.ViewHolder>() {
        fun addData(position: Int, info: ProjectApproveInfo.ApproveFile) {
            infos.add(position, info)
            notifyItemInserted(position)
        }

        fun removeData(position: Int) {
            infos.removeAt(position)
            notifyItemRemoved(position)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_add_file, null, false))
        }

        override fun getItemCount(): Int {
            return infos.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.fitData(infos[position], position)
            holder.bindListener(infos[position], position)
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val iv_image = itemView.findViewById<ImageView>(R.id.iv_image)
            val tv_name = itemView.findViewById<TextView>(R.id.tv_name)
            val iv_delete = itemView.findViewById<ImageView>(R.id.iv_delete)

            fun fitData(projectFileInfo: ProjectApproveInfo.ApproveFile, position: Int) {
                if (null != projectFileInfo) {
                    iv_image.imageResource = FileTypeUtils.getFileType(projectFileInfo.file_name).icon
                    tv_name.text = projectFileInfo.file_name
                }
            }

            fun bindListener(projectFileInfo: ProjectApproveInfo.ApproveFile, position: Int) {
                iv_delete.setOnClickListener {
                    if (null != projectFileInfo.file) {
                        removeDataFroOss(projectFileInfo, position)
                    } else {
                        removeDataFromNet(projectFileInfo, position)
                    }
                }
            }

            private fun removeDataFroOss(projectFileInfo: ProjectApproveInfo.ApproveFile, position: Int) {
                SoguApi.getService(App.INSTANCE, OtherService::class.java)
                        .deleteProjectOssFile(projectFileInfo.filePath)
                        .execute {
                            onNext { payload ->
                                if (payload.isOk) {
                                    removeData(position)
                                    if (type == 0){
                                        setRecycleLayout(fileInfos.size)
                                    }else if (type == 1){
                                       setLxhRecycleLayout(lxhFileInfos.size)
                                    }
                                } else {
                                    ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, mAct!!)
                                }
                            }

                            onError {
                                it.printStackTrace()
                                ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "文件删除失败", mAct!!)
                            }
                        }
            }

            private fun removeDataFromNet(projectFileInfo: ProjectApproveInfo.ApproveFile, position: Int) {
                SoguApi.getService(App.INSTANCE, OtherService::class.java)
                        .deleteProjectFile(projectFileInfo.file_id!!)
                        .execute {
                            onNext { payload ->
                                if (payload.isOk) {
                                    removeData(position)
                                    if (type == 0){
                                        setRecycleLayout(fileInfos.size)
                                    }else if (type == 1){
                                        setLxhRecycleLayout(lxhFileInfos.size)
                                    }
                                } else {
                                    ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, mAct!!)
                                }
                            }

                            onError {
                                it.printStackTrace()
                                ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "文件删除失败", mAct!!)
                            }
                        }
            }
        }
    }

    var progressDialog: ProgressDialog? = null
    fun showProgress(msg: String) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(mAct)
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
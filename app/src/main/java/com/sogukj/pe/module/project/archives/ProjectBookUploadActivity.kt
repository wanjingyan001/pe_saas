package com.sogukj.pe.module.project.archives

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.bean.DirBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.module.fileSelector.FileMainActivity
import com.sogukj.pe.module.fund.FundUploadActivity
import com.sogukj.pe.module.project.ProjectListFragment
import com.sogukj.pe.peExtended.defaultHeadImg
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.InfoService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_project_book_upload.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.find
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ProjectBookUploadActivity : ToolbarActivity() {
    val filterList = TreeMap<Int, String>()
    lateinit var project: ProjectBean
    lateinit var map: HashMap<Int, String>
    lateinit var uploadAdapter: UploadAdapter
    val uploadList = ArrayList<UploadBean>()
    var currentPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        map = intent.getSerializableExtra(Extras.MAP) as HashMap<Int, String>
        filterList.putAll(map)
        setContentView(R.layout.activity_project_book_upload)
        setBack(true)
        title = "项目文书上传"
        val user = Store.store.getUser(this)
        tv_user.text = user?.name

        var tmpInt = XmlDb.open(context).get(Extras.TYPE, "").toInt()
        var tmpStr = ""
        when (tmpInt) {
            ProjectListFragment.TYPE_CB -> tmpStr = "储备"
            ProjectListFragment.TYPE_LX -> tmpStr = "立项"
            ProjectListFragment.TYPE_YT -> tmpStr = "已投"
            ProjectListFragment.TYPE_DY -> tmpStr = "调研"
            ProjectListFragment.TYPE_TC -> tmpStr = "退出"
        }
        tv_step.text = tmpStr

        if (user?.headImage().isNullOrEmpty()) {
            val ch = user?.name?.first()
            iv_user.setChar(ch)
        } else {
            Glide.with(this)
                    .load(MyGlideUrl(user?.headImage()))
                    .apply(RequestOptions().error(defaultHeadImg()))
                    .into(iv_user)
        }

        tv_title.text = project.name
        uploadList.add(UploadBean())
        uploadAdapter = UploadAdapter(this, uploadList)
        project_upload_list.layoutManager = LinearLayoutManager(this)
        project_upload_list.adapter = uploadAdapter

        loadDir()
    }

    override fun onPause() {
        super.onPause()
        hideProgress()
    }

    class UploadBean {
        var file: String? = null
        var group: Int? = null//分类
        var type: Int? = null//标签
        var date: Long? = null
        var isSuccess: Boolean = false
    }

    fun doCheck(bean: UploadBean): Boolean {
        if (bean.file.isNullOrEmpty()) {
            showCustomToast(R.drawable.icon_toast_common, "请选择文件")
            return false
        }
        if (null == bean.group) {
            showCustomToast(R.drawable.icon_toast_common, "请选择分类")
            return false
        }
        if (null == bean.type) {
            showCustomToast(R.drawable.icon_toast_common, "请选择标签")
            return false
        }
        return true
    }

    private fun doSave(uploadBean: UploadBean, btn_upload: Button) {
        if (!doCheck(uploadBean)) {
            return
        }
        if (uploadBean.isSuccess) {
            return
        }
        btn_upload.isEnabled = false
        btn_upload.backgroundColor = Color.GRAY
        val file = File(uploadBean.file)
        var body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("type", "1")
                .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("*/*"), file))
                .addFormDataPart("company_id", project.company_id.toString())
                .addFormDataPart("dir_id", uploadBean.group.toString())
                .addFormDataPart("fileClass", uploadBean.type.toString())
                .build()

        showProgress("正在上传")
        SoguApi.getService(application,InfoService::class.java)
                .uploadArchives(body)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        uploadBean.isSuccess = true
                        showCustomToast(R.drawable.icon_toast_success, "上传成功")
                        hideProgress()
                        uploadAdapter.notifyDataSetChanged()
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        hideProgress()
                        btn_upload.isEnabled = true
                        btn_upload.backgroundResource = R.drawable.bg_btn_blue
                    }
                }, { e ->
                    Trace.e(e)
                    hideProgress()
                    showCustomToast(R.drawable.icon_toast_fail, "上传失败")
                    btn_upload.isEnabled = true
                    btn_upload.backgroundResource = R.drawable.bg_btn_blue
                })
    }

    var dirList = ArrayList<DirBean>()

    fun loadDir() {
        SoguApi.getService(application,InfoService::class.java)
                .showCatalog(1, project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload?.payload?.forEach {
                            dirList.add(it)
                        }
                        for (dir in dirList) {
                            uploadAdapter.items.add(dir.dirname)
                        }
                    } else {
                    }
                }, { e ->
                })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null && resultCode === Activity.RESULT_OK) {
            if (requestCode == FundUploadActivity.REQ_SELECT_FILE) {
                data.let {
                    val paths = it.getStringArrayListExtra(Extras.LIST)
                    paths.forEach {
                        val bean = UploadBean()
                        bean.file = it
                        bean.date = System.currentTimeMillis()
                        uploadList.add(uploadList.size - 1, bean)
                    }
                    uploadAdapter.notifyDataSetChanged()
                }
            } else if (requestCode == FundUploadActivity.REQ_CHANGE_FILE) {
                data.let {
                    val path = it.getStringExtra(Extras.DATA)
                    uploadList[currentPosition].file = path
                    uploadList[currentPosition].date = System.currentTimeMillis()
                    uploadAdapter.notifyDataSetChanged()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        val REQ_SELECT_FILE = 0xf0
        val REQ_CHANGE_FILE = 0xf1
        fun start(ctx: Activity?, project: ProjectBean, filterList: HashMap<Int, String>) {
            val intent = Intent(ctx, ProjectBookUploadActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(Extras.MAP, filterList)
            ctx?.startActivity(intent)
        }
    }

    inner class UploadAdapter(val context: Context, val uploadList: List<UploadBean>) : RecyclerView.Adapter<UploadAdapter.FileHolder>() {

        val items = ArrayList<String?>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadAdapter.FileHolder {
            return FileHolder(LayoutInflater.from(context).inflate(R.layout.item_fund_upload_new, parent, false))
        }

        override fun onBindViewHolder(holder: UploadAdapter.FileHolder, position: Int) {
            holder.setData(uploadList[position], position)
        }

        override fun getItemCount(): Int = uploadList.size

        override fun getItemViewType(position: Int): Int {
            return if (uploadList.get(position).isSuccess) 0 else 1
        }

        inner class FileHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ll_upload_full = itemView.find<LinearLayout>(R.id.ll_upload_full)//已选择（可能是上传完毕的，也可能是准备换文件的）
            val tv_file = itemView.find<TextView>(R.id.tv_file)
            val tv_time = itemView.find<TextView>(R.id.tv_time)
            val ll_upload_empty = itemView.find<LinearLayout>(R.id.ll_upload_empty)//未选择图片
            val llgroup = itemView.find<LinearLayout>(R.id.llgroup)//分类
            val tv_group = itemView.find<TextView>(R.id.tv_group)
            val tag_layout = itemView.find<LinearLayout>(R.id.tag_layout)//标签
            val tv_class = itemView.find<TextView>(R.id.tv_class)
            val btn_upload = itemView.find<Button>(R.id.btn_upload)

            fun setData(data: UploadBean, position: Int) {
                if (data.file.isNullOrEmpty()) {
                    ll_upload_empty.visibility = View.VISIBLE
                    ll_upload_full.visibility = View.GONE
                    //选择文件
                    ll_upload_empty.setOnClickListener {
                        showProgress("正在读取内存文件")
                        FileMainActivity.start(this@ProjectBookUploadActivity, maxSize = 1, requestCode = REQ_SELECT_FILE)
                    }
                } else {
                    ll_upload_empty.visibility = View.GONE
                    ll_upload_full.visibility = View.VISIBLE
                    var file = File(data.file)
                    tv_file.text = file.name
                    tv_time.text = Utils.getTime(data.date!!, "yyyy-MM-dd HH:mm")
                    //替换文件
                    ll_upload_full.setOnClickListener {
                        if (data.isSuccess) {
                            return@setOnClickListener
                        }
                        currentPosition = position
                        showProgress("正在读取内存文件")
                        FileMainActivity.start(this@ProjectBookUploadActivity, 1, true, REQ_CHANGE_FILE)
                    }
                }
                //分类点击
                tv_group.text = ""
                if (data.group != null) {
                    for (dir in dirList) {
                        if (dir.dir_id == data.group) {
                            tv_group.text = dir.dirname
                        }
                    }
                }
                llgroup.setOnClickListener {
                    if (data.file.isNullOrEmpty()) {
                        showCustomToast(R.drawable.icon_toast_common, "请先选择文件")
                        return@setOnClickListener
                    }
                    if (data.isSuccess) {
                        return@setOnClickListener
                    }
                    if (items.size == 0) {
                        showCustomToast(R.drawable.icon_toast_common, "分类信息未加载，请检查网络")
                        return@setOnClickListener
                    }
                    MaterialDialog.Builder(this@ProjectBookUploadActivity)
                            .theme(Theme.LIGHT)
                            .items(items)
                            .canceledOnTouchOutside(true)
                            .itemsCallbackSingleChoice(-1, MaterialDialog.ListCallbackSingleChoice { dialog, v, p, s ->
                                if (p == -1) return@ListCallbackSingleChoice false
                                tv_group.text = items[p]
                                data.group = dirList[p].dir_id
                                dialog?.dismiss()
                                true
                            })
                            .show()
                }
                //标签点击
                tv_class.text = ""
                val rMap = HashMap<String, Int>()
                map.entries.forEach { e -> rMap.put(e.value, e.key) }
                if (data.type != null) {
                    for ((k, v) in rMap) {
                        if (v == data.type) {
                            tv_class.text = k
                        }
                    }
                }
                tag_layout.setOnClickListener {
                    if (data.file.isNullOrEmpty()) {
                        showCustomToast(R.drawable.icon_toast_common, "请先选择文件")
                        return@setOnClickListener
                    }
                    if (data.isSuccess) {
                        return@setOnClickListener
                    }
                    val items = ArrayList<String?>()
                    items.addAll(map.values)
                    MaterialDialog.Builder(this@ProjectBookUploadActivity)
                            .theme(Theme.LIGHT)
                            .items(items)
                            .canceledOnTouchOutside(true)
                            .itemsCallbackSingleChoice(-1, MaterialDialog.ListCallbackSingleChoice { dialog, v, p, s ->
                                if (p == -1) return@ListCallbackSingleChoice false
                                tv_class.text = items[p]
                                data.type = rMap[items[p]]
                                dialog?.dismiss()
                                true
                            })
                            .show()
                }
                // button
                if (data.isSuccess) {
                    btn_upload.visibility = View.GONE
                } else {
                    btn_upload.visibility = View.VISIBLE
                    btn_upload.isEnabled = true
                    btn_upload.backgroundResource = R.drawable.bg_btn_blue
                    btn_upload.setOnClickListener {
                        if (doCheck(data)) {
                            handler.postDelayed({
                                doSave(data, btn_upload)
                            }, 100)
                        }
                    }
                }
            }
        }
    }
}

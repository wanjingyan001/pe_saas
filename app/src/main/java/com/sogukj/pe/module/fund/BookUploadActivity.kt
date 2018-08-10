package com.sogukj.pe.module.fund

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
import com.sogukj.pe.bean.DirBean
import com.sogukj.pe.bean.FileListBean
import com.sogukj.pe.module.fileSelector.FileMainActivity
import com.sogukj.pe.peExtended.defaultHeadImg
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.FundService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_book_upload.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.find
import java.io.File
import java.util.HashMap

class BookUploadActivity : ToolbarActivity() {

    companion object {
        val REQ_SELECT_FILE = 0xf0
        val REQ_CHANGE_FILE = 0xf1
        fun start(ctx: Activity?, name: String, type: Int, id: Int, stage: String, dir_id: Int? = null, dirName: String) {
            val intent = Intent(ctx, BookUploadActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.NAME, name)
            intent.putExtra(Extras.ID, id)
            intent.putExtra(Extras.TITLE, dirName)
            intent.putExtra(Extras.STAGE, stage)
            intent.putExtra(Extras.DIR_ID, dir_id)
            ctx?.startActivityForResult(intent, 0x126)
        }
    }

    lateinit var uploadAdapter: UploadAdapter
    val uploadList = ArrayList<UploadBean>()
    var currentPosition = 0
    var type: Int? = null
    var id: Int? = null
    var map = HashMap<Int, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_upload)
        setBack(true)
        title = intent.getStringExtra(Extras.NAME)
        type = intent.getIntExtra(Extras.TYPE, 0)
        id = intent.getIntExtra(Extras.ID, 0)
        tv_step.text = "阶段：${intent.getStringExtra(Extras.STAGE)}"

        val user = Store.store.getUser(this)
        tv_user.text = user?.name
        if (user?.headImage().isNullOrEmpty()) {
            val ch = user?.name?.first()
            iv_user.setChar(ch)
        } else {
            Glide.with(this)
                    .load(MyGlideUrl(user?.headImage()))
                    .apply(RequestOptions().error(defaultHeadImg()))
                    .into(iv_user)
        }
        uploadList.add(UploadBean())
        uploadAdapter = UploadAdapter(this, uploadList)
        upload_file_list.layoutManager = LinearLayoutManager(this)
        upload_file_list.adapter = uploadAdapter

        loadDir()

        if (type == 1) {
            SoguApi.getService(application, FundService::class.java)
                    .projectFilter()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk && payload.payload != null) {
                            map.clear()
                            map.putAll(payload.payload!!)
                        } else
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }, { e ->
                        Trace.e(e)
                    })
        }
    }

    class UploadBean {
        var file: String? = null
        var group: Int? = null//分类
        var type: Int? = null//标签,基金不需要
        var date: Long? = null
        var isSuccess: Boolean = false
    }

    fun doCheck(bean: UploadBean): Boolean {
        if (bean.file.isNullOrEmpty()) {
            showCustomToast(R.drawable.icon_toast_common, "请选择文件")
            return false
        }
        if (type == 1) {
            if (null == bean.type) {
                showCustomToast(R.drawable.icon_toast_common, "请选择标签")
                return false
            }
        }
        if (null == bean.group) {
            showCustomToast(R.drawable.icon_toast_common, "请选择分类")
            return false
        }
        return true
    }

    private fun doSave(bean: UploadBean, btn_upload: Button) {
        if (!doCheck(bean)) {
            return
        }
        if (bean.isSuccess) {
            return
        }
        btn_upload.isEnabled = false
        btn_upload.backgroundColor = Color.GRAY
        val file = File(bean.file)
        var builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("type", type.toString())
                .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("*/*"), file))
                .addFormDataPart("company_id", id.toString())
                .addFormDataPart("dir_id", bean.group.toString())
        //.addFormDataPart("fileClass", bean.type.toString())
        var body: MultipartBody
        if (type == 1) {
            body = builder.addFormDataPart("fileClass", bean.type.toString()).build()
        } else {
            body = builder.build()
        }

        showProgress("正在上传")
        SoguApi.getService(application, FundService::class.java)
                .uploadArchives(body)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        bean.isSuccess = true
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

    var dirList = ArrayList<FileListBean>()

    fun loadDir() {
        var dir_id: Int? = null
        try {
            dir_id = intent.extras[Extras.DIR_ID] as Int
        } catch (e: Exception) {
            dir_id = null
        }
        var bean = FileListBean()
        bean.id = dir_id
        bean.dirname = intent.getStringExtra(Extras.TITLE)
        dirList.add(bean)
        uploadAdapter.items.add(bean.dirname)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null && resultCode === Activity.RESULT_OK) {
            if (requestCode == REQ_SELECT_FILE) {
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
            } else if (requestCode == REQ_CHANGE_FILE) {
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

    inner class UploadAdapter(val context: Context, val uploadList: List<UploadBean>) : RecyclerView.Adapter<UploadAdapter.FileHolder>() {
        val items = ArrayList<String?>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileHolder {
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
                        FileMainActivity.start(this@BookUploadActivity, maxSize = 1, requestCode = REQ_SELECT_FILE)
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
                        FileMainActivity.start(this@BookUploadActivity, 1, true, REQ_CHANGE_FILE)
                    }
                }
                //分类点击
                tv_group.text = ""
                if (data.group != null) {
                    for (dir in dirList) {
                        if (dir.id == data.group) {
                            tv_group.text = dir.dirname
                        }
                    }
                }
                if (items.size == 1) {
                    tv_group.text = items[0]
                    data.group = dirList[0].id
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
                    MaterialDialog.Builder(this@BookUploadActivity)
                            .theme(Theme.LIGHT)
                            .items(items)
                            .canceledOnTouchOutside(true)
                            .itemsCallbackSingleChoice(-1, MaterialDialog.ListCallbackSingleChoice { dialog, v, p, s ->
                                if (p == -1) return@ListCallbackSingleChoice false
                                tv_group.text = items[p]
                                data.group = dirList[p].id
                                dialog?.dismiss()
                                true
                            })
                            .show()
                }
                //标签点击
                if (type == 2) {
                    tag_layout.visibility = View.GONE
                } else {
                    data.type = 0
                    tag_layout.visibility = View.VISIBLE
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
                        MaterialDialog.Builder(this@BookUploadActivity)
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

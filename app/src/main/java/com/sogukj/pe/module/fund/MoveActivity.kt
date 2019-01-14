package com.sogukj.pe.module.fund

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.amap.api.mapcore.util.it
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.DateUtils
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.FileListBean
import com.sogukj.pe.peUtils.FileTypeUtils
import com.sogukj.pe.service.FundService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_move.*
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource

class MoveActivity : ToolbarActivity() {

    override val menuId: Int
        get() = R.menu.menu_mark

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val flag = super.onCreateOptionsMenu(menu)
        val menuMark = menu.findItem(R.id.action_mark) as MenuItem
        menuMark.title = "取消"
        return flag
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_mark -> {
                finish()
            }
        }
        return false
    }

    companion object {
        // bean  id  dirname
        fun start(ctx: Activity?, bean: FileListBean, company_id: Int, type: Int, size: Int) {
            val intent = Intent(ctx, MoveActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            intent.putExtra(Extras.COMPANY_ID, company_id)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.DATA2, size)
            ctx?.startActivityForResult(intent, 0x899)
        }
    }

    var company_id: Int? = null
    var type: Int? = null
    lateinit var mFileAdapter: RecyclerAdapter<FileListBean>
    var dstArr = ArrayList<FileListBean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_move)

        move.text = "移动(${intent.getIntExtra(Extras.DATA2, 0)})"

        company_id = intent.getIntExtra(Extras.COMPANY_ID, 0)
        type = intent.getIntExtra(Extras.TYPE, 0)
        var bean = intent.getSerializableExtra(Extras.DATA) as FileListBean

        title = bean.dirname
        dstRoute.text = "（已选：${bean.dirname}）"
        dstArr.add(bean)

        toolbar?.apply {
            val back = this.findViewById<View>(R.id.toolbar_back)
            val title1 = this.findViewById<TextView>(R.id.toolbar_title)
            back?.visibility = View.VISIBLE
            back?.setOnClickListener {
                if (dstArr.size > 1) {
                    dstArr.remove(dstArr.last())
                    title1?.text = dstArr.last().dirname
                    dstRoute.text = "（已选：${dstArr.last().dirname}）"
                    page = 1
                    loadFileList()
                } else {
                    finish()
                }
            }
        }

        kotlin.run {
            mFileAdapter = RecyclerAdapter<FileListBean>(this, { _adapter, parent, _type ->
                val convertView = _adapter.getView(R.layout.item_booklist, parent)
                object : RecyclerHolder<FileListBean>(convertView) {
                    val file_icon = convertView.find<ImageView>(R.id.file_icon)
                    val tvSummary = convertView.find<TextView>(R.id.tv_summary)
                    val tvFileSize = convertView.find<TextView>(R.id.tv_fileSize)
                    val tvTime = convertView.find<TextView>(R.id.tv_time)
                    val tvName = convertView.find<TextView>(R.id.tv_name)
                    val ivSelect = convertView.find<ImageView>(R.id.select)

                    override fun setData(view: View, data: FileListBean, position: Int) {
                        ivSelect.visibility = View.GONE
                        if (data.dirname.isNullOrEmpty()) {
                            file_icon.imageResource = FileTypeUtils.getFileType(data.doc_title).icon
                            tvSummary.text = data.doc_title
                            tvFileSize.text = data.fileSize
                            try {
                                tvTime.text = DateUtils.timesFormat(data.add_time, "yyyy-MM-dd HH:mm:ss")//1526294091
                            } catch (e: Exception) {
                                tvTime.visibility = View.GONE
                            }
                            tvName.text = data.submitter
                        } else {
                            file_icon.imageResource = R.drawable.folder_zip
                            tvSummary.text = data.dirname
                            tvFileSize.visibility = View.GONE
                            var str = ""
                            if (!data.edit_time.isNullOrEmpty()) {
                                str = "更新时间 ${data.edit_time} "
                            }
                            data.count?.apply {
                                if (this == 0) {
                                    str = "${str}无文件"
                                } else {
                                    str = "${str}${this}个文件"
                                }
                            }
                            tvTime.text = str
                            tvName.visibility = View.GONE
                        }
                    }

                }
            })
            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            fileList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            fileList.layoutManager = layoutManager
            fileList.adapter = mFileAdapter
            mFileAdapter.onItemClick = { v, p ->
                val data = mFileAdapter.getItem(p)
                page = 1
                title = bean.dirname
                dstRoute.text = "（已选：${bean.dirname}）"
                dstArr.add(data)
                loadFileList()
            }
        }

        refresh.setOnRefreshListener {
            page = 1
            loadFileList()
            refresh.finishRefresh(1000)
        }
        refresh.setOnLoadMoreListener {
            if (bean.id == null) {
                refresh.finishLoadMore(0)
                return@setOnLoadMoreListener
            }
            ++page
            loadFileList()
            refresh.finishLoadMore(1000)
        }

        page = 1
        loadFileList()

        newDir.setOnClickListener {
            val inflate = LayoutInflater.from(this).inflate(R.layout.layout_input_dialog, null)
            val dialog = MaterialDialog.Builder(this)
                    .customView(inflate, false)
                    .cancelable(true)
                    .build()
            dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val title = inflate.find<TextView>(R.id.approval_comments_title)
            val commentInput = inflate.find<EditText>(R.id.approval_comments_input)
            val veto = inflate.find<TextView>(R.id.veto_comment)
            val confirm = inflate.find<TextView>(R.id.confirm_comment)
            commentInput.filters = Utils.getFilter(this)
            title.text = "请输入文件夹名字"
            title.textSize = 16.toFloat()
            commentInput.hint = ""
            veto.text = "取消"
            confirm.text = "确定"
            veto.setOnClickListener {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
            confirm.setOnClickListener {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
                SoguApi.getService(application, FundService::class.java)
                        .mkProjOrFundDir(type = type!!, dirname = commentInput.text.toString(), id = company_id!!, pid = dstArr.last().id)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ payload ->
                            if (payload.isOk) {
                                showCustomToast(R.drawable.icon_toast_success, "新建文件夹成功")
                                page = 1
                                loadFileList()
                            } else {
                                showCustomToast(R.drawable.icon_toast_fail, payload.message)
                            }
                        }, { e ->
                            Trace.e(e)
                            showCustomToast(R.drawable.icon_toast_fail, "新建文件夹失败")
                        })
            }
            dialog.show()
        }

        move.setOnClickListener {
            var id = dstArr.last().id
            if (id == null) {
                showCustomToast(R.drawable.icon_toast_common, "不能放在根目录下")
                return@setOnClickListener
            }
            var intent = Intent()
            intent.putExtra(Extras.ID, id)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun onBackPressed() {
        if (dstArr.size > 1) {
            dstArr.remove(dstArr.last())
            title = dstArr.last().dirname
            dstRoute.text = "（已选：${dstArr.last().dirname}）"
            page = 1
            loadFileList()
        } else {
            super.onBackPressed()
        }
    }

    var page = 1

    fun loadFileList() {

        var bean = dstArr.last()

        var map = HashMap<String, Any?>()
        map.put("fc_id", company_id)
        map.put("type", type)
        map.put("dir_id", bean.id)
        map.put("page", page)
        map.put("pageSize", 20)

        SoguApi.getService(application,FundService::class.java)
                .fileList(map)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1) {
                            mFileAdapter.dataList.clear()
                        }
                        payload?.payload?.forEach {
                            if (!it.dirname.isNullOrEmpty()) {
                                mFileAdapter.dataList.add(it)
                            }
                        }
                        mFileAdapter.notifyDataSetChanged()
                        if (mFileAdapter.dataList.size == 0) {
                            fileList.visibility = View.GONE
                            iv_empty.visibility = View.VISIBLE
                        } else {
                            fileList.visibility = View.VISIBLE
                            iv_empty.visibility = View.GONE
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_fail, "加载失败")
                    if (mFileAdapter.dataList.size == 0) {
                        fileList.visibility = View.GONE
                        iv_empty.visibility = View.VISIBLE
                    } else {
                        fileList.visibility = View.VISIBLE
                        iv_empty.visibility = View.GONE
                    }
                })
    }
}

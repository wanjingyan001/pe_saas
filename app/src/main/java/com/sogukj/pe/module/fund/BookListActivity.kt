package com.sogukj.pe.module.fund

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.alibaba.android.arouter.facade.annotation.Route
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.DateUtils
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.FileListBean
import com.sogukj.pe.bean.FundSmallBean
import com.sogukj.pe.module.other.OnlinePreviewActivity
import com.sogukj.pe.peUtils.FileTypeUtils
import com.sogukj.pe.service.FundService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_book_list.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.padding

@Route(path = ARouterPath.BookListActivity)
class BookListActivity : BaseActivity() {

    var company_id: Int? = null
    var dir_id: Int? = null
    var type: Int? = null
    lateinit var adapter: RecyclerAdapter<FileListBean>
    lateinit var mFileAdapter: RecyclerAdapter<FileListBean>

    var fundBean: FundSmallBean? = null

    companion object {
        // type	（1=>项目，2=>基金）
        // name 和 stage 是上传界面要用的
        fun start(ctx: Activity?, company_id: Int, type: Int, dir_id: Int? = null, dir_name: String, name: String, stage: String) {
            val intent = Intent(ctx, BookListActivity::class.java)
            intent.putExtra(Extras.COMPANY_ID, company_id)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.DIR_ID, dir_id)
            intent.putExtra(Extras.TITLE, dir_name)
            intent.putExtra(Extras.NAME, name)
            intent.putExtra(Extras.STAGE, stage)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_list)

        company_id = intent.getIntExtra(Extras.COMPANY_ID, 0)
        type = intent.getIntExtra(Extras.TYPE, 0)
        try {
            dir_id = intent.extras[Extras.DIR_ID] as Int
        } catch (e: Exception) {
            dir_id = null
        }

        toolbar?.apply {
            val back = this.findViewById<FrameLayout>(R.id.toolbar_back)
            back?.visibility = View.VISIBLE
            back?.setOnClickListener {
                onBackPressed()
            }
        }
        toolbar_title.text = intent.getStringExtra(Extras.TITLE)

        toolbar_menu.setOnClickListener {
            if (dir_id == null) {
                addDir("请输入文件夹名字", { newDir() })
            } else {
                if (add_layout.visibility == View.VISIBLE) {
                    add_layout.visibility = View.GONE
                } else {
                    add_layout.visibility = View.VISIBLE
                    add_layout.setOnClickListener {
                        add_layout.visibility = View.GONE
                    }
                }
            }
        }
        toolbar?.setOnTouchListener { v, event ->
            if (add_layout.visibility == View.VISIBLE) {
                add_layout.visibility = View.GONE
            }
            true
        }
        start_chat.setOnClickListener {
            add_layout.visibility = View.GONE
            addDir("请输入文件夹名字", { newDir() })
        }
        scan.setOnClickListener {
            add_layout.visibility = View.GONE
            addFile()
        }

        //搜索   两个adapter内容一样
        kotlin.run {
            adapter = RecyclerAdapter<FileListBean>(this, { _adapter, parent, type ->
                val convertView = _adapter.getView(R.layout.item_booklist, parent) as View
                object : RecyclerHolder<FileListBean>(convertView) {
                    val file_icon = convertView.find<ImageView>(R.id.file_icon)
                    val tvSummary = convertView.find<TextView>(R.id.tv_summary)
                    val tvFileSize = convertView.find<TextView>(R.id.tv_fileSize)
                    val tvTime = convertView.find<TextView>(R.id.tv_time)
                    val tvName = convertView.find<TextView>(R.id.tv_name)
                    val ivSelect = convertView.find<ImageView>(R.id.select)
                    override fun setData(view: View, data: FileListBean, position: Int) {
                        ivSelect.visibility = View.INVISIBLE
                        if (data.dirname.isNullOrEmpty()) {
                            file_icon.imageResource = FileTypeUtils.getFileType(data.doc_title).icon
                            tvSummary.text = data?.doc_title
                            tvFileSize.text = data?.fileSize
                            try {
                                tvTime.text = DateUtils.timesFormat(data?.add_time, "yyyy-MM-dd HH:mm:ss")//1526294091
                            } catch (e: Exception) {
                                tvTime.visibility = View.GONE
                            }
                            tvName.text = data.submitter
                        } else {
                            file_icon.imageResource = R.drawable.folder_zip
                            tvSummary.text = data?.dirname
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
            recycler_result.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            recycler_result.layoutManager = layoutManager
            recycler_result.adapter = adapter
            adapter.onItemClick = { v, p ->
                val data = adapter.getItem(p);
                if (!TextUtils.isEmpty(data.url)) {
                    OnlinePreviewActivity.start(context, data.url!!, data.doc_title!!)
//                    val intent = Intent(Intent.ACTION_VIEW)
//                    intent.data = Uri.parse(data.url)
//                    startActivity(intent)
                }
            }
        }

        initSearchView()

        //文件列表
        kotlin.run {
            mFileAdapter = RecyclerAdapter<FileListBean>(this, { _adapter, parent, _type ->
                val convertView = _adapter.getView(R.layout.item_booklist, parent) as View
                object : RecyclerHolder<FileListBean>(convertView) {
                    val file_icon = convertView.find<ImageView>(R.id.file_icon)
                    val tvSummary = convertView.find<TextView>(R.id.tv_summary)
                    val tvFileSize = convertView.find<TextView>(R.id.tv_fileSize)
                    val tvTime = convertView.find<TextView>(R.id.tv_time)
                    val tvName = convertView.find<TextView>(R.id.tv_name)
                    val ivSelect = convertView.find<ImageView>(R.id.select)

                    fun selectState() {
                        ivSelect.imageResource = R.drawable.oval_2
                        ivSelect.padding = Utils.dpToPx(context, 10)
                        ivSelect.tag = 2
                    }

                    fun unSelectState() {
                        ivSelect.imageResource = R.drawable.oval_1
                        ivSelect.padding = Utils.dpToPx(context, 16)
                        ivSelect.tag = 1
                    }

                    override fun setData(view: View, data: FileListBean, position: Int) {
                        if (data.dirname.isNullOrEmpty()) {
                            file_icon.imageResource = FileTypeUtils.getFileType(data.doc_title).icon
                            tvSummary.text = data?.doc_title
                            tvFileSize.text = data?.fileSize
                            try {
                                tvTime.text = DateUtils.timesFormat(data?.add_time, "yyyy-MM-dd HH:mm:ss")//1526294091
                            } catch (e: Exception) {
                                tvTime.visibility = View.GONE
                            }
                            tvName.text = data.submitter

                            unSelectState()

                            if (selectMode) {
                                if (selectAll) {
                                    selectState()
                                } else {
                                    unSelectState()
                                }
                            }
                        } else {
                            file_icon.imageResource = R.drawable.folder_zip
                            if (data.dirname!!.contains("默认")) {
                                tvSummary.text = data.dirname!!.replace("（默认）", "")
                                val drawable = resources.getDrawable(R.drawable.icon_default_folder)
                                drawable.setBounds(0,0,dip(20),dip(12))
//                                tvSummary.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_default_folder, 0)
                                tvSummary.setCompoundDrawables(null,null,drawable,null)
                                tvSummary.clipBounds
                                tvSummary.compoundDrawablePadding = dip(8)
                            } else {
                                tvSummary.text = data.dirname
                            }
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

                            if (data.amend == 1) {
                                unSelectState()
                            } else {
                                ivSelect.visibility = View.INVISIBLE
                                ivSelect.tag = 0
                            }

                            if (selectMode) {
                                if (selectAll) {
                                    if (data.amend == 1) {
                                        selectState()
                                    } else {
                                        ivSelect.visibility = View.INVISIBLE
                                        ivSelect.tag = 0
                                    }
                                } else {
                                    if (data.amend == 1) {
                                        unSelectState()
                                    } else {
                                        ivSelect.visibility = View.INVISIBLE
                                        ivSelect.tag = 0
                                    }
                                }
                            }
                        }

                        ivSelect.setOnClickListener {
                            if (ivSelect.tag == 0) {
                                return@setOnClickListener
                            }
                            if (ivSelect.tag == 1) {
                                ivSelect.imageResource = R.drawable.oval_2
                                ivSelect.padding = Utils.dpToPx(context, 10)
                                ivSelect.tag = 2
                                if (!selectMode) {
                                    enterSelectMode()
                                }
                                selected.add(data)
                                judgeState()
                            } else if (ivSelect.tag == 2) {
                                ivSelect.imageResource = R.drawable.oval_1
                                ivSelect.padding = Utils.dpToPx(context, 16)
                                ivSelect.tag = 1
                                if (selectMode) {
                                    selected.remove(data)
                                    judgeState()
                                }
                            }
                        }

                        (file_icon.parent as LinearLayout).setOnClickListener {
                            if (selectMode) {
                                return@setOnClickListener
                            }
                            if (data.dirname.isNullOrEmpty()) {
                                if (!TextUtils.isEmpty(data.url)) {
                                    OnlinePreviewActivity.start(context, data.url!!, data.doc_title!!)
//                                    val intent = Intent(Intent.ACTION_VIEW)
//                                    intent.data = Uri.parse(data.url)
//                                    startActivity(intent)
                                }
                            } else {
                                BookListActivity.start(context, company_id!!, type!!, data.id, data.dirname!!, intent.getStringExtra(Extras.NAME), intent.getStringExtra(Extras.STAGE))
                            }
                        }
                    }

                }
            })
            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            fileList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            fileList.layoutManager = layoutManager
            fileList.adapter = mFileAdapter
        }

        refresh.setOnRefreshListener {
            if (selectMode) {
                refresh.finishRefresh(0)
                return@setOnRefreshListener
            }
            page = 1
            loadFileList()
            refresh.finishRefresh(1000)
        }
        refresh.setOnLoadMoreListener {
            if (selectMode) {
                refresh.finishLoadMore(0)
                return@setOnLoadMoreListener
            }
            if (dir_id == null) {
                refresh.finishLoadMore(0)
                return@setOnLoadMoreListener
            }
            ++page
            loadFileList()
            refresh.finishLoadMore(1000)
        }
    }

    fun judgeState() {
        //判断是否可以  "删除", "重命名", "移动"
        subTitle.text = "已选择${selected.size}个文件"
        // "删除"   文件夹
        // "重命名"  文件夹
        // "移动"  文件
        var delete = false
        if (selected.size == 1 && !selected.get(0).dirname.isNullOrEmpty() && selected.get(0).amend == 1)
        //一个文件夹
            delete = true
        else {
            //多个文件
            for (item in selected) {
                if (item.dirname.isNullOrEmpty()) {
                    delete = true
                } else {
                    delete = false
                    break
                }
            }
        }
        var rename = if (selected.size == 1 && !selected.get(0).dirname.isNullOrEmpty() && selected.get(0).amend == 1) true else false
        var move = true
        for (item in selected) {
            if (!item.dirname.isNullOrEmpty()) {
                move = false
                break
            }
        }
        bottomBar.update(arrayListOf(delete, rename, move))//
        if (selected.size == 0) {
            restore()
        }
    }

    internal var selectMode = false
    internal var selected = ArrayList<FileListBean>()
    internal var selectAll = false

    fun enterSelectMode() {
        toolbar?.visibility = View.GONE
        toolbar2.visibility = View.VISIBLE
        bottomBar.visibility = View.VISIBLE
        selectMode = true
        bottomBar.setDataSource(arrayListOf("删除", "重命名", "移动"), arrayListOf(R.drawable.bb_delete, R.drawable.bb_rename, R.drawable.bb_move))

        //全选
        tvSelect.setOnClickListener {
            if (selectAll) {
                tvSelect.text = "全选"
                selectAll = false
                mFileAdapter.notifyDataSetChanged()

                selected.clear()
                judgeState()
            } else {
                tvSelect.text = "全不选"
                selectAll = true
                mFileAdapter.notifyDataSetChanged()

                selected.clear()
                for (item in mFileAdapter.dataList) {
                    if (item.dirname.isNullOrEmpty()) {
                        selected.add(item)
                    } else {
                        if (item.amend == 1) {
                            selected.add(item)
                        }
                    }
                }
                judgeState()
            }
        }
        //取消
        cancel.setOnClickListener {
            restore()
        }
        //删除
        bottomBar.root.getChildAt(0).setOnClickListener {
            if (!bottomBar.actives[0]) {
                return@setOnClickListener
            }
            if (selected.size == 1 && !selected.get(0).dirname.isNullOrEmpty()) {
                var map = HashMap<String, Any>()
                map.put("type", type!!)
                map.put("dirId", selected.get(0).id!!)
                SoguApi.getService(application, FundService::class.java)
                        .delDir(map)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ payload ->
                            if (payload.isOk) {
                                showCustomToast(R.drawable.icon_toast_success, "删除成功")
                                restore()
                                page = 1
                                loadFileList()
                            } else {
                                showCustomToast(R.drawable.icon_toast_fail, payload.message)
                            }
                        }, { e ->
                            Trace.e(e)
                            showCustomToast(R.drawable.icon_toast_fail, "删除失败")
                        })
            } else {

                var fileIdStr = ""
                for (item in selected) {
                    fileIdStr = "${fileIdStr}${item.id},"
                }
                fileIdStr = fileIdStr.removeSuffix(",")

                var map = HashMap<String, Any>()
                map.put("idStr", fileIdStr)
                SoguApi.getService(application, FundService::class.java)
                        .delFile(map)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ payload ->
                            if (payload.isOk) {
                                showCustomToast(R.drawable.icon_toast_success, "删除成功")
                                restore()
                                page = 1
                                loadFileList()
                            } else {
                                showCustomToast(R.drawable.icon_toast_fail, payload.message)
                            }
                        }, { e ->
                            Trace.e(e)
                            showCustomToast(R.drawable.icon_toast_fail, "删除失败")
                        })
            }

        }
        //重命名
        bottomBar.root.getChildAt(1).setOnClickListener {
            if (!bottomBar.actives[1]) {
                return@setOnClickListener
            }
            if (selected.size != 1) {
                return@setOnClickListener
            }
            addDir("请输入新的文件夹名字", { editDir() }, selected.get(0).dirname)
        }
        //移动
        bottomBar.root.getChildAt(2).setOnClickListener {
            if (!bottomBar.actives[2]) {
                return@setOnClickListener
            }
            var bean = FileListBean()
            bean.id = null
            bean.dirname = if (type == 1) "项目文书" else "基金文书"
            MoveActivity.start(context, bean, company_id!!, type!!, selected.size)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x899) {
            if (data != null) {
                var fileIdStr = ""
                for (item in selected) {
                    fileIdStr = "${fileIdStr}${item.id},"
                }
                fileIdStr = fileIdStr.removeSuffix(",")

                var map = HashMap<String, Any>()
                map.put("type", type!!)
                map.put("fileIdStr", fileIdStr)
                map.put("newDirId", data.getIntExtra(Extras.ID, -1))
                SoguApi.getService(application, FundService::class.java)
                        .moveFile(map)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ payload ->
                            if (payload.isOk) {
                                showCustomToast(R.drawable.icon_toast_success, "移动成功")
                                restore()
                                page = 1
                                loadFileList()
                            } else {
                                showCustomToast(R.drawable.icon_toast_fail, payload.message)
                            }
                        }, { e ->
                            Trace.e(e)
                            showCustomToast(R.drawable.icon_toast_fail, "移动失败")
                        })
            }
            restore()
        }
    }

    fun restore() {
        selectMode = false
        toolbar?.visibility = View.VISIBLE
        selected.clear()
        toolbar2.visibility = View.GONE
        bottomBar.visibility = View.GONE
        mFileAdapter.notifyDataSetChanged()
    }

    fun addDir(titleStr: String, method: () -> Unit, oldName: String? = null) {
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
        //commentInput.filters = Utils.getFilter(this)
        if (!oldName.isNullOrEmpty()) {
            commentInput.setText(oldName)
            commentInput.setSelection(oldName!!.length)
        }
        title.text = titleStr
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
            newDirName = commentInput.text.toString()
            method()
        }
        dialog.show()
        commentInput.postDelayed( {
            commentInput.isFocusable = true
            commentInput.isFocusableInTouchMode = true
            commentInput.requestFocus()
            Utils.toggleSoftInput(this, commentInput)
        },50)
    }

    var newDirName = ""

    fun newDir() {
        SoguApi.getService(application, FundService::class.java)
                .mkProjOrFundDir(type = type!!, dirname = newDirName, id = company_id!!, pid = dir_id)
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

    fun editDir() {
        var map = HashMap<String, Any>()
        map.put("type", type!!)
        map.put("dirId", selected.get(0).id!!)
        map.put("dirname", newDirName)
        SoguApi.getService(application, FundService::class.java)
                .editDirname(map)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showCustomToast(R.drawable.icon_toast_success, "修改成功")
                        restore()
                        page = 1
                        loadFileList()
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_fail, "修改失败")
                })
    }

    fun addFile() {
        BookUploadActivity.start(context, intent.getStringExtra(Extras.NAME), type!!, company_id!!, intent.getStringExtra(Extras.STAGE), dir_id, intent.getStringExtra(Extras.TITLE))
    }

    override fun onResume() {
        super.onResume()
        page = 1
        loadFileList()
    }

    fun loadFileList() {

        var map = HashMap<String, Any?>()
        map.put("fc_id", company_id)
        map.put("type", type)
        map.put("dir_id", dir_id)
        map.put("page", page)
        map.put("pageSize", 20)

        SoguApi.getService(application, FundService::class.java)
                .fileList(map)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1) {
                            mFileAdapter.dataList.clear()
                        }
                        payload?.payload?.apply {
                            mFileAdapter.dataList.addAll(this)
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

    var searchKey = ""

    private fun initSearchView() {
        tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_search1, 0))
        search_edt.filters = Utils.getFilter(context)
        search_edt.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus || search_edt.textStr.isNotEmpty()) {
                search_hint.visibility = View.GONE
                search_icon.visibility = View.VISIBLE
                delete1.visibility = View.VISIBLE
            } else {
                search_hint.visibility = View.VISIBLE
                search_icon.visibility = View.GONE
                delete1.visibility = View.GONE
                search_edt.clearFocus()
            }
        }
        search_edt.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchKey = search_edt.text.toString()
                doSearch()
                true
            } else {
                false
            }
        }
        delete1.setOnClickListener {
            Utils.toggleSoftInput(context, search_edt)
            search_edt.setText("")
            search_edt.clearFocus()
            ll_result.visibility = View.GONE
        }
        search_edt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!search_edt.text.isNullOrEmpty()) {
                    delete1.visibility = View.VISIBLE
                } else {
                    delete1.visibility = View.GONE
                }
                if (search_edt.text.toString().isEmpty()) {
                    searchKey = ""
                    ll_result.visibility = View.GONE
                } else {
                    searchKey = search_edt.text.toString()
                    doSearch()
                }
            }
        })
    }

    var page = 1

    fun doSearch() {
        ll_result.visibility = View.VISIBLE

        var map = HashMap<String, Any?>()
        map.put("fc_id", company_id)
        map.put("type", type)
        map.put("dir_id", dir_id)
        map.put("page", 1)
        map.put("pageSize", 20)
        map.put("query", searchKey)

        SoguApi.getService(application, FundService::class.java)
                .fileList(map)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        //if (page == 1)
                        adapter.dataList.clear()
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_search1, 0))
                    showCustomToast(R.drawable.icon_toast_fail, "查询失败")
                }, {
                    tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_search1, adapter.dataList.size))
                    adapter.notifyDataSetChanged()
                })
    }

    override fun onPause() {
        super.onPause()
        if (null != search_edt){
            Utils.closeInput(this,search_edt)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

package com.sogukj.pe.module.fileSelector

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.IntRange
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.CoordinatorLayout
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.netease.nim.uikit.common.media.picker.loader.RotateTransformation
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.isNullOrEmpty
import com.sogukj.pe.baselibrary.Extended.setOnClickFastListener
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.module.fileSelector.DocumentsFragment.Companion.ALL_DOC
import com.sogukj.pe.module.fileSelector.DocumentsFragment.Companion.DING_TALK
import com.sogukj.pe.module.fileSelector.DocumentsFragment.Companion.PE_LOCAL
import com.sogukj.pe.module.fileSelector.DocumentsFragment.Companion.QQ_DOC
import com.sogukj.pe.module.fileSelector.DocumentsFragment.Companion.QQ_DOC_PATH
import com.sogukj.pe.module.fileSelector.DocumentsFragment.Companion.QQ_DOC_PATH1
import com.sogukj.pe.module.fileSelector.DocumentsFragment.Companion.WX_DOC
import com.sogukj.pe.module.fileSelector.DocumentsFragment.Companion.WX_DOC_PATH1
import com.sogukj.pe.module.fileSelector.DocumentsFragment.Companion.WX_DOC_PATH2
import com.sogukj.pe.peUtils.FileTypeUtils
import com.sogukj.pe.peUtils.FileUtil
import kotlinx.android.synthetic.main.activity_filter_file.*
import kotlinx.android.synthetic.main.layout_empty.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import java.io.File
import java.util.*
import kotlin.properties.Delegates

class FilterFileActivity : BaseActivity() {
    val selectedFile = ArrayList<File>()
    lateinit var files: MutableList<File>
    var maxSize: Int by Delegates.notNull()//最大选择数量
    var isReplace: Boolean = false//文件选择替换功能(单选)
    var isForResult: Boolean = false////是否需要返回选择的文件
    var type: Int by Delegates.notNull()
    lateinit var fileType: FileUtil.FileType
    lateinit var mAdapter: RecyclerAdapter<File>
    val selectType = mutableSetOf<String>()
    private var page = 0

    companion object {
        fun start(context: Activity, maxSize: Int = 9, isReplace: Boolean, isForResult: Boolean,
                  @IntRange(from = 0, to = 4) directoryType: Int, fileType: FileUtil.FileType) {
            val intent = Intent(context, FilterFileActivity::class.java)
            intent.putExtra(Extras.DATA, maxSize)
            intent.putExtra(Extras.FLAG, isReplace)
            intent.putExtra(Extras.TYPE, isForResult)
            intent.putExtra(Extras.TYPE1, directoryType)
            intent.putExtra(Extras.TYPE2, fileType)
            context.startActivityForResult(intent, Extras.REQUESTCODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_file)
        Utils.setWindowStatusBarColor(this, R.color.white)
        maxSize = intent.getIntExtra(Extras.DATA, 9)
        isReplace = intent.getBooleanExtra(Extras.FLAG, false)
        isForResult = intent.getBooleanExtra(Extras.TYPE, false)
        type = intent.getIntExtra(Extras.TYPE1, 0)
        fileType = intent.getSerializableExtra(Extras.TYPE2) as FileUtil.FileType
        if (!isForResult) {
            operating.text = "删除"
        } else {
            operating.text = "发送"
        }

        mAdapter = RecyclerAdapter(this) { _adapter, parent, type ->
            FilterHolder(_adapter.getView(R.layout.item_document_list, parent))
        }
        initData()
        initRefresh()
        val dialog = initBottomDialog()
        filterImg.setOnClickFastListener {
            dialog.show()
        }
        mFileList.apply {
            layoutManager = LinearLayoutManager(this@FilterFileActivity)
            adapter = mAdapter
        }
        back.setOnClickFastListener {
            finish()
        }
        operating.setOnClickFastListener {
            doOperating()
        }
    }

    fun showSelectedInfo() {
        var size: Long = 0
        selectedFile.forEach {
            size += it.length()
        }
        selected_files_size.text = "已选择 : ${FileUtil.formatFileSize(size, FileUtil.SizeUnit.Auto)}"
        send_selected_files.isEnabled = selectedFile.size > 0
        send_selected_files.text = "选择(${selectedFile.size}/$maxSize)"
        send_selected_files.setOnClickFastListener {
            doOperating()
        }
    }

    private fun doOperating() {
        if (!isForResult) {
            //删除
            MaterialDialog.Builder(ctx)
                    .theme(Theme.LIGHT)
                    .title("警告")
                    .content("是否确认删除选中的文件")
                    .positiveText("确定")
                    .negativeText("取消")
                    .onPositive { dialog, _ ->
                        dialog.dismiss()
                        selectedFile.forEach {
                            if (it.exists()) {
                                if (files.contains(it)) {
                                    files.remove(it)
                                }
                                it.delete()
                                //通知列表刷新
                            }
                        }
                        selectedFile.clear()
                        initData()
                        refreshList(selectType)
                    }
                    .onNegative { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
        } else {
            //发送
            val intent = Intent()
            val paths = ArrayList<String>()
            selectedFile.forEach {
                paths.add(it.path)
            }
            intent.putExtra(Extras.LIST, paths)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun initData() {
        when (type) {
            PE_LOCAL -> {
                files = FileUtil.getFiles(FileUtil.getExternalFilesDir(applicationContext)).toMutableList()
                directory1.text = "本应用"
            }
            ALL_DOC -> {
                val list = FileUtil.getFiles(WX_DOC_PATH1)
                val list1 = FileUtil.getFiles(WX_DOC_PATH2)
                val list2 = FileUtil.getFiles(QQ_DOC_PATH)
                val list4 = FileUtil.getFiles(QQ_DOC_PATH1)
                val list5 = FileUtil.getDingFiles(ctx)
                val list3 = FileUtil.getFiles(FileUtil.getExternalFilesDir(applicationContext))
                files = list.plus(list1).plus(list2).plus(list3).plus(list4).plus(list5).toMutableList()
                directory1.text = "全部"
            }
            WX_DOC -> {
                val list = FileUtil.getFiles(WX_DOC_PATH1)
                val list1 = FileUtil.getFiles(WX_DOC_PATH2)
                files = list.plus(list1).toMutableList()
                directory1.text = "微信"
            }
            QQ_DOC -> {
                val list = FileUtil.getFiles(QQ_DOC_PATH)
                val list1 = FileUtil.getFiles(QQ_DOC_PATH1)
                files = list.plus(list1).toMutableList()
                directory1.text = "QQ"
            }
            DING_TALK -> {
                val list = FileUtil.getDingFiles(ctx)
                files = list.toMutableList()
                directory1.text = "钉钉"
            }
        }
        when (fileType) {
            FileUtil.FileType.IMAGE -> {
                directory2.text = "图片"
            }
            FileUtil.FileType.VIDEO -> {
                directory2.text = "视频"
            }
            FileUtil.FileType.DOC -> {
                directory2.text = "文档"
            }
            FileUtil.FileType.ZIP -> {
                directory2.text = "压缩包"
            }
            FileUtil.FileType.OTHER->{
                directory2.text = "其他"
                filterImg.setVisible(false)
            }
            else -> {
            }
        }
        files = files.filter { FileUtil.getFileType(it) == fileType }.toMutableList()
        files.sortWith(Comparator { o1, o2 ->
            o2.lastModified().compareTo(o1.lastModified())
        })
    }

    private fun initRefresh() {
        refresh.apply {
            isEnableRefresh = false
            isEnableLoadMore = true
            isEnableAutoLoadMore = true
            isEnableOverScrollBounce = false
            isEnableScrollContentWhenLoaded = false
            setEnableFooterTranslationContent(false)
            setRefreshFooter(ClassicsFooter(ctx), 0, 0)
            setDisableContentWhenLoading(true)
            setOnLoadMoreListener {
                page += 1
                refreshFileList()
            }
        }
    }

    private fun loadFiles(page: Int): MutableList<File> {
        val start = 0 + 20 * page
        val end = if (19 + 20 * page > files.size) files.size - 1 else 19 + 20 * page
        return if (files.isEmpty()) {
            mutableListOf()
        } else if (start > files.size || end < 0) {
            mutableListOf()
        } else {
            files.slice(IntRange(start, end)).toMutableList()
        }
    }

    private fun refreshFileList() {
        val list = loadFiles(page)
        if (page == 0){
            mAdapter.dataList.clear()
        }
        mAdapter.dataList.addAll(list)
        if (mAdapter.dataList.isNullOrEmpty()) {
            refresh.setVisible(false)
            iv_empty.setVisible(true)
        } else {
            refresh.setVisible(true)
            iv_empty.setVisible(false)
            mAdapter.notifyDataSetChanged()
        }
        refresh.finishLoadMore(0)
    }

    override fun onResume() {
        super.onResume()
        refreshFileList()
    }

    private fun initBottomDialog(): BottomSheetDialog {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogStyle)
        val view = layoutInflater.inflate(R.layout.layout_file_filter_dialog, null)
        val typeList = view.find<RecyclerView>(R.id.filterTypeList)
        val cancel = view.find<TextView>(R.id.cancel)
        val determine = view.find<TextView>(R.id.determine)
        val mAdapter = RecyclerAdapter<String>(this) { _adapter, parent, type ->
            val itemView = _adapter.getView(R.layout.item_filter_type_list, parent)
            object : RecyclerHolder<String>(itemView) {
                val typeName = itemView.find<TextView>(R.id.typeName)
                val typeSelect = itemView.find<ImageView>(R.id.typeSelect)
                override fun setData(view: View, data: String, position: Int) {
                    typeName.text = data.toUpperCase()
                    typeSelect.isSelected = selectType.contains(data)
                    view.setOnClickFastListener {
                        typeSelect.isSelected = !typeSelect.isSelected
                        if (selectType.contains(data)) {
                            selectType.remove(data)
                        } else {
                            selectType.add(data)
                        }
                    }
                }
            }
        }
        cancel.setOnClickFastListener {
            dialog.dismiss()
        }
        determine.setOnClickFastListener {
            dialog.dismiss()
            refreshList(selectType)
        }
        val types: MutableList<String> = fileType.extensions.toMutableList()
        mAdapter.dataList.addAll(types)
        typeList.apply {
            layoutManager = LinearLayoutManager(this@FilterFileActivity)
            adapter = mAdapter
        }
        dialog.setContentView(view)
        dialog.window.findViewById<FrameLayout>(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent)
        val parent = view.parent as View
        view.measure(0, 0)
        val behavior = BottomSheetBehavior.from(parent)
        behavior.peekHeight = view.measuredHeight
        val params = parent.layoutParams as CoordinatorLayout.LayoutParams
        params.leftMargin = Utils.dpToPx(this, 10)
        params.rightMargin = Utils.dpToPx(this, 10)
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        parent.layoutParams = params
        return dialog
    }


    private fun refreshList(types: MutableSet<String>) {
        val map = FileUtil.getFilesByType(files)
        val list = map[fileType]
        list?.let {
            val filter = it.filter { file ->
                if (types.isNotEmpty()) {
                    types.contains(FileUtil.getExtension(file.name))
                } else {
                    file != null
                }
            }
            val result = DiffUtil.calculateDiff(DiffCallBack(mAdapter.dataList, filter))
            result.dispatchUpdatesTo(mAdapter)
            mAdapter.dataList.clear()
            mAdapter.dataList.addAll(filter)
        }
    }

    inner class FilterHolder(view: View) : RecyclerHolder<File>(view) {
        val slector = view.find<ImageView>(R.id.selectIcon)
        val icon = view.find<ImageView>(R.id.file_icon)
        val name = view.find<TextView>(R.id.file_name)
        val info = view.find<TextView>(R.id.file_info)
        val point = view.find<ImageView>(R.id.red_point)
        override fun setData(view: View, data: File, position: Int) {
            slector.isSelected = selectedFile.contains(data)
            if (FileUtil.getFileType(data.absolutePath) != null) {
                Glide.with(context)
                        .load(data.absoluteFile)
                        .thumbnail(0.1f)
                        .apply(RequestOptions()
                                .centerCrop()
                                .error(R.drawable.icon_pic)
                                .transform(RotateTransformation(context, data.absolutePath)))
                        .into(icon)
            } else {
                icon.imageResource = FileTypeUtils.getFileType(data).icon
            }
            name.text = data.name
            val builder = StringBuilder()
            when {
                data.absolutePath.contains("QQ") -> builder.append("QQ  ")
                data.absolutePath.contains(context.packageName) -> builder.append("本应用  ")
                data.absolutePath.contains("DingTalk") -> builder.append("钉钉  ")
                else -> builder.append("微信  ")
            }
            val time = Utils.getTime(data.lastModified(), "yyyy/MM/dd HH:mm")
            builder.append(time.substring(2, time.length) + "  ")
            builder.append(FileUtil.formatFileSize(data.length(), FileUtil.SizeUnit.Auto))
            info.text = builder.toString()

            view.setOnClickListener {
                if (!isReplace) {
                    if (selectedFile.contains(data)) {
                        selectedFile.remove(data)
                        slector.isSelected = false
                    } else {
                        if (selectedFile.size < maxSize) {
                            slector.isSelected = true
                            selectedFile.add(data)
                        } else {
                            showCustomToast(R.drawable.icon_toast_common, "最多只能选择${maxSize}个")
                        }
                    }
                    showSelectedInfo()
                } else {
                    val intent = Intent()
                    intent.putExtra(Extras.DATA, data)
                    setResult(Extras.RESULTCODE, intent)
                    finish()
                }
            }
        }
    }
}

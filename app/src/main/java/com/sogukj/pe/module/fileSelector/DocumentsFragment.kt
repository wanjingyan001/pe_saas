package com.sogukj.pe.module.fileSelector


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.BasePageFragment
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.peUtils.FileTypeUtils
import com.sogukj.pe.peUtils.FileUtil
import kotlinx.android.synthetic.main.fragment_documents.*
import kotlinx.android.synthetic.main.layout_documents_header.view.*
import kotlinx.android.synthetic.main.layout_empty.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.find
import java.io.File
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [DocumentsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DocumentsFragment : BasePageFragment(), View.OnClickListener {

    private var type: Int? = null
    lateinit var adapter: RecyclerAdapter<File>
    lateinit var files: MutableList<File>
    lateinit var fileActivity: FileMainActivity
    lateinit var header: LinearLayout
    private var page = 0

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        fileActivity = activity as FileMainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            type = arguments!!.getInt(TYPE, 0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_documents, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = RecyclerAdapter(ctx) { _adpater, parent, type ->
            DocumentHolder(_adpater.getView(R.layout.item_document_list, parent))
        }
        initRefreshConfig()
        documentList.layoutManager = LinearLayoutManager(ctx)
        documentList.adapter = adapter
        header = find(R.id.documents_header)
        view.mPicManage.setOnClickListener(this)
        view.mVideoManage.setOnClickListener(this)
        view.mDocManage.setOnClickListener(this)
        view.mZipManage.setOnClickListener(this)
        view.mOtherManage.setOnClickListener(this)
    }

    override fun onFragmentVisibleChange(isVisible: Boolean) {
        super.onFragmentVisibleChange(isVisible)
        if (isVisible) {
            doAsync {
                getDirectoryFiles()
                uiThread {
                    getFiles()
                    refreshHead()
                }
            }
        }
    }

    private fun initRefreshConfig() {
        refresh.apply {
            setDragRate(0.1f)
            isEnableRefresh = false
            isEnableLoadMore = true
            isEnableAutoLoadMore = true
            isEnableOverScrollBounce = false
            isEnableScrollContentWhenLoaded = true
            setDisableContentWhenLoading(false)
            setEnableOverScrollDrag(true)
            setEnableFooterFollowWhenLoadFinished(false)
            setRefreshFooter(ClassicsFooter(ctx), 0, 0)
            setOnLoadMoreListener {
                page += 1
                getFiles()
            }
        }
    }


    private fun getFiles() {
        val dirFiles = loadFiles(page)
        val result = DiffUtil.calculateDiff(DiffCallBack(adapter.dataList, adapter.dataList.plus(dirFiles)))
        result.dispatchUpdatesTo(adapter)
        if (adapter.dataList.plus(dirFiles).isEmpty()) {
            header.setVisible(false)
            refresh.setVisible(false)
            iv_empty.setVisible(true)
        } else {
            header.setVisible(true)
            refresh.setVisible(true)
            iv_empty.setVisible(false)
            if (page == 0) {
                adapter.dataList.clear()
            }
            adapter.dataList.addAll(dirFiles)
        }
        adapter.notifyDataSetChanged()
        refresh.finishLoadMore()

    }

    fun deleteFile(selectedFile: List<File>) {
        selectedFile.forEach {
            if (files.contains(it) && it.exists()) {
                it.delete()
                adapter.dataList.remove(it)
            }
        }
        adapter.notifyDataSetChanged()
        doAsync {
            getDirectoryFiles()
            uiThread {
                refreshHead()
            }
        }
    }


    private fun getDirectoryFiles() {
        when (type) {
            PE_LOCAL -> {
                files = FileUtil.getFiles(FileUtil.getExternalFilesDir(fileActivity.applicationContext)).toMutableList()
            }
            ALL_DOC -> {
                val list = FileUtil.getFiles(WX_DOC_PATH1)
                val list1 = FileUtil.getFiles(WX_DOC_PATH2)
                val list2 = FileUtil.getFiles(QQ_DOC_PATH)
                val list4 = FileUtil.getFiles(QQ_DOC_PATH1)
                val list5 = FileUtil.getDingFiles(ctx)
                val list3 = FileUtil.getFiles(FileUtil.getExternalFilesDir(fileActivity.applicationContext))
                files = list.plus(list1).plus(list2).plus(list3).plus(list4).plus(list5).toMutableList()
            }
            WX_DOC -> {
                val list = FileUtil.getFiles(WX_DOC_PATH1)
                val list1 = FileUtil.getFiles(WX_DOC_PATH2)
                files = list.plus(list1).toMutableList()
            }
            QQ_DOC -> {
                val list = FileUtil.getFiles(QQ_DOC_PATH)
                val list1 = FileUtil.getFiles(QQ_DOC_PATH1)
                files = list.plus(list1).toMutableList()
            }
            DING_TALK -> {
                val list = FileUtil.getDingFiles(ctx)
                files = list.toMutableList()
            }
        }
        files.sortWith(Comparator { o1, o2 ->
            o2.lastModified().compareTo(o1.lastModified())
        })
    }

    @SuppressLint("SetTextI18n")
    private fun refreshHead() {
        header.find<TextView>(R.id.mPicNum).text = "(${files.filter { FileUtil.getFileType(it) == FileUtil.FileType.IMAGE }.size})"
        header.find<TextView>(R.id.mVideoNum).text = "(${files.filter { FileUtil.getFileType(it) == FileUtil.FileType.VIDEO }.size})"
        header.find<TextView>(R.id.mDocNum).text = "(${files.filter { FileUtil.getFileType(it) == FileUtil.FileType.DOC }.size})"
        header.find<TextView>(R.id.mZipNum).text = "(${files.filter { FileUtil.getFileType(it) == FileUtil.FileType.ZIP }.size})"
        header.find<TextView>(R.id.mOtherNum).text = "(${files.filter { FileUtil.getFileType(it) == FileUtil.FileType.OTHER }.size})"
    }

    private fun loadFiles(page: Int): MutableList<File> {
        val start = 0 + 20 * page
        val end = if (19 + 20 * page > files.size) files.size - 1 else 19 + 20 * page
        return if (files.isEmpty()) {
            mutableListOf()
        } else if (start > files.size || end < 0) {
            mutableListOf()
        } else {
            if (files.size>start && files.size>end) {
                files.slice(IntRange(start, end)).toMutableList()
            }else{
                mutableListOf()
            }
        }
    }

    fun search(searchStr: String) {
        if (searchStr.isNotEmpty()) {
            val filter = adapter.dataList.filter { file -> file.name.contains(searchStr) }
            val result = DiffUtil.calculateDiff(DiffCallBack(adapter.dataList, filter))
            result.dispatchUpdatesTo(adapter)
            adapter.dataList.clear()
            adapter.dataList.addAll(filter)
        } else {
            page = 0
            val dirFiles = loadFiles(page)
            if (adapter.dataList.plus(dirFiles).isEmpty()) {
                header.setVisible(false)
                documentList.setVisible(false)
                iv_empty.setVisible(true)
            } else {
                header.setVisible(true)
                documentList.setVisible(true)
                iv_empty.setVisible(false)
                val result = DiffUtil.calculateDiff(DiffCallBack(adapter.dataList, adapter.dataList.plus(dirFiles)))
                result.dispatchUpdatesTo(adapter)
                adapter.dataList.addAll(dirFiles)
            }
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.mPicManage -> {
                FilterFileActivity.start(fileActivity, fileActivity.maxSize,
                        fileActivity.isReplace, fileActivity.isForResult, type!!, FileUtil.FileType.IMAGE)
            }
            R.id.mVideoManage -> {
                FilterFileActivity.start(fileActivity, fileActivity.maxSize,
                        fileActivity.isReplace, fileActivity.isForResult, type!!, FileUtil.FileType.VIDEO)
            }
            R.id.mDocManage -> {
                FilterFileActivity.start(fileActivity, fileActivity.maxSize,
                        fileActivity.isReplace, fileActivity.isForResult, type!!, FileUtil.FileType.DOC)
            }
            R.id.mZipManage -> {
                FilterFileActivity.start(fileActivity, fileActivity.maxSize,
                        fileActivity.isReplace, fileActivity.isForResult, type!!, FileUtil.FileType.ZIP)
            }
            R.id.mOtherManage -> {
                FilterFileActivity.start(fileActivity, fileActivity.maxSize,
                        fileActivity.isReplace, fileActivity.isForResult, type!!, FileUtil.FileType.OTHER)
            }
            else -> {

            }
        }
    }

    companion object {
        private val TYPE = "type"
        private val ARG_PARAM2 = "param2"
        val QQ_DOC_PATH = Environment.getExternalStorageDirectory().path + "/tencent/QQfile_recv/"
        val QQ_DOC_PATH1 = Environment.getExternalStorageDirectory().path + "/tencent/QQ_Images/"
        val WX_DOC_PATH1 = Environment.getExternalStorageDirectory().path + "/tencent/MicroMsg/WeiXin/"
        val WX_DOC_PATH2 = Environment.getExternalStorageDirectory().path + "/tencent/MicroMsg/Download/"
        val DING_TALK_PATH = Environment.getExternalStorageDirectory().path + "/DingTalk/"
        val PE_LOCAL = 0
        val ALL_DOC = 1
        val WX_DOC = 2
        val QQ_DOC = 3
        val DING_TALK = 4


        fun newInstance(type: Int): DocumentsFragment {
            val fragment = DocumentsFragment()
            val args = Bundle()
            args.putInt(TYPE, type)
            fragment.arguments = args
            return fragment
        }
    }


    inner class DocumentHolder(view: View) : RecyclerHolder<File>(view) {
        val slector = view.find<ImageView>(R.id.selectIcon)
        val icon = view.find<ImageView>(R.id.file_icon)
        val name = view.find<TextView>(R.id.file_name)
        val info = view.find<TextView>(R.id.file_info)
        val point = view.find<ImageView>(R.id.red_point)
        override fun setData(view: View, data: File, position: Int) {
            if (position == 0) {
                point.visibility = View.VISIBLE
            } else {
                point.visibility = View.GONE
            }
            slector.isSelected = fileActivity.selectedFile.contains(data)
            if (FileUtil.getFileType(data.absolutePath) != null) {
                Glide.with(ctx)
                        .load(data.absolutePath)
                        .thumbnail(0.1f)
                        .apply(RequestOptions()
                                .centerCrop()
                                .error(R.drawable.icon_pic))
                        .into(icon)
            } else {
                icon.imageResource = FileTypeUtils.getFileType(data).icon
            }
            name.text = data.name
            val builder = StringBuilder()
            when {
                data.absolutePath.contains("QQ") -> builder.append("QQ  ")
                data.absolutePath.contains(ctx.packageName) -> builder.append("本应用  ")
                data.absolutePath.contains("DingTalk") -> builder.append("钉钉  ")
                else -> builder.append("微信  ")
            }
            val time = Utils.getTime(data.lastModified(), "yyyy/MM/dd HH:mm")
            builder.append(time.substring(2, time.length) + "  ")
            builder.append(FileUtil.formatFileSize(data.length(), FileUtil.SizeUnit.Auto))
            info.text = builder.toString()

            view.setOnClickListener {
                if (!fileActivity.isReplace) {
                    if (fileActivity.selectedFile.contains(data)) {
                        fileActivity.selectedFile.remove(data)
                        slector.isSelected = false
                    } else {
                        if (fileActivity.selectedFile.size < fileActivity.maxSize) {
                            slector.isSelected = true
                            fileActivity.selectedFile.add(data)
                        } else {
                            ctx.toast("最多只能选择${fileActivity.maxSize}个")
                        }
                    }
                    fileActivity.showSelectedInfo()
                } else {
                    fileActivity.sendChangeFile(data)
                }
            }
        }
    }
}

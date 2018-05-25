package com.sogukj.pe.module.project.archives

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.InputType
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.DateUtils
import com.sogukj.pe.baselibrary.utils.DownloadUtil
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.*
import com.sogukj.pe.baselibrary.widgets.ListAdapter
import com.sogukj.pe.bean.DirBean
import com.sogukj.pe.bean.FileListBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.ProjectBookBean
import com.sogukj.pe.peUtils.OpenFileUtil
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.InfoService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_list_project_book.*
import kotlinx.android.synthetic.main.search_view.*
import java.text.SimpleDateFormat


/**
 * Created by qinfei on 17/8/11.
 */
class ProjectBookActivity : ToolbarActivity(), SupportEmptyView {

    lateinit var adapter1: ListAdapter<ProjectBookBean>
    lateinit var adapter2: ListAdapter<ProjectBookBean>
    lateinit var adapter3: ListAdapter<ProjectBookBean>
    lateinit var adapter: RecyclerAdapter<ProjectBookBean>

    lateinit var project: ProjectBean
    val df = SimpleDateFormat("yyyy-MM-dd")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_project_book)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setBack(true)
        title = "项目文书"
        btn_upload.setOnClickListener {
            if (!filterList.isEmpty())
                ProjectBookUploadActivity.start(this, project, filterList)
        }
        adapter1 = ListAdapter { ProjectBookHolder() }
        adapter2 = ListAdapter { ProjectBookHolder() }
        adapter3 = ListAdapter { ProjectBookHolder() }
        adapter = RecyclerAdapter(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_project_book, parent)
            object : RecyclerHolder<ProjectBookBean>(convertView) {
                val tvSummary = convertView.findViewById<TextView>(R.id.tv_summary) as TextView
                val tvDate = convertView.findViewById<TextView>(R.id.tv_date) as TextView
                val tvTime = convertView.findViewById<TextView>(R.id.tv_time) as TextView
                val tvType = convertView.findViewById<TextView>(R.id.tv_type) as TextView
                val tvName = convertView.findViewById<TextView>(R.id.tv_name) as TextView
                override fun setData(view: View, data: ProjectBookBean, position: Int) {
                    tvSummary.text = data.doc_title
                    val strTime = data.add_time
                    tvTime.visibility = View.GONE
                    if (!TextUtils.isEmpty(strTime)) {
                        val strs = strTime!!.trim().split(" ")
                        if (!TextUtils.isEmpty(strs.getOrNull(1))) {
                            tvTime.visibility = View.VISIBLE
                        }
                        tvDate.text = strs.getOrNull(0)
                        tvTime.text = strs.getOrNull(1)
                    }
                    tvType.text = data.name
                    if (data.name.isNullOrEmpty()) {
                        tvType.visibility = View.INVISIBLE
                    }
                    tvName.text = data.submitter
                }

            }
        })

        run {
            stateDefault()
            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
//            recycler_result.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
            recycler_result.layoutManager = layoutManager
            recycler_result.adapter = adapter

            adapter.onItemClick = { v, p ->
                val data = adapter.getItem(p)
                if (!TextUtils.isEmpty(data.url)) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(data.url)
                    startActivity(intent)
                }
            }


            tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_search1, 0))
            search_view.iv_back.visibility = View.VISIBLE
            search_view.iv_back.setOnClickListener {
                onBackPressed()
            }
            search_view.tv_cancel.visibility = View.GONE
            search_view.tv_cancel.setOnClickListener {
                stateDefault()
                this.key = ""
                search_view.search = ""
            }
            search_view.onTextChange = { text ->
                handler.removeCallbacks(searchTask)
                handler.postDelayed(searchTask, 100)
            }
            et_search.setOnClickListener {
                stateSearch()
            }
            search_view.onSearch = { text ->
                if (null != text && !TextUtils.isEmpty(text))
                    doSearch(text)
            }
        }
        list1.adapter = adapter1
        list2.adapter = adapter2
        list3.adapter = adapter3
        list1.setOnItemClickListener { parent, view, position, id ->
            val data = adapter1.getItem(position)
            if (!TextUtils.isEmpty(data?.url)) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(data!!.url)
                startActivity(intent)
            }
        }
        list2.setOnItemClickListener { parent, view, position, id ->
            val data = adapter2.getItem(position)
            if (!TextUtils.isEmpty(data?.url)) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(data!!.url)
                startActivity(intent)
            }
        }
        list3.setOnItemClickListener { parent, view, position, id ->
            val data = adapter3.getItem(position)
            if (!TextUtils.isEmpty(data?.url)) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(data!!.url)
                startActivity(intent)
            }
        }
        tv_more1.setOnClickListener {
            ProjectBookMoreActivity.start(this, project, 1)
        }
        tv_more2.setOnClickListener {
            ProjectBookMoreActivity.start(this, project, 2)
        }
        tv_more3.setOnClickListener {
            ProjectBookMoreActivity.start(this, project, 3)
        }
        list1.setOnItemClickListener { parent, view, position, id ->
            val data = adapter1.dataList[position]
            //download(data.url!!, data.doc_title!!)
            if (!TextUtils.isEmpty(data.url)) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(data.url)
                startActivity(intent)
            }
            //ViewActivity.start(context, data.url!!)
//            NewsDetailActivity.start(this, data)
        }
        list2.setOnItemClickListener { parent, view, position, id ->
            val data = adapter2.dataList[position]
            //download(data.url!!, data.doc_title!!)
            if (!TextUtils.isEmpty(data.url)) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(data.url)
                startActivity(intent)
            }
//            NewsDetailActivity.start(this, data)
        }
        list3.setOnItemClickListener { parent, view, position, id ->
            val data = adapter3.dataList[position]
            //download(data.url!!, data.doc_title!!)
            if (!TextUtils.isEmpty(data.url)) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(data.url)
                startActivity(intent)
            }
//            NewsDetailActivity.start(this, data)
        }

        val header = ProgressLayout(this)
        header.setColorSchemeColors(ContextCompat.getColor(this, R.color.color_main))
        refresh.setHeaderView(header)
        val footer = BallPulseView(this)
        footer.setAnimatingColor(ContextCompat.getColor(this, R.color.color_main))
        refresh.setBottomView(footer)
        refresh.setOverScrollRefreshShow(false)
        refresh.setOnRefreshListener(object : RefreshListenerAdapter() {
            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
                page = 1
                handler.post(searchTask)
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                ++page
                handler.post(searchTask)
            }

        })
        refresh.setAutoLoadMore(true)
//        handler.postDelayed({
//            doRequest()
//        }, 100)

        loadDir()
    }

    fun download(url: String, fileName: String) {
        //showToast("开始下载")
        showCustomToast(R.drawable.icon_toast_common, "开始下载")
        DownloadUtil.getInstance().download(url, externalCacheDir.toString(), fileName, object : DownloadUtil.OnDownloadListener {
            override fun onDownloadSuccess(path: String?) {
                var intent = OpenFileUtil.openFile(context, path)
                if (intent == null) {
                    showCustomToast(R.drawable.icon_toast_fail, "文件类型不合格")
                    //showToast("文件类型不合格")
                } else {
                    startActivity(intent)
                }
            }

            override fun onDownloading(progress: Int) {
            }

            override fun onDownloadFailed() {
                //showToast("下载失败")
                showCustomToast(R.drawable.icon_toast_fail, "下载失败")
            }
        })
    }

    fun stateDefault() {
        page = 1
        fl_filter.visibility = View.GONE
        search_view.tv_cancel.visibility = View.GONE
        iv_filter.visibility = View.VISIBLE
        ll_result.visibility = View.GONE
        et_search.isFocusable = false
        et_search.clearFocus()
    }

    fun stateSearch() {
        page = 1
        checkedFilter.clear()
        fl_filter.visibility = View.GONE
        search_view.tv_cancel.visibility = View.VISIBLE
        iv_filter.visibility = View.GONE
        ll_result.visibility = View.VISIBLE

        et_search.inputType = InputType.TYPE_CLASS_TEXT
        et_search.isFocusable = true
        et_search.isFocusableInTouchMode = true
        et_search.requestFocus()
//        Utils.showInput(this, et_search)

    }

    fun stateFilter() {
        page = 1
        checkedFilter.clear()
        setTags(filterList)
        search_view.search = ""
        fl_filter.visibility = View.VISIBLE
        search_view.tv_cancel.visibility = View.GONE
        iv_filter.visibility = View.VISIBLE
        ll_result.visibility = View.GONE
        et_search.isFocusable = false
        et_search.clearFocus()

    }


    val searchTask = Runnable {
        doSearch(search_view.search)
    }
    var key = ""
    val filterList = HashMap<Int, String>()
    var page = 1
    fun doSearch(text: String = "") {
        this.key = text
        val user = Store.store.getUser(this)
        val filter = if (checkedFilter.isEmpty()) null else checkedFilter.joinToString(",")
//        project.company_id = 325
        //TODO
        SoguApi.getService(application,InfoService::class.java)
                .projectBookSearch(company_id = project.company_id!!,
                        fuzzyQuery = text,
                        type = 1
                        , fileClass = filter, page = page)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1)
                            adapter.dataList.clear()
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                }, {
                    tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_search1, adapter.dataList.size))
                    adapter.notifyDataSetChanged()
                    if (page == 1)
                        refresh?.finishRefreshing()
                    else
                        refresh?.finishLoadmore()
                })

    }

    fun doRequest() {
//        project.company_id = 325
        SoguApi.getService(application,InfoService::class.java)
                .projectBook(type = 1, company_id = project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        adapter1.dataList.clear()
                        adapter2.dataList.clear()
                        adapter3.dataList.clear()
                        payload.payload?.apply {
                            if (null != list1)
                                (0..2)
                                        .filter { list1!!.size > it }
                                        .forEach { adapter1.dataList.add(list1!!.get(it)) }

                            if (null != list2)
                                (0..2)
                                        .filter { list2!!.size > it }
                                        .forEach { adapter2.dataList.add(list2!!.get(it)) }
                            if (null != list3)
                                (0..2)
                                        .filter { list3!!.size > it }
                                        .forEach { adapter3.dataList.add(list3!!.get(it)) }

                            if (list1!!.size <= 3)
                                tv_more1.visibility = View.GONE
                            else
                                tv_more1.visibility = View.VISIBLE

                            if (list2!!.size <= 3)
                                tv_more2.visibility = View.GONE
                            else
                                tv_more2.visibility = View.VISIBLE

                            if (list3!!.size <= 3)
                                tv_more3.visibility = View.GONE
                            else
                                tv_more3.visibility = View.VISIBLE
                        }
                        adapter1.notifyDataSetChanged()
                        adapter2.notifyDataSetChanged()
                        adapter3.notifyDataSetChanged()
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        tv_more1.visibility = View.GONE
                        tv_more2.visibility = View.GONE
                        tv_more3.visibility = View.GONE
                    }
                }, { e ->
                    Trace.e(e)
                    tv_more1.visibility = View.GONE
                    tv_more2.visibility = View.GONE
                    tv_more3.visibility = View.GONE
                })
        SoguApi.getService(application,InfoService::class.java)
                .projectFilter()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk && payload.payload != null) {
                        filterList.clear()
                        filterList.putAll(payload.payload!!)
                        setTags(filterList)

                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                })
    }

    val checkedFilter = ArrayList<String>()
    fun setTags(filterList: HashMap<Int, String>) {
        checkedFilter.clear()
        tags.removeAllViews()
        val textColor1 = resources.getColor(R.color.white)
        val textColor0 = resources.getColor(R.color.text_1)
        val bgColor0 = R.drawable.bg_tag_filter_0
        val bgColor1 = R.drawable.bg_tag_filter_1
        val onClick: (View) -> Unit = { v ->
            val tvTag = v as TextView
            val ftag = tvTag.tag as String
            if (checkedFilter.contains(ftag)) {
                checkedFilter.remove(ftag)
                tvTag.setTextColor(textColor0)
                tvTag.setBackgroundResource(bgColor0)
            } else {
                checkedFilter.add(ftag)
                tvTag.setTextColor(textColor1)
                tvTag.setBackgroundResource(bgColor1)
            }
        }
        kotlin.run {
            val itemTag = View.inflate(this, R.layout.item_tag_filter, null)
            val tvTag = itemTag.findViewById<TextView>(R.id.tv_tag) as TextView
            tvTag.text = "全部"
            tags.addView(itemTag)
            tvTag.setOnClickListener {
                fl_filter.visibility = View.GONE
                ll_result.visibility = View.VISIBLE
                checkedFilter.clear()
                doSearch()
            }
        }
        filterList.entries.forEach { e ->
            val itemTag = View.inflate(this, R.layout.item_tag_filter, null)
            val tvTag = itemTag.findViewById<TextView>(R.id.tv_tag) as TextView
            tvTag.text = e.value
            tvTag.tag = "${e.key}"
            tvTag.setOnClickListener(onClick)
            tags.addView(itemTag)
        }


        fl_filter.setOnClickListener {
            stateDefault()
        }
        iv_filter.setOnClickListener {
            if (fl_filter.visibility == View.VISIBLE) {
                stateDefault()
            } else {
                stateFilter()
            }
        }
        ll_filter.setOnClickListener { }
        btn_reset.setOnClickListener {
            checkedFilter.clear()
            for (i in 0 until tags.childCount) {
                val itemTag = tags.getChildAt(i)
                val tvTag = itemTag.findViewById<TextView>(R.id.tv_tag) as TextView
                tvTag.setTextColor(textColor0)
                tvTag.setBackgroundResource(bgColor0)
            }
            stateDefault()
            this.key = ""
            search_view.search = ""
        }
        btn_ok.setOnClickListener {
            fl_filter.visibility = View.GONE
            ll_result.visibility = View.VISIBLE
            if (!checkedFilter.isEmpty())
                doSearch()
            else
                doSearch()
        }
    }

    class ProjectBookHolder
        : ListHolder<ProjectBookBean> {
        lateinit var tvSummary: TextView
        lateinit var tvDate: TextView
        lateinit var tvTime: TextView
        lateinit var tvType: TextView
        lateinit var tvName: TextView
        override fun createView(inflater: LayoutInflater): View {
            val convertView = inflater.inflate(R.layout.item_project_book, null)
            tvSummary = convertView.findViewById<TextView>(R.id.tv_summary) as TextView
            tvDate = convertView.findViewById<TextView>(R.id.tv_date) as TextView
            tvTime = convertView.findViewById<TextView>(R.id.tv_time) as TextView
            tvType = convertView.findViewById<TextView>(R.id.tv_type) as TextView
            tvName = convertView.findViewById<TextView>(R.id.tv_name) as TextView
            return convertView
        }

        override fun showData(convertView: View, position: Int, data: ProjectBookBean?) {
            tvSummary.text = data?.doc_title
            val strTime = data?.add_time
            tvTime.visibility = View.GONE
            if (!TextUtils.isEmpty(strTime)) {
                val strs = strTime!!.trim().split(" ")
                if (!TextUtils.isEmpty(strs.getOrNull(1))) {
                    tvTime.visibility = View.VISIBLE
                }
                tvDate.text = strs.getOrNull(0)
                tvTime.text = strs.getOrNull(1)
            }
            tvType.text = data?.name
            tvName.text = data?.submitter
        }

    }

    override fun onResume() {
        super.onResume()
        doRequest()
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, ProjectBookActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }

    fun loadDir() {
        SoguApi.getService(application,InfoService::class.java)
                .showCatalog(1, project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        var group = ArrayList<DirBean>()
                        var child = ArrayList<ArrayList<FileListBean>>()
                        payload?.payload?.forEach {
                            group.add(it)
                            child.add(ArrayList())
                        }
                        fileAdapter = MyExpListAdapter(context, group, child)
                        fileList.setAdapter(fileAdapter)
                    } else {
                        //showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    //showCustomToast(R.drawable.icon_toast_fail, "加载失败")
                })

        fileList.setOnGroupClickListener { parent, v, groupPosition, id ->
            var group = fileAdapter.group[groupPosition]
            if(group.click){
                fileList.collapseGroup(groupPosition)
                group.click = false
            } else {
                loadFileList(group.dir_id, groupPosition)
                group.click = true
            }
            true
        }
    }

    lateinit var fileAdapter:MyExpListAdapter

    fun loadFileList(dirId:Int?=null, position:Int) {
        SoguApi.getService(application,InfoService::class.java)
                .fileList(project.company_id!!, 1, dirId, page = 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        fileAdapter.childs[position].clear()
                        payload.payload?.forEach {
                            fileAdapter.childs[position].add(it)
                        }
                        fileList.collapseGroup(position)
                        fileList.expandGroup(position)
                    } else {
                        //showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    //showCustomToast(R.drawable.icon_toast_fail, "加载失败")
                })
    }

    inner class MyExpListAdapter(val context: Context, val group: ArrayList<DirBean>,
                                 val childs: ArrayList<ArrayList<FileListBean>>) : BaseExpandableListAdapter() {

        override fun getGroup(groupPosition: Int): Any {
            return group[groupPosition]
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
            return true
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            var holder: GroupHolder? = null
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.file_header, null)
                holder = GroupHolder()
                holder.title = view.findViewById<TextView>(R.id.head_title) as TextView
                holder.direction = view.findViewById<ImageView>(R.id.direction) as ImageView
                view.setTag(holder)
            } else {
                holder = view.tag as GroupHolder
            }

            holder.title?.text = group[groupPosition].dirname
            if (isExpanded) {
                holder.direction?.setBackgroundResource(R.drawable.up)
            } else {
                holder.direction?.setBackgroundResource(R.drawable.down)
            }

            return view!!
        }

        inner class GroupHolder {
            var title: TextView? = null
            var direction: ImageView? = null
        }

        override fun getChildrenCount(groupPosition: Int): Int {
            return 1
        }

        override fun getChild(groupPosition: Int, childPosition: Int): Any {
            return childs[groupPosition][childPosition]
        }

        override fun getGroupId(groupPosition: Int): Long {
            return groupPosition.toLong()
        }

        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            var holder: FatherHolder? = null
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.file_child, null)
                holder = FatherHolder()
                holder.list = view.findViewById<ListViewCompat>(R.id.list1) as ListViewCompat
                holder.tv_more = view.findViewById<TextView>(R.id.tv_more1) as TextView
                view.setTag(holder)
            } else {
                holder = view.tag as FatherHolder
            }

            var dataList = childs.get(groupPosition)
            if (dataList.size <= 3) {
                holder.tv_more?.visibility = View.GONE
            } else {
                holder.tv_more?.visibility = View.VISIBLE
            }

            var adapter = MyListAdapter(context, ArrayList<FileListBean>())
            holder.list?.adapter = adapter

            if (dataList.size <= 3) {
                adapter.datalist.addAll(dataList)
                adapter.notifyDataSetChanged()
            } else {
                for (i in 0..2) {
                    adapter.datalist.add(dataList.get(i))
                }
                adapter.notifyDataSetChanged()
            }

            holder.tv_more?.setOnClickListener {
                ProjectBookMoreActivity.start(this@ProjectBookActivity, project, group[groupPosition].dir_id!!)
            }

            return view!!
        }

        inner class FatherHolder {
            var list: ListViewCompat? = null
            var tv_more: TextView? = null
        }

        override fun getChildId(groupPosition: Int, childPosition: Int): Long {
            return childPosition.toLong()
        }

        override fun getGroupCount(): Int {
            return group.size
        }

        inner class MyListAdapter(val context: Context, val datalist: ArrayList<FileListBean>) : BaseAdapter() {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                var view = convertView
                var holder: ChildHolder? = null
                if (view == null) {
                    view = LayoutInflater.from(context).inflate(R.layout.item_project_book, null)
                    holder = ChildHolder()
                    holder.root = view.findViewById<LinearLayout>(R.id.root) as LinearLayout
                    holder.tvSummary = view.findViewById<TextView>(R.id.tv_summary) as TextView
                    holder.tvDate = view.findViewById<TextView>(R.id.tv_date) as TextView
                    holder.tvTime = view.findViewById<TextView>(R.id.tv_time) as TextView
                    holder.tvType = view.findViewById<TextView>(R.id.tv_type) as TextView
                    holder.tvName = view.findViewById<TextView>(R.id.tv_name) as TextView
                    view.setTag(holder)
                } else {
                    holder = view.tag as ChildHolder
                }

                var data = datalist.get(position)
                holder.tvSummary?.text = data.doc_title
                val strTime = DateUtils.timedate(data.add_time)
                holder.tvTime?.visibility = View.GONE
                if (!TextUtils.isEmpty(strTime)) {
                    val strs = strTime!!.trim().split(" ")
                    if (!TextUtils.isEmpty(strs.getOrNull(1))) {
                        holder.tvTime?.visibility = View.VISIBLE
                    }
                    holder.tvDate?.text = strs.getOrNull(0)
                    holder.tvTime?.text = strs.getOrNull(1)
                }
                holder.tvType?.visibility = View.INVISIBLE
                holder.tvType?.text = data.name
                holder.tvName?.text = data.submitter

                holder.root?.setOnClickListener {
                    if (!TextUtils.isEmpty(data.url)) {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(data.url)
                        startActivity(intent)
                    }
                }

                return view!!
            }

            override fun getItem(position: Int): Any {
                return datalist.get(position)
            }

            override fun getItemId(position: Int): Long {
                return position.toLong()
            }

            override fun getCount(): Int {
                return datalist.size
            }

            inner class ChildHolder {
                var root: LinearLayout? = null
                var tvSummary: TextView? = null
                var tvDate: TextView? = null
                var tvTime: TextView? = null
                var tvType: TextView? = null
                var tvName: TextView? = null
            }
        }
    }
}
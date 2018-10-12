package com.sogukj.pe.module.project

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.checkEmpty
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.CharacterParser
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SideBar
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.peUtils.SortUtil
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.NewService
import com.sogukj.pe.widgets.ProjectBeanLayout
import com.sogukj.pe.widgets.SnappingLinearLayoutManager
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_list_project.*
import org.jetbrains.anko.support.v4.ctx
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/7/18.
 */
class ProjectListFragment : BaseFragment(), SupportEmptyView {
    override val containerViewId: Int
        get() = R.layout.fragment_list_project
    lateinit var adapter: RecyclerAdapter<ProjectBean>
    var type: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getInt(Extras.TYPE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RecyclerAdapter<ProjectBean>(baseActivity!!, { _adapter, parent, t ->
            ProjectHolderNew(_adapter.getView(R.layout.item_main_project_new, parent))
        })
        adapter.onItemClick = { v, p ->
            val project = adapter.getItem(p);

            val t = if (type == ProjectListFragment.TYPE_GZ) {
                project.type
            } else {
                type
            }
            ProjectDetailActivity.start(ctx, project, t ?: 0, p)
            XmlDb.open(ctx).set(Extras.TYPE, type.toString())
        }
        val layoutManager = LinearLayoutManager(baseActivity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
        recycler_view.layoutManager = SnappingLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycler_view.adapter = adapter


        refresh.setOnRefreshListener {
            offset = 0
            doRequest()
            refresh.finishRefresh(1000)
        }

        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        iv_loading?.visibility = View.VISIBLE


        Log.e("onViewCreated", "${type}")
        isViewCreated = true

        initSideBar()

        if (arguments!!.getBoolean(Extras.FLAG, false)) {
            doRequest()
        }
    }

    fun initSideBar() {
        side_bar.setTextView(dialog)
//        side_bar.setB(arrayListOf(
//                "A", "B", "C", "D", "E", "F", "G",
//                "H", "I", "J", "K", "L", "M", "N",
//                "O", "P", "Q", "R", "S", "T",
//                "U", "V", "W", "X", "Y", "Z"))
        //设置右侧触摸监听
        side_bar.setOnTouchingLetterChangedListener(SideBar.OnTouchingLetterChangedListener { s ->
            if (!groups.contains(s)) {
                return@OnTouchingLetterChangedListener
            }
            var list = adapter.dataList
            for (index in list.indices) {
                if (list[index].sortLetters == s) {
                    recycler_view.smoothScrollToPosition(index)
                    break
                }
            }
        })
    }

    private var groups = ArrayList<String>()
    private var childs = ArrayList<ArrayList<ProjectBean>>()
    private var characterParser = CharacterParser.getInstance()

    /**
     * 为ListView填充数据
     */
    private fun filledData(list: List<ProjectBean>) {
        //java 用for循环为一个字符串数组输入从a到z的值。//groups
        groups.clear()
        var result = ""
        var i = 0
        var j = 'a'
        while (i < 26) {
            groups.add((j.toString() + "").toUpperCase())
            result += j.toString() + " "//连起来，空格隔开
            i++
            j++
        }

        //生成sortLetters
        for (index in list.indices) {
            var bean = list[index]
            //汉字转换成拼音
            var name = if (bean.shortName.isNullOrEmpty()) bean.name else bean.shortName
            name = name?.replace("（".toRegex(), "")?.replace("）".toRegex(), "")
            if (name.isNullOrEmpty()) {
                continue
            }
            var sortString: String
            try {
                sortString = characterParser.getAlpha(name).toUpperCase().substring(0, 1)
                // 正则表达式，判断首字母是否是英文字母
                if (sortString.matches("[A-Z]".toRegex())) {
                    bean.sortLetters = sortString.toUpperCase()
                } else {
                    bean.sortLetters = "#"
                }
            } catch (e: Exception) {
                bean.sortLetters = "#"
            }
        }

        //组装格式
        childs.clear()
        for (gindex in groups.indices) {
            var innerList = ArrayList<ProjectBean>()
            list.indices
                    .filter { list[it].sortLetters.equals(groups[gindex]) }
                    .mapTo(innerList) { list[it] }
            childs.add(innerList)
        }
        for (i in (childs.size - 1) downTo 0) {
            if (childs[i].size == 0) {
                childs.removeAt(i)
                groups.removeAt(i)
            }
        }

        side_bar.setB(groups)
    }

    var isViewCreated = false

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && isViewCreated) {
            Log.e("setUserVisibleHint", "${type}")
            doRequest()
        }
    }

    fun request() {
        doRequest()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isViewCreated = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x001) {
            doRequest()
            if (resultCode == Activity.RESULT_OK && parentFragment != null) {
                if (data!!.getStringExtra(Extras.FLAG) == "DELETE") {
                    return
                }
                var mPFragment = parentFragment as MainProjectFragment
                var viewpager = mPFragment.getViewPager()
                ++viewpager.currentItem
                (mPFragment.fragments.get(viewpager.currentItem) as ProjectListFragment).request()
            }
        }
    }

    val fmt = SimpleDateFormat("MM/dd HH:mm")
    var offset = 0

    fun doRequest() {
        val user = Store.store.getUser(baseActivity!!)
        SoguApi.getService(baseActivity!!.application, NewService::class.java)
                .listProject(offset = offset, type = type, uid = user!!.uid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (offset == 0)
                            adapter.dataList.clear()
                        var list = ArrayList<ProjectBean>()
                        payload.payload?.forEach {
                            list.add(it)
                        }
                        SortUtil.sortByProjectName(list)
                        filledData(list)
                        adapter.dataList.addAll(list)
                        refreshHead.setLastUpdateText("更新了${list.size}条数据")
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        refreshHead.setLastUpdateText("无数据更新")
                    }
                    iv_loading?.visibility = View.GONE
                }, { e ->
                    Trace.e(e)
                    refreshHead.setLastUpdateText("无数据更新")
                    iv_loading?.visibility = View.GONE
                    iv_empty.visibility = if (adapter.dataList.isEmpty()) View.VISIBLE else View.GONE
                }, {
                    iv_empty.visibility = if (adapter.dataList.isEmpty()) View.VISIBLE else View.GONE
                    adapter.notifyDataSetChanged()
                })
    }

    companion object {
        val TAG = ProjectListFragment::class.java.simpleName
        const val TYPE_CB = 4
        const val TYPE_LX = 1
        const val TYPE_YT = 2
        const val TYPE_GZ = 3
        const val TYPE_DY = 6
        const val TYPE_TC = 7

        fun newInstance(type: Int, isFocus: Boolean): ProjectListFragment {
            val fragment = ProjectListFragment()
            val intent = Bundle()
            intent.putInt(Extras.TYPE, type)
            intent.putBoolean(Extras.FLAG, isFocus)
            fragment.arguments = intent
            return fragment
        }
    }

    //StoreProjectHolder
    inner class ProjectHolderNew(convertView: View)
        : RecyclerHolder<ProjectBean>(convertView) {

        val ivIcon: ImageView
        val ivSC: FrameLayout
        val tvTitle: TextView
        val tvDSZ: TextView
        val tvDate: TextView
        val tvTime: TextView
        val tvStage: TextView
        val tvBusiness: TextView
        val tvAbility: TextView
        val divider_t: TextView
        val tvState: TextView

        val bean1: ProjectBeanLayout
        val bean2: ProjectBeanLayout
        val point: ImageView

        val divider_1: TextView
        val divider_2: TextView

        init {
            ivIcon = convertView.findViewById<ImageView>(R.id.company_icon) as ImageView
            ivSC = convertView.findViewById<FrameLayout>(R.id.shoucang) as FrameLayout
            tvTitle = convertView.findViewById<TextView>(R.id.tv_title) as TextView
            tvDSZ = convertView.findViewById<TextView>(R.id.tv_dsz) as TextView
            tvDate = convertView.findViewById<TextView>(R.id.tv_date) as TextView
            tvTime = convertView.findViewById<TextView>(R.id.tv_time) as TextView
            tvStage = convertView.findViewById<TextView>(R.id.stage) as TextView
            tvBusiness = convertView.findViewById<TextView>(R.id.business) as TextView
            tvAbility = convertView.findViewById<TextView>(R.id.ability) as TextView
            divider_t = convertView.findViewById<TextView>(R.id.divider_t) as TextView
            tvState = convertView.findViewById<TextView>(R.id.state) as TextView
            bean1 = convertView.findViewById<ProjectBeanLayout>(R.id.layout1) as ProjectBeanLayout
            bean2 = convertView.findViewById<ProjectBeanLayout>(R.id.layout2) as ProjectBeanLayout
            point = convertView.findViewById<ImageView>(R.id.point) as ImageView
            divider_1 = convertView.findViewById<TextView>(R.id.divider_1) as TextView
            divider_2 = convertView.findViewById<TextView>(R.id.divider_2) as TextView
        }

        override fun setData(view: View, data: ProjectBean, position: Int) {
            if (data.logo.isNullOrEmpty()) {
                var bitmap = BitmapFactory.decodeResource(resources, R.drawable.default_icon)
                var draw = RoundedBitmapDrawableFactory.create(resources, bitmap)
                draw.cornerRadius = Utils.dpToPx(context, 4).toFloat()
                ivIcon.setBackgroundDrawable(draw)
            } else {
                Glide.with(ctx).asBitmap().load(data.logo).into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(bitmap: Bitmap, glideAnimation: Transition<in Bitmap>?) {
                        var draw = RoundedBitmapDrawableFactory.create(resources, bitmap)
                        draw.cornerRadius = Utils.dpToPx(context, 4).toFloat()
                        ivIcon.setBackgroundDrawable(draw)
                    }
                })
            }

            var sc_icon = ivSC.getChildAt(0) as ImageView
            if (data.is_focus == 1) {
                Glide.with(ctx).load(R.drawable.sc_yes).into(sc_icon)
            } else if (data.is_focus == 0) {
                Glide.with(ctx).load(R.drawable.sc_no).into(sc_icon)
            }
            ivSC.setOnClickListener {
                val user = Store.store.getUser(ctx) ?: return@setOnClickListener
                SoguApi.getService(baseActivity!!.application, NewService::class.java)
                        .mark(uid = user!!.uid!!, company_id = data.company_id!!, type = (1 - data.is_focus))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ payload ->
                            if (payload.isOk) {
                                data.is_focus = 1 - data.is_focus
                                if (data.is_focus == 1) {
                                    Glide.with(ctx).load(R.drawable.sc_yes).into(sc_icon)
                                } else if (data.is_focus == 0) {
                                    Glide.with(ctx).load(R.drawable.sc_no).into(sc_icon)
                                }
                            }
                        }, { e ->
                            Trace.e(e)
                        })
            }

            var label = data.shortName
            if (TextUtils.isEmpty(label))
                label = data.name
            tvTitle.text = label

            tvDSZ.text = "法人：${data.chairman.checkEmpty()}"
            if (data.chairman.isNullOrEmpty()) {
                tvDSZ.visibility = View.INVISIBLE
            }
            tvDate.text = "负责人：${data.chargeName.checkEmpty()}"
            if (data.chargeName.isNullOrEmpty()) {
                tvDate.visibility = View.INVISIBLE
            }

            val strTime = data.add_time
            if (!TextUtils.isEmpty(strTime)) {
                try {
                    val strs = strTime!!.trim().split(" ") // 2018/07/08 18:23
                    var time = strs.get(0).split("/")
                    tvTime.text = "${time[1]}-${time[2]}"
                } catch (e: Exception) {
                    tvTime.visibility = View.INVISIBLE
                }
            } else {
                tvTime.visibility = View.INVISIBLE
            }

            tvStage.text = data.state

            if (data.is_business == 1) {
                tvBusiness.visibility = View.VISIBLE
                divider_1.visibility = View.VISIBLE
                tvBusiness.text = "有商业价值"
            } else if (data.is_business == 2) {
                tvBusiness.visibility = View.VISIBLE
                divider_1.visibility = View.VISIBLE
                tvBusiness.text = "无商业价值"
            } else if (data.is_business == null) {
                tvBusiness.visibility = View.GONE
                divider_1.visibility = View.GONE
            }

            if (data.is_ability == 1) {
                tvAbility.visibility = View.VISIBLE
                divider_2.visibility = View.VISIBLE
                tvAbility.text = "创始人靠谱"
            } else if (data.is_ability == 2) {
                tvAbility.visibility = View.VISIBLE
                divider_2.visibility = View.VISIBLE
                tvAbility.text = "创始人不靠谱"
            } else if (data.is_ability == null) {
                tvAbility.visibility = View.GONE
                divider_2.visibility = View.GONE
            }

            // 立项页面时显示（已完成或准备中）两个状态，   退出显示（部分退出或全部退出）两个状态   //1准备中  2已完成
            if (type == TYPE_YT) {
                if (data.quit == 0) {
                    tvState.text = "在投"
                } else if (data.quit == 1) {
                    tvState.text = "部分退出"
                }
            } else {
                divider_t.visibility = View.INVISIBLE
                tvState.visibility = View.INVISIBLE
            }

            if (data.update_time.isNullOrEmpty()) {
                bean1.setData(false, 1, "暂无更新资讯")
            } else {
                bean1.setData(true, 1, data.update_time)
            }
            if (data.track_time.isNullOrEmpty()) {
                bean2.setData(false, 2, "未进行跟踪")
            } else {
                bean2.setData(true, 2, data.track_time)
            }

            if (data.red != null && data.red != 0) {
                point.visibility = View.VISIBLE
            } else {
                point.visibility = View.INVISIBLE
            }
        }

    }
}
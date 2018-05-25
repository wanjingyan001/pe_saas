package com.sogukj.pe.module.project

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.checkEmpty
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.NewService
import com.sogukj.pe.widgets.ProjectBeanLayout
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
        get() = R.layout.fragment_list_project//To change initializer of created properties use File | Settings | File Templates.
    lateinit var adapter: RecyclerAdapter<ProjectBean>
    var type: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments!!.getInt(Extras.TYPE)
    }

    fun getRecycleView(): RecyclerView {
        return recycler_view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ll_header.visibility = View.GONE
        ll_header1.visibility = View.GONE
        ll_header2.visibility = View.GONE
        ll_header3.visibility = View.GONE

        adapter = RecyclerAdapter(baseActivity!!, { _adapter, parent, t ->
            ProjectHolderNew(_adapter.getView(R.layout.item_main_project_new, parent))
        })
        adapter.onItemClick = { v, p ->
            val project = adapter.getItem(p)
            val intent = Intent(context, ProjectActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            if (type == ProjectListFragment.TYPE_GZ) {
                intent.putExtra(Extras.TYPE, project.type)
            } else {
                intent.putExtra(Extras.TYPE, type)
            }
            intent.putExtra(Extras.CODE, p)
            startActivityForResult(intent, 0x001)

            XmlDb.open(ctx).set(Extras.TYPE, type.toString())
        }
        if (type != TYPE_GZ)// && user?.is_admin == 1
            adapter.onItemLongClick = { v, p ->
                if (type == TYPE_DY || type == TYPE_CB) {
                } else {
                    editOptions(v, p)
                }
                true
            }
        val layoutManager = LinearLayoutManager(baseActivity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter

        refresh.setOnRefreshListener {
            offset = 0
            doRequest()
            refresh.finishRefresh(1000)
        }
        refresh.setOnLoadMoreListener {
            offset = adapter.dataList.size
            doRequest()
            refresh.finishLoadMore(1000)
        }

        ll_order_name_1.setOnClickListener { v ->
            asc *= if (orderBy == 1) -1 else 1
            orderBy = 1
            doRequest()
        }
        ll_order_time_1.setOnClickListener { v ->
            asc *= if (orderBy == 4) -1 else 1
            orderBy = 4
            doRequest()
        }

        ll_order_name.setOnClickListener { v ->
            asc *= if (orderBy == 1) -1 else 1
            orderBy = 1
            doRequest()
        }
        ll_order_time.setOnClickListener { v ->
            asc *= if (orderBy == 3) -1 else 1
            orderBy = 3
            doRequest()
        }
        ll_order_state.setOnClickListener { v ->
            asc *= if (orderBy == 2) -1 else 1
            orderBy = 2
            doRequest()
        }
        Glide.with(ctx)
                .load(Uri.parse("file:///android_asset/img_loading.gif"))
                .into(iv_loading)
        iv_loading?.visibility = View.VISIBLE
        handler.postDelayed({
            doRequest()
        }, 100)
        Log.e("onViewCreated", "${type}")
        isViewCreated = true
    }

    override fun onStart() {
        super.onStart()
        Log.e("onStart", "${type}")
        //doRequest()
    }

    var isViewCreated = false

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && isViewCreated) {
            Log.e("setUserVisibleHint", "$type")
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
            if (resultCode == Activity.RESULT_OK) {
                var mPFragment = parentFragment as MainProjectFragment
                var viewpager = mPFragment.getViewPager()
                ++viewpager.currentItem
                mPFragment.fragments.get(viewpager.currentItem).request()
            }
        }
    }

    val fmt = SimpleDateFormat("MM/dd HH:mm")
    var offset = 0
    var asc = -1
    var orderBy = 4

    fun doRequest() {
        iv_sort_name_1.visibility = View.GONE
        iv_sort_time_1.visibility = View.GONE
        iv_sort_name.visibility = View.GONE
        iv_sort_time.visibility = View.GONE
        iv_sort_state.visibility = View.GONE
        val imgAsc = if (asc == -1) R.drawable.ic_down else R.drawable.ic_up
        if (type == TYPE_CB || type == TYPE_DY) {
            val view = when (Math.abs(orderBy)) {
                1 -> iv_sort_name_1
                4 -> iv_sort_time_1
                else -> {
                    orderBy = 4
                    iv_sort_time_1
                }
            }
            view.setImageResource(imgAsc)
            view.visibility = View.VISIBLE
        } else {
            val view = when (Math.abs(orderBy)) {
                1 -> iv_sort_name
                2 -> iv_sort_state
                else -> {
                    orderBy = 3
                    iv_sort_time
                }
            }
            view.setImageResource(imgAsc)
            view.visibility = View.VISIBLE
        }


        val sort = orderBy * asc
        val user = Store.store.getUser(baseActivity!!)
        SoguApi.getService(baseActivity!!.application,NewService::class.java)
                .listProject(offset = offset, type = type, uid = user!!.uid, sort = sort)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (offset == 0)
                            adapter.dataList.clear()
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    iv_loading?.visibility = View.GONE
                }, { e ->
                    Trace.e(e)
                    iv_loading?.visibility = View.GONE
                    iv_empty.visibility = if (adapter.dataList.isEmpty()) View.VISIBLE else View.GONE
                }, {
                    iv_empty.visibility = if (adapter.dataList.isEmpty()) View.VISIBLE else View.GONE
                    adapter.notifyDataSetChanged()
                    if (offset == 0)
                        refresh?.finishRefresh()
                    else
                        refresh?.finishLoadMore()
                })
    }

    fun editOptions(view: View, position: Int) {
        val popupView = View.inflate(baseActivity, R.layout.pop_edit_project, null)
        val tvAdd = popupView.findViewById<TextView>(R.id.tv_add) as TextView
        val tvDel = popupView.findViewById<TextView>(R.id.tv_del) as TextView
        val pop = PopupWindow(popupView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true)
        tvAdd.setOnClickListener { view1 ->
            doAdd(position)
            pop.dismiss()
        }
        tvDel.setOnClickListener { view12 ->
            //            doDel(position)
            pop.dismiss()
            MaterialDialog.Builder(baseActivity!!)
                    .theme(Theme.LIGHT)
                    .title("提示")
                    .content("确定要删除这条数据?")
                    .onPositive { materialDialog, dialogAction ->
                        doDel(position)
                    }
                    .positiveText("确定")
                    .negativeText("取消")
                    .show()
        }
        if (type == TYPE_TC) tvAdd.visibility = View.GONE
        var toast_txt = if (type == TYPE_LX) "添加已投" else if (type == TYPE_YT) "添加到退出" else ""
        tvAdd.text = toast_txt
        pop.isTouchable = true
        pop.isFocusable = true
        pop.isOutsideTouchable = true
        pop.setBackgroundDrawable(BitmapDrawable(resources, null as Bitmap?))
    }

    fun doDel(position: Int) {
        val project = adapter.dataList[position]
        SoguApi.getService(baseActivity!!.application,NewService::class.java)
                .delProject(project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showCustomToast(R.drawable.icon_toast_success, "删除成功")
                        adapter.dataList.removeAt(position)
                        adapter.notifyDataSetChanged()
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_fail, "删除失败")
                })
    }

    fun doAdd(position: Int) {
        var status = if (type == TYPE_LX) 3 else if (type == TYPE_YT) 4 else 0
        val project = adapter.dataList[position]
        SoguApi.getService(baseActivity!!.application,NewService::class.java)
                //.editProject(project.company_id!!)
                .changeStatus(project.company_id!!, status)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (type == TYPE_LX) {
                            showCustomToast(R.drawable.icon_toast_success, "添加拟投成功")
                        } else if (type == TYPE_YT) {
                            showCustomToast(R.drawable.icon_toast_success, "已添加到退出")
                        }
                        adapter.dataList.removeAt(position)
                        adapter.notifyDataSetChanged()
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                    if (type == TYPE_LX) {
                        showCustomToast(R.drawable.icon_toast_fail, "添加拟投失败")
                    } else if (type == TYPE_YT) {
                        showCustomToast(R.drawable.icon_toast_fail, "添加到退出失败")
                    }
                })

    }

//    inner class StoreProjectHolder(convertView: View)
//        : RecyclerHolder<ProjectBean>(convertView) {
//
//
//        val tvTitle: TextView
//        val btnAdd: TextView
//        val tvDate: TextView
//        val tvTime: TextView
//        val tvEdit: TextView
//        val tvDel: TextView
//        val tv4: TextView
//        val tv5: TextView
//        val tv_pingjia: LinearLayout
//        val point: TextView
//
//        init {
//            tvTitle = convertView.findViewById(R.id.tv_title) as TextView
//            btnAdd = convertView.findViewById(R.id.btn_add) as TextView
//            tvDate = convertView.findViewById(R.id.tv_date) as TextView
//            tvTime = convertView.findViewById(R.id.tv_time) as TextView
//            tvEdit = convertView.findViewById(R.id.tv_edit) as TextView
//            tvDel = convertView.findViewById(R.id.tv_del) as TextView
//            tv4 = convertView.findViewById(R.id.business) as TextView
//            tv5 = convertView.findViewById(R.id.ability) as TextView
//            tv_pingjia = convertView.findViewById(R.id.pingjia) as LinearLayout
//            point = convertView.findViewById(R.id.point) as TextView
//        }
//
//        override fun setData(view: View, data: ProjectBean, position: Int) {
//            var label = data.shortName
//            if (TextUtils.isEmpty(label))
//                label = data.name
//            tvTitle.text = Html.fromHtml(label)
//            val strTime = data.add_time
//            tvTime.visibility = View.GONE
//            if (!TextUtils.isEmpty(strTime)) {
//                val strs = strTime!!.trim().split(" ")
//                if (!TextUtils.isEmpty(strs.getOrNull(1))) {
//                    tvTime.visibility = View.VISIBLE
//                }
//                tvDate.text = strs
//                        .getOrNull(0)
//                tvTime.text = strs
//                        .getOrNull(1)
//            }
//
//            if (type == TYPE_DY) {
//                btnAdd.text = "+储备"
//                btnAdd.setOnClickListener {
//                    MaterialDialog.Builder(baseActivity!!)
//                            .theme(Theme.LIGHT)
//                            .content("是否确认储备")
//                            .negativeText("取消")
//                            .positiveText("确认")
//                            .onPositive { dialog, which ->
//                                doSetUp(position, data, 1)
//                                dialog.dismiss()
//                            }.show()
//                }
//            } else if (type == TYPE_CB) {
//                btnAdd.text = "+立项"
//                btnAdd.setOnClickListener {
//                    MaterialDialog.Builder(baseActivity!!)
//                            .theme(Theme.LIGHT)
//                            .content("是否确认立项")
//                            .negativeText("取消")
//                            .positiveText("确认")
//                            .onPositive { dialog, which ->
//                                doSetUp(position, data, 2)
//                                dialog.dismiss()
//                            }.show()
//                }
//            }
//
//            tvEdit.setOnClickListener {
//                if (type == TYPE_CB) {
//                    StoreProjectAddActivity.startEdit(baseActivity, data)
//                } else if (type == TYPE_DY) {
//                    ProjectAddActivity.startEdit(baseActivity, data)
//                }
//
//            }
//            tvDel.setOnClickListener {
//                MaterialDialog.Builder(baseActivity!!)
//                        .theme(Theme.LIGHT)
//                        .content("确认删除该数据")
//                        .negativeText("取消")
//                        .positiveText("确认")
//                        .onPositive { dialog, which ->
//                            doDel(position)
//                            dialog.dismiss()
//                        }.show()
//            }
//            if (type == TYPE_CB) {
//                tv_pingjia.visibility = View.VISIBLE
//            } else if (type == TYPE_DY) {
//                tv_pingjia.visibility = View.GONE
//            }
//            //convertView.findViewById(R.id.suffix).visibility = View.VISIBLE
//            if (data.is_business == 1) {
//                tv4.text = "有商业价值"
//            } else if (data.is_business == 2) {
//                tv4.text = "无商业价值"
//            }
//
//            if (data.is_ability == 1) {
//                tv5.text = "创始人靠谱"
//            } else if (data.is_ability == 2) {
//                tv5.text = "创始人不靠谱"
//            }
//
//            if (data.is_business == null && data.is_ability == null) {
//                tv_pingjia.visibility = View.GONE
//            } else {
//                tv_pingjia.visibility = View.VISIBLE
//                if (data.is_business == null) {
//                    tv4.visibility = View.GONE
//                } else {
//                    tv4.visibility = View.VISIBLE
//                }
//                if (data.is_ability == null) {
//                    tv5.visibility = View.GONE
//                } else {
//                    tv5.visibility = View.VISIBLE
//                }
//            }
//
//            if (data.red != null && data.red != 0) {
//                point.visibility = View.VISIBLE
//                point.text = data.red.toString()
//            } else {
//                point.visibility = View.GONE
//            }
//        }
//
//    }
//
//    fun doSetUp(position: Int, data: ProjectBean, status: Int) {
//        SoguApi.getService(baseActivity!!.application)
//                //.setUpProject(data.company_id!!)
//                .changeStatus(data.company_id!!, status!!)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe({ payload ->
//                    if (payload.isOk) {
//                        showCustomToast(R.drawable.icon_toast_success, "修改成功")
//                        adapter.dataList.removeAt(position)
//                        adapter.notifyDataSetChanged()
//                    } else {
//                        showCustomToast(R.drawable.icon_toast_fail, "修改失败")
//                    }
//                }, { e -> Trace.e(e) })
//    }
//
//    inner class ProjectHolder(view: View)
//        : RecyclerHolder<ProjectBean>(view) {
//
//        val tv1: TextView
//        val tv2: TextView
//        val tv3: TextView
//        val tv4: TextView
//        val tv5: TextView
//        val pingjia: LinearLayout
//        val point: TextView
//
//        init {
//            tv1 = view.find(R.id.tv1)
//            tv2 = view.find(R.id.tv2)
//            tv3 = view.find(R.id.tv3)
//            tv4 = view.find(R.id.business)
//            tv5 = view.find(R.id.ability)
//            pingjia = view.find(R.id.pingjia)
//            point = view.find(R.id.point)
//        }
//
//        override fun setData(view: View, data: ProjectBean, position: Int) {
//            var label = data.shortName
//            if (TextUtils.isEmpty(label))
//                label = data.name
//            tv1.text = Html.fromHtml(label)
//            if (type == 1) {
//                tv2.text = when (data.status) {
//                    2 -> "已完成"
//                    else -> "准备中"
//                }
//            } else if (type == 2 || type == 5) {
//                tv2.text = data.state
//                tv2.textColor = when (data.state) {
//                    "初创" -> Color.parseColor("#9e9e9e")
//                    "天使轮" -> Color.parseColor("#e64a19")
//                    "A轮" -> Color.parseColor("#f57c00")
//                    "B轮" -> Color.parseColor("#ffa000")
//                    "C轮" -> Color.parseColor("#fbc02d")
//                    "D轮" -> Color.parseColor("#afb42b")
//                    "E轮" -> Color.parseColor("#689f38")
//                    "PIPE轮" -> Color.parseColor("#388e3c")
//                    "新三板" -> Color.parseColor("#00796b")
//                    "IPO" -> Color.parseColor("#0097a7")
//                    else -> Color.parseColor("#9e9e9e")
//                }
//                val bg = when (data.state) {
//                    "初创" -> R.drawable.bg_border_proj1
//                    "天使轮" -> R.drawable.bg_border_proj2
//                    "A轮" -> R.drawable.bg_border_proj3
//                    "B轮" -> R.drawable.bg_border_proj4
//                    "C轮" -> R.drawable.bg_border_proj5
//                    "D轮" -> R.drawable.bg_border_proj6
//                    "E轮" -> R.drawable.bg_border_proj7
//                    "PIPE轮" -> R.drawable.bg_border_proj8
//                    "新三板" -> R.drawable.bg_border_proj9
//                    "IPO" -> R.drawable.bg_border_proj10
//                    else -> R.drawable.bg_border_proj1
//                }
//                tv2.setBackgroundResource(bg)
//            }
//            tv3.text = when (type) {
//                1 -> data.add_time
//                2 -> data.update_time
//                else -> data.next_time
//            }
//
//            if (tv2.text.isNullOrEmpty()) {
//                tv2.text = "--"
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    tv2.background = null
//                }
//            }
//            if (tv3.text.isNullOrEmpty()) tv3.text = "--"
//
//            if (data.is_business == 1) {
//                tv4.text = "有商业价值"
//            } else if (data.is_business == 2) {
//                tv4.text = "无商业价值"
//            }
//
//            if (data.is_ability == 1) {
//                tv5.text = "创始人靠谱"
//            } else if (data.is_ability == 2) {
//                tv5.text = "创始人不靠谱"
//            }
//
//            if (data.is_business == null && data.is_ability == null) {
//                pingjia.visibility = View.GONE
//            } else {
//                pingjia.visibility = View.VISIBLE
//                if (data.is_business == null) {
//                    tv4.visibility = View.GONE
//                } else {
//                    tv4.visibility = View.VISIBLE
//                }
//                if (data.is_ability == null) {
//                    tv5.visibility = View.GONE
//                } else {
//                    tv5.visibility = View.VISIBLE
//                }
//            }
//            if (data.red != null && data.red != 0) {
//                point.visibility = View.VISIBLE
//                point.text = data.red.toString()
//            } else {
//                point.visibility = View.GONE
//            }
//        }
//
//    }

    companion object {
        val TAG = ProjectListFragment::class.java.simpleName
        const val TYPE_CB = 4
        const val TYPE_LX = 1
        const val TYPE_YT = 2
        const val TYPE_GZ = 3
        const val TYPE_DY = 6
        const val TYPE_TC = 7

        fun newInstance(type: Int): ProjectListFragment {
            val fragment = ProjectListFragment()
            val intent = Bundle()
            intent.putInt(Extras.TYPE, type)
            fragment.arguments = intent
            return fragment
        }
    }


    //StoreProjectHolder
    inner class ProjectHolderNew(convertView: View)
        : RecyclerHolder<ProjectBean>(convertView) {

        val ivIcon: ImageView = convertView.findViewById<ImageView>(R.id.company_icon) as ImageView
        val ivSC: ImageView = convertView.findViewById<ImageView>(R.id.shoucang) as ImageView
        val tvTitle: TextView = convertView.findViewById<TextView>(R.id.tv_title) as TextView
        val tvDSZ: TextView = convertView.findViewById<TextView>(R.id.tv_dsz) as TextView
        val tvDate: TextView = convertView.findViewById<TextView>(R.id.tv_date) as TextView
        val tvTime: TextView = convertView.findViewById<TextView>(R.id.tv_time) as TextView
        val tvStage: TextView = convertView.findViewById<TextView>(R.id.stage) as TextView
        val tvBusiness: TextView = convertView.findViewById<TextView>(R.id.business) as TextView
        val tvAbility: TextView = convertView.findViewById<TextView>(R.id.ability) as TextView
        val divider_t: TextView = convertView.findViewById<TextView>(R.id.divider_t) as TextView
        val tvState: TextView = convertView.findViewById<TextView>(R.id.state) as TextView

        val bean1: ProjectBeanLayout = convertView.findViewById<ProjectBeanLayout>(R.id.layout1) as ProjectBeanLayout
        val bean2: ProjectBeanLayout = convertView.findViewById<ProjectBeanLayout>(R.id.layout2) as ProjectBeanLayout
        val point: ImageView = convertView.findViewById<ImageView>(R.id.point) as ImageView

        val divider_1: TextView = convertView.findViewById<TextView>(R.id.divider_1) as TextView
        val divider_2: TextView = convertView.findViewById<TextView>(R.id.divider_2) as TextView

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

            if (data.is_focus == 1) {
                Glide.with(ctx).load(R.drawable.sc_yes).into(ivSC)
            } else if (data.is_focus == 0) {
                Glide.with(ctx).load(R.drawable.sc_no).into(ivSC)
            }
            ivSC.setOnClickListener {
                val user = Store.store.getUser(ctx) ?: return@setOnClickListener
                SoguApi.getService(baseActivity!!.application,NewService::class.java)
                        .mark(uid = user.uid!!, company_id = data.company_id!!, type = (1 - data.is_focus))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ payload ->
                            if (payload.isOk) {
                                data.is_focus = 1 - data.is_focus
                                if (data.is_focus == 1) {
                                    Glide.with(ctx).load(R.drawable.sc_yes).into(ivSC)
                                } else if (data.is_focus == 0) {
                                    Glide.with(ctx).load(R.drawable.sc_no).into(ivSC)
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

            tvDSZ.text = "董事长：${data.chairman.checkEmpty()}"

            val strTime = data.add_time
            if (!TextUtils.isEmpty(strTime)) {
                val strs = strTime!!.trim().split(" ")
                tvDate.text = strs
                        .getOrNull(0)
                tvTime.text = strs
                        .getOrNull(1)
            } else {
                tvDate.visibility = View.INVISIBLE
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
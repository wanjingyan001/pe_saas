package com.sogukj.pe.module.creditCollection

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.RxBus
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.CreditInfo
import com.sogukj.pe.bean.MessageEvent
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.service.CreditService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_shareholder_credit.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.find

class ShareholderCreditActivity : BaseActivity(), View.OnClickListener {

    private lateinit var bean: ProjectBean
    private lateinit var mAdapter: RecyclerAdapter<CreditInfo.Item>

    companion object {
        val TAG = ShareholderCreditActivity::class.java.simpleName
        fun start(ctx: Context?, project: ProjectBean) {
            val intent = Intent(ctx, ShareholderCreditActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        var dispose = RxBus.getIntanceBus().doSubscribe(MessageEvent::class.java, { bean ->
            mAdapter.dataList.add(0, bean.message)
            mAdapter.notifyDataSetChanged()
            iv_empty.visibility = if (mAdapter.dataList.isEmpty()) View.VISIBLE else View.GONE
            Log.e("success", bean.message.toString())
        }, { t ->
            Log.e("success", t.message)
        })
        RxBus.getIntanceBus().addSubscription(this, dispose)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        RxBus.getIntanceBus().unSubscribe(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shareholder_credit)
        bean = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        initAdapter()
        initSearchView()
        back.setOnClickListener(this)
        inquireBtn.setOnClickListener(this)

        Glide.with(context).asGif().load(R.drawable.dynamic).into(gif)

        AppBarLayout.addOnOffsetChangedListener { _, verticalOffset ->
            if (gif.height > 0) {
                var appBarHeight = AppBarLayout.height
                var toolbarHeight = toolbar.height
                var gifH = gif.height

                var finalGifH = Utils.dpToPx(context, 30)

                //初始距离（git.top）,终点距离0
                //计算移动距离Y
                val distanceHeadImg = gif.top * 1.0f// - (toolbarHeight - finalGifH) / 2
                var mHeadImgScale = distanceHeadImg / (appBarHeight - toolbarHeight) * (-Math.abs(verticalOffset))
                gif.translationY = mHeadImgScale

                //计算移动距离X
                //gif原来显示的长宽（不是ImageView的长宽）是84*56，现在45*30，需要移动(84-45)/2=20(其实是19.5)
                var xDis = Math.abs(verticalOffset) * 1.0f / (appBarHeight - toolbarHeight)
                gif.translationX = xDis * Utils.dpToPx(context, 20)

                //图片默认一开始高度为56dp(gifH),最后变成30dp
                //放大缩小
                var scale = 1.0f - (gifH - finalGifH) * Math.abs(verticalOffset) * 1.0f / (appBarHeight - toolbarHeight) / gifH
                gif.scaleX = scale
                gif.scaleY = scale

                if (appBarHeight - toolbarHeight - Math.abs(verticalOffset).toFloat() < 5) {
                    collapse.title = ""
                    toolbar_title.visibility = View.VISIBLE
                } else {
                    collapse.title = "让不良记录无处遁形"
                    toolbar_title.visibility = View.GONE
                }
            }
        }

        offset = 0
        doRequest(bean.company_id)

        mAdapter.onItemClick = { v, p ->
            var cell = mAdapter.dataList.get(p)
            if (cell.status == 2) {
                SensitiveInfoActivity.start(context, cell)
            } else if (cell.status == 3) {
                AddCreditActivity.start(context, "EDIT", cell, 0x002)
            }
        }


    }

    var searchKey: String? = null

    private fun initSearchView() {
        search_edt.filters = Utils.getFilter(context)
        search_edt.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                search_hint.visibility = View.GONE
                search_icon.visibility = View.VISIBLE
            } else {
                search_hint.visibility = View.VISIBLE
                search_icon.visibility = View.GONE
                search_edt.clearFocus()
            }
        }
        search_edt.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchKey = search_edt.text.toString()
                offset = 0
                doRequest(bean.company_id)
                true
            } else {
                false
            }
        }
        search_edt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (search_edt.text.toString().isEmpty()) {
                    searchKey = ""
                    offset = 0
                    doRequest(bean.company_id)
                }
                if (!search_edt.text.isNullOrEmpty()) {
                    delete1.visibility = View.VISIBLE
                    delete1.setOnClickListener {
                        search_edt.setText("")
                    }
                } else {
                    delete1.visibility = View.GONE
                }
            }
        })
    }

    private fun initAdapter() {
        mAdapter = RecyclerAdapter(this, { _adapter, parent, _ ->
            val convertView = _adapter.getView(R.layout.item_shareholder_credit, parent)
            ShareHolder(convertView)
        })
        lister.layoutManager = LinearLayoutManager(this)
        lister.adapter = mAdapter

        refresh.setOnRefreshListener {
            offset = 0
            doRequest(bean.company_id)
            refresh.finishRefresh(1000)
        }
        refresh.setOnLoadMoreListener {
            offset = mAdapter.dataList.size
            doRequest(bean.company_id)
            refresh.finishLoadMore(1000)
        }
    }

    var offset = 0

    fun doRequest(companyId: Int?) {
        SoguApi.getService(application, CreditService::class.java)
                .showCreditList(company_id = companyId, offset = offset, fuzzyQuery = searchKey)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (offset == 0)
                            mAdapter.dataList.clear()
                        payload.payload?.forEach {
                            mAdapter.dataList.add(it)
                        }
                        if (offset != 0) {
                            if (payload.payload == null || payload.payload!!.size == 0) {
                                showCustomToast(R.drawable.icon_toast_common, "已加载全部")
                            }
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                    iv_empty.visibility = if (mAdapter.dataList.isEmpty()) View.VISIBLE else View.GONE
                }, {
                    mAdapter.notifyDataSetChanged()
                    iv_empty.visibility = if (mAdapter.dataList.isEmpty()) View.VISIBLE else View.GONE
                    if (offset == 0)
                        refresh?.finishRefresh()
                    else
                        refresh?.finishLoadMore()
                })
    }

    inner class ShareHolder(convertView: View) : RecyclerHolder<CreditInfo.Item>(convertView) {

        private val directorName = convertView.find<TextView>(R.id.directorName)
        private val directorPosition = convertView.find<TextView>(R.id.directorPosition)
        private val inquireStatus = convertView.find<ImageView>(R.id.inquireStatus)//失败
        private val phoneNumberTv = convertView.find<TextView>(R.id.phoneNumberTv)
        private val IDCardTv = convertView.find<TextView>(R.id.IDCardTv)
        private val companyTv = convertView.find<TextView>(R.id.companyTv)
        private val quireTimeTv = convertView.find<TextView>(R.id.quireTime)
        private val edit = convertView.find<ImageView>(R.id.edit)
        private val number = convertView.find<TextView>(R.id.number)//失败
        private val fail = convertView.find<FrameLayout>(R.id.fail)//失败
        private val success = convertView.find<ImageView>(R.id.success)//成功

        override fun setData(view: View, data: CreditInfo.Item, position: Int) {
            directorName.text = data.name
            directorPosition.text = when (data.type) {
                1 -> "董监高"
                2 -> "股东"
                else -> ""
            }
            if (data.type == 0) {
                directorPosition.visibility = View.GONE
            } else {
                directorPosition.visibility = View.VISIBLE
            }
            phoneNumberTv.text = data.phone
            IDCardTv.text = data.idCard
            if (data.company.isNullOrEmpty()) {
                companyTv.visibility = View.GONE
            } else {
                companyTv.visibility = View.VISIBLE
                companyTv.text = data.company
            }
            if (data.queryTime.isNullOrEmpty()) {
                quireTimeTv.visibility = View.GONE
            } else {
                quireTimeTv.visibility = View.VISIBLE
                quireTimeTv.text = "最新查询时间：${data.queryTime}"
            }
            when (data.status) {
                1 -> {
                    fail.visibility = View.GONE
                    success.visibility = View.VISIBLE
                    success.setImageResource(R.drawable.zhengxin_chaxunzhong)
                }
                2 -> {
                    success.visibility = View.GONE
                    fail.visibility = View.VISIBLE
                    if (data.sum == null || data.sum == 0) {
                        number.visibility = View.GONE
                        inquireStatus.setImageResource(R.drawable.zhengxin_zhengchang)
                    } else {
                        number.visibility = View.VISIBLE
                        number.text = "${data.sum}"
                        inquireStatus.setImageResource(R.drawable.zhengxin_fail)
                    }
                }
                3 -> {
                    fail.visibility = View.GONE
                    success.visibility = View.VISIBLE
                    success.setImageResource(R.drawable.zhengxin_chaxunshibai)
                }
            }
            edit.setOnClickListener {
                AddCreditActivity.start(context, "EDIT", data, 0x002)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back -> finish()
            R.id.inquireBtn -> {
                var str = XmlDb.open(context).get("INNER", "TRUE")
                if (str == "TRUE") {
                    var item = CreditInfo.Item()
                    item.company = bean.name
                    item.company_id = bean.company_id!!
                    AddCreditActivity.start(context, "ADD", item, 0x001)
                } else if (str == "FALSE") {
                    ShareHolderStepActivity.start(context, 1, 0, "")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x001) {
            data?.apply {
//                var bean = this.getSerializableExtra(Extras.DATA) as CreditInfo.Item
//                mAdapter.dataList.add(0, bean)
//                mAdapter.notifyDataSetChanged()
//                iv_empty.visibility = if (mAdapter.dataList.isEmpty()) View.VISIBLE else View.GONE
                offset = 0
                doRequest(bean.company_id)
            }
        } else if (requestCode == 0x002) {
            data?.apply {
                var bean = this.getSerializableExtra(Extras.DATA) as CreditInfo.Item
                var type = ""
                try {
                    type = this.getSerializableExtra(Extras.TYPE) as String
                } catch (e: Exception) {
                }
                var list = ArrayList<CreditInfo.Item>(mAdapter.dataList)
                for (index in list.indices) {
                    if (list[index].id == bean.id) {
                        if (type.equals("DELETE")) {
                            list.removeAt(index)
                        } else {
                            list[index] = bean
                        }
                        break
                    }
                }
                mAdapter.dataList.clear()
                mAdapter.dataList.addAll(list)
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onMessageReceive(item: CreditInfo.Item) {
        mAdapter.dataList.add(0, item)
        mAdapter.notifyDataSetChanged()
        iv_empty.visibility = if (mAdapter.dataList.isEmpty()) View.VISIBLE else View.GONE
        EventBus.getDefault().removeAllStickyEvents()
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND, sticky = true)
    fun onMessageReceive111(item: CreditInfo.Item) {
        var a = 1
    }

    @Subscribe(threadMode = ThreadMode.POSTING, sticky = true)
    fun onMessageReceive111111(item: CreditInfo.Item) {
        var a = 1
    }

    @Subscribe(threadMode = ThreadMode.ASYNC, sticky = true)
    fun onMessageReceive111222(item: CreditInfo.Item) {
        var a = 1
    }

    //不知道为什么EventBus失效
    fun insert(item: CreditInfo.Item) {
        mAdapter.dataList.add(0, item)
        mAdapter.notifyDataSetChanged()
        iv_empty.visibility = if (mAdapter.dataList.isEmpty()) View.VISIBLE else View.GONE
    }
}

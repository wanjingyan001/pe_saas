package com.sogukj.pe.module.creditCollection

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.inputmethod.EditorInfo
import com.bumptech.glide.Glide
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.PersonCreList
import com.sogukj.pe.service.CreditService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_new_credit_list.*
import kotlinx.android.synthetic.main.item_new_credit_list.view.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.dip
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import org.jetbrains.anko.startActivity

class NewCreditListActivity : BaseActivity() {
    private lateinit var listAdapter: RecyclerAdapter<PersonCreList>
    private val model: CreditViewModel by lazy {
        ViewModelProviders.of(this).get(CreditViewModel::class.java)
    }
    private var offset = 0
    var searchKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_credit_list)
        Glide.with(context).asGif().load(R.drawable.dynamic).into(gif)
        AppBarLayout.addOnOffsetChangedListener { _, verticalOffset ->
            if (gif.height > 0) {
                val appBarHeight = AppBarLayout.height
                val toolbarHeight = toolbar.height
                val gifH = gif.height

                val finalGifH = Utils.dpToPx(context, 30)

                //初始距离（git.top）,终点距离0
                //计算移动距离Y
                val distanceHeadImg = gif.top * 1.0f// - (toolbarHeight - finalGifH) / 2
                val mHeadImgScale = distanceHeadImg / (appBarHeight - toolbarHeight) * (-Math.abs(verticalOffset))
                gif.translationY = mHeadImgScale

                //计算移动距离X
                //gif原来显示的长宽（不是ImageView的长宽）是84*56，现在45*30，需要移动(84-45)/2=20(其实是19.5)
                val xDis = Math.abs(verticalOffset) * 1.0f / (appBarHeight - toolbarHeight)
                gif.translationX = xDis * Utils.dpToPx(context, 20)

                //图片默认一开始高度为56dp(gifH),最后变成30dp
                //放大缩小
                val scale = 1.0f - (gifH - finalGifH) * Math.abs(verticalOffset) * 1.0f / (appBarHeight - toolbarHeight) / gifH
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
        initView()
        initAdapter()
    }

    override fun onResume() {
        super.onResume()
        getNewCreditList()
    }

    private fun initView() {
        refresh.isEnableLoadMore = true
        refresh.setOnRefreshListener {
            offset = 0
            getNewCreditList()
            refresh.postDelayed({ refresh.finishRefresh() }, 2000)

        }
        refresh.setOnLoadMoreListener {
            offset = listAdapter.dataList.size
            loadMore(offset, searchKey)
            refresh.postDelayed({ refresh.finishLoadMore(0) }, 2000)
        }
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
        search_edt.textChangedListener {
            afterTextChanged {
                delete1.setVisible(search_edt.textStr.isNotEmpty())
                search_edt.textStr.isEmpty().yes {
                    search_edt.clearFocus()
                    offset = 0
                    getNewCreditList()
                }
            }
        }
        search_edt.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchKey = search_edt.text.toString()
                offset = 0
                loadMore(offset, searchKey)
                true
            } else {
                false
            }
        }
        delete1.clickWithTrigger {
            search_edt.setText("")
        }
        inquireBtn.clickWithTrigger {
            startActivity<HundredSearchActivity>()
        }
        back.clickWithTrigger {
            finish()
        }
    }

    private fun initAdapter() {
        listAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            CreditHolder(_adapter.getView(R.layout.item_new_credit_list, parent))
        }
        listAdapter.onItemClick = { v, position ->
            getNewCreditDetail(listAdapter.dataList[position].idCard)
        }
        lister.apply {
            layoutManager = LinearLayoutManager(ctx)
            addItemDecoration(SpaceItemDecoration(dip(10)))
            adapter = listAdapter
        }
    }

    private fun getNewCreditList() {
        model.getCreditList().observe(this, Observer { list ->
            list?.let {
                listAdapter.refreshData(it)
            }
            iv_empty.setVisible(listAdapter.dataList.isEmpty())
        })
    }

    private fun loadMore(offset: Int, searchKey: String? = null) {
        SoguApi.getService(application, CreditService::class.java)
                .getPersonalCreList(offset, fuzzyQuery = searchKey)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                if (offset == 0){
                                    listAdapter.dataList.clear()
                                }
                                listAdapter.dataList.addAll(it)
                                listAdapter.notifyDataSetChanged()
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                    onComplete {
                        iv_empty.setVisible(listAdapter.dataList.isEmpty())
                    }
                }
    }


    private fun getNewCreditDetail(idcard: String) {
        SoguApi.getService(application, CreditService::class.java)
                .hundredCreditDetail(idcard)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                startActivity<NewCreditDetailActivity>(Extras.BEAN to it)
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }


    inner class CreditHolder(itemView: View) : RecyclerHolder<PersonCreList>(itemView) {
        override fun setData(view: View, data: PersonCreList, position: Int) {
            view.directorName.text = data.name
            view.phoneNumberTv.text = data.phone
            view.IDCardTv.text = data.idCard
            view.quireTime.text = data.add_time
            view.edit.clickWithTrigger {
                startActivity<HundredSearchActivity>(
                        Extras.NAME to data.name,
                        Extras.DATA to data.phone,
                        Extras.DATA2 to data.idCard)
            }
        }
    }
}

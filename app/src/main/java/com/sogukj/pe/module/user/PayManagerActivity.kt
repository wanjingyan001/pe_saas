package com.sogukj.pe.module.user

import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.bean.UserManagerBean
import com.sogukj.pe.module.receipt.AllPayCallBack
import com.sogukj.pe.module.receipt.PayDialog
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_pay_manager.*
import org.jetbrains.anko.find

/**
 * Created by CH-ZH on 2018/10/30.
 * 账户管理
 */
class PayManagerActivity : BaseRefreshActivity(), AllPayCallBack {
    private lateinit var adapter : RecyclerAdapter<UserManagerBean.ManagerInfo>
    private lateinit var alreadySelected: MutableSet<UserManagerBean.ManagerInfo>
    private var selectCount = 0
    private var price = "99"
    private var user : UserBean? = null
    private var userAccount = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_manager)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        setBack(true)
        setTitle("账户管理")
        rv_manager.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rv_manager.layoutManager = LinearLayoutManager(this)
        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        alreadySelected = ArrayList<UserManagerBean.ManagerInfo>().toMutableSet()
        user = Store.store.getUser(this)
        user?.let {
            userAccount = user!!.phone
        }
    }

    private fun initData() {
        adapter = RecyclerAdapter(this){_adapter,parent,_ ->
            PayManagerHolder(_adapter.getView(R.layout.pay_manager_item,parent))
        }
        rv_manager.adapter = adapter
        setLoadding()
        getManagerData()
    }

    private fun getManagerData() {
        SoguApi.getService(application,OtherService::class.java)
                .getManagerData()
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val bean = payload.payload
                            if (null != bean){
                                setManagerData(bean)
                            }
                        }else{
                            showErrorToast(payload.message)
                        }
                        dofinishRefresh()
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                        dofinishRefresh()
                    }
                }
    }

    private fun setManagerData(bean: UserManagerBean) {
        val message = bean.message
        if (null != message){
            price = message.price
            tv_2.text = message.price
            tv_yqcount.text = message.max_yuqing_count+"个"
            tv_zxcount.text = "${message.max_credit_count}个"
            if (null != message.list && message.list.size > 0){
                goneEmpty()
                adapter.dataList.clear()
                adapter.dataList.addAll(message.list)
                adapter.notifyDataSetChanged()
            }else{
                showEmpty()
            }
        }
    }

    inner class PayManagerHolder(itemView: View) : RecyclerHolder<UserManagerBean.ManagerInfo>(itemView) {
        val iv_select = itemView.find<ImageView>(R.id.iv_select)
        val tv_buy = itemView.find<TextView>(R.id.tv_buy)
        val tv_delete = itemView.find<TextView>(R.id.tv_delete)
        val ll_content = itemView.find<LinearLayout>(R.id.ll_content)
        val iv_image = itemView.find<ImageView>(R.id.iv_image)
        val tv_name = itemView.find<TextView>(R.id.tv_name)
        val tv_phone = itemView.find<TextView>(R.id.tv_phone)
        val tv_data = itemView.find<TextView>(R.id.tv_data)
        override fun setData(view: View, data: UserManagerBean.ManagerInfo, position: Int) {
            if (null == data) return
            Glide.with(this@PayManagerActivity)
                    .load(data.url)
                    .apply(RequestOptions.circleCropTransform()
                            .placeholder(R.drawable.default_head)
                            .error(R.drawable.default_head))
                    .into(iv_image)
            iv_select.isSelected = data.isSelect
            tv_name.text = data.name
            tv_phone.text = data.phone
            if (data.expiry_time.isNullOrEmpty()){
                tv_data.text = "到期时间：- -  - -"
            }else{
                tv_data.text = "到期时间：${data.expiry_time}"
            }
            itemView.clickWithTrigger {
                if (alreadySelected.contains(data)){
                    alreadySelected.remove(data)
                    selectCount --
                    if (selectCount <= 0){
                        selectCount = 0
                    }
                }else{
                    alreadySelected.add(data)
                    selectCount++
                }
                data.isSelect = !data.isSelect
                iv_select.isSelected = data.isSelect
                tv_pay_count.text = "已选择账号：${selectCount}个"
            }

            tv_buy.clickWithTrigger {
                //购买
                PayDialog.showPayBookDialog(this@PayManagerActivity,2,this@PayManagerActivity,userAccount,price,1,data.user_id)
            }
        }

    }

    override fun pay(order_type: Int, count: Int, pay_type: Int, fee: String, tv_per_balance: TextView,
                     iv_pre_select: ImageView, tv_bus_balance: TextView, iv_bus_select: ImageView,
                     tv_per_title:TextView,tv_bus_title:TextView) {

    }

    override fun payForOther(id: String, order_type: Int, count: Int, pay_type: Int, fee: String,
                             tv_per_balance: TextView, iv_pre_select: ImageView, tv_bus_balance: TextView, iv_bus_select: ImageView,
                             tv_per_title:TextView,tv_bus_title:TextView) {
        SoguApi.getService(application, OtherService::class.java)
                .getAccountPayInfo(order_type,count,pay_type,fee,id)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            if (pay_type == 1 || pay_type == 2){
                                showSuccessToast("支付成功")
                                PayDialog.refreshAccountData(tv_per_balance,iv_pre_select, tv_bus_balance,iv_bus_select,tv_per_title,tv_bus_title)
                                doRefresh()
                            }else{
                                if (pay_type == 3){
                                    //支付宝
                                }else if (pay_type == 4){
                                    //微信
                                }
                            }
                        }else{
                            showErrorToast(payload.message)
                        }
                    }

                    onError {
                        it.printStackTrace()
                        if (pay_type == 1 || pay_type == 2){
                            showErrorToast("支付失败")
                        }else{
                            showErrorToast("获取订单失败")
                        }
                    }
                }
    }

    private fun setLoadding(){
        view_recover.visibility = View.VISIBLE
    }

    private fun goneLoadding(){
        view_recover.visibility = View.INVISIBLE
    }

    private fun showEmpty(){
        fl_empty.visibility = View.VISIBLE
    }
    private fun goneEmpty(){
        fl_empty.visibility = View.INVISIBLE
    }

    private fun bindListener() {
        tv_pay.clickWithTrigger {
            //批量购买
            var ids = ""
            if (selectCount > 0){
                alreadySelected.forEach {
                    ids = it.user_id + ","
                }
                PayDialog.showPayBookDialog(this,2,this,userAccount,price,selectCount,ids)
            }else{
                showCommonToast("请至少选择一个账号")
            }
        }

        tv_delete.clickWithTrigger {
            //批量删除
        }
    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = false
        config.autoLoadMoreEnable = false
        config.scrollContentWhenLoaded = true
        config.disableContentWhenRefresh = true
        return config
    }

    fun dofinishRefresh() {
        if (this::refresh.isLateinit && refresh.isRefreshing) {
            refresh.finishRefresh()
        }
        goneLoadding()
    }

    override fun doRefresh() {
        getManagerData()
    }


    override fun doLoadMore() {

    }
}
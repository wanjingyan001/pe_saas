package com.sogukj.pe.module.im.clouddish

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.fromJson
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.FileDynamicBean
import com.sogukj.pe.bean.MechanismBasicInfo
import com.sogukj.pe.module.im.ImSearchResultActivity
import com.sogukj.pe.peUtils.Store
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_secret_cloud.*
import org.jetbrains.anko.startActivity
/**
 * Created by CH-ZH on 2018/10/25.
 * 首页加密云盘
 */
@Route(path = ARouterPath.SecretCloudActivity)
class SecretCloudActivity : ToolbarActivity() {
    private var company = ""
    private var data = ArrayList<FileDynamicBean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secret_cloud)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        val companyInfo = sp.getString(Extras.SAAS_BASIC_DATA, "")
        val detail = Gson().fromJson<MechanismBasicInfo?>(companyInfo)
        bindListener()
        setBack(true)
        setTitle("加密云盘")
        if (null != detail){
            company = detail.mechanism_name?:""
            detail.let {
                Glide.with(this).load(it.logo).into(iv_logo)
            }
        }
        tv_company.text = company
    }

    override fun onResume() {
        super.onResume()
        getDynamicData()
        getMemoryData()
    }

    private fun getMemoryData() {
        SoguApi.getStaticHttp(application)
                .getDirMemoryData("/我的文件",Store.store.getUser(this)!!.phone)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val jsonObject = payload.payload
                            if (null != jsonObject){
                                val total = jsonObject.get("total").asString
                                val used = jsonObject.get("used").asString
                                tv_file.text = "${used}/${total}"
                            }
                        }else{
                            showErrorToast(payload.message)
                        }
                    }
                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                    }
                }

    }

    private fun getDynamicData() {
        SoguApi.getStaticHttp(application)
                .getFileDynamicData(1, Store.store.getUser(this)!!.phone)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val info = payload.payload
                            data = info as ArrayList<FileDynamicBean>
                            if (null != info && info.size > 0){
                                val fileDynamicBean = info[0]
                                if (null != fileDynamicBean){
                                    tv_dynamic.text = "文件动态：${fileDynamicBean.display_name+fileDynamicBean.show}"
                                }else{
                                    tv_dynamic.text = "文件动态：暂无动态"
                                }
                            }else{
                                tv_dynamic.text = "文件动态：暂无动态"
                            }
                        }else{
                            showErrorToast(payload.message)
                        }
                    }

                    onError {
                        it.printStackTrace()
                    }
                }
    }
    private fun bindListener() {
        rl_search.clickWithTrigger {
            //文件搜索
            ImSearchResultActivity.invoke(this,3)
        }

        tv_dynamic.clickWithTrigger {
            //文件动态
            startActivity<FileDynamicActivity>(Extras.DATA to data)
        }

        view_mine_file.clickWithTrigger {
            //我的文件
            startActivity<MineFileActivity>(Extras.TITLE to "我的文件",Extras.DIR to "/我的文件",Extras.TYPE to false)
        }

        ll_company_file.clickWithTrigger {
            //公司文件
            startActivity<MineFileActivity>(Extras.TITLE to company,Extras.DIR to "/${company}",Extras.TYPE to true)
        }

        ll_fund_file.clickWithTrigger {
            //基金文件
            startActivity<MineFileActivity>(Extras.TITLE to "基金文件",Extras.DIR to "/${company}/基金文件")
        }

        ll_pro_file.clickWithTrigger {
            //项目文件
            startActivity<MineFileActivity>(Extras.TITLE to "项目文件",Extras.DIR to "/${company}/项目文件")
        }

        ll_chat_file.clickWithTrigger {
            //群组文件
            startActivity<MineFileActivity>(Extras.TITLE to "群组文件",Extras.DIR to "/${company}/群组文件")
        }
    }
}
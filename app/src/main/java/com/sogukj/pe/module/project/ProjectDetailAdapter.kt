package com.sogukj.pe.module.project

import android.graphics.Color
import android.graphics.PorterDuff
import android.support.constraint.ConstraintLayout
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sogukj.pe.R
import com.sogukj.pe.R.id.view
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.bean.ProjectModules
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.textColor

/**
 * Created by admin on 2018/8/23.
 */
class ProjectDetailAdapter(data: List<ProjectModules>)
    : BaseSectionQuickAdapter<ProjectModules, BaseViewHolder>(R.layout.item_pro_detail_module, R.layout.item_pro_detail_header, data) {
    override fun convertHead(helper: BaseViewHolder, item: ProjectModules) {

        val tagImg = helper.getView<ImageView>(R.id.titleTag)
        when {
            item.header.contains("公开") -> {
                tagImg.setVisible(true)
                tagImg.imageResource = R.mipmap.icon_pro_title_tag1
                helper.getView<TextView>(R.id.titleTv).text = item.header.substring(0, item.header.indexOf("（"))
            }
            item.header.contains("内部") -> {
                tagImg.setVisible(true)
                tagImg.imageResource = R.mipmap.icon_pro_title_tag2
                helper.getView<TextView>(R.id.titleTv).text = item.header.substring(0, item.header.indexOf("（"))
            }
            else -> {
                tagImg.setVisible(false)
                helper.getView<TextView>(R.id.titleTv).text = item.header
            }
        }
    }

    override fun convert(helper: BaseViewHolder, item: ProjectModules) {
        helper.setTag(R.id.moduleItem, item.t.id)
        val layout = helper.getView<ConstraintLayout>(R.id.moduleItem)
        helper.getView<TextView>(R.id.moduleName).text = item.t.name
        val icon = helper.getView<ImageView>(R.id.moduleIcon)
        icon.imageResource = IdToDrawable(item.t.id!!)
        val num = helper.getView<TextView>(R.id.countNum)
        val count = item.t.count
        if (item.t.module == 1) {
            num.visibility = View.GONE
            if (item.t.status == 1) {
                icon.clearColorFilter()
                layout.isClickable = true
            } else {
                icon.setColorFilter(Color.parseColor("#D9D9D9"), PorterDuff.Mode.SRC_ATOP)
                layout.isClickable = false
            }
        } else {
            if (count != null && count.toInt() > 0) {
                num.text = count
                icon.setColorFilter(Color.parseColor("#608cf8"), PorterDuff.Mode.SRC_ATOP)
                layout.isClickable = true
            } else {
                num.visibility = View.GONE
                icon.setColorFilter(Color.parseColor("#D9D9D9"), PorterDuff.Mode.SRC_ATOP)
                layout.isClickable = false
            }
        }
    }

    private fun IdToDrawable(id: Int): Int {
        return when (id) {
            1 -> R.drawable.ic_proj_gsxx//工商信息
            2 -> R.drawable.ic_proj_qygx//企业关系
            3 -> R.drawable.ic_proj_gudong//股东信息
            4 -> R.drawable.ic_proj_gqjg//股权结构
            5 -> R.drawable.ic_proj_zyry//主要人员
            6 -> R.drawable.ic_proj_dwtz//对外投资
            7 -> R.drawable.ic_proj_bgjl//变更记录
            8 -> R.drawable.ic_proj_qynb//企业年报
            9 -> R.drawable.ic_proj_fzjg//分支机构
            10 -> R.drawable.ic_proj_gsjj//公司简介
            11 -> R.drawable.ic_proj_rzls//融资历史
            12 -> R.drawable.ic_proj_tzsj//投资事件
            13 -> R.drawable.ic_proj_hxtd//核心团队
            14 -> R.drawable.ic_proj_qyyw//企业业务
            15 -> R.drawable.ic_proj_jpxx//竞品信息
            16 -> R.drawable.ic_proj_zpxx//招聘信息
            17 -> R.drawable.ic_proj_zqxx//债券信息
            18 -> R.drawable.ic_proj_swpj//税务评级
            19 -> R.drawable.ic_proj_gdxx//购地信息
            20 -> R.drawable.ic_proj_ztb//招投标
            21 -> R.drawable.ic_proj_zzzs//资质证书
            22 -> R.drawable.ic_proj_ccjc//抽查检查
            23 -> R.drawable.ic_proj_cpxx//产品信息
            24 -> R.drawable.ic_proj_xzxk//行政许可
            25 -> R.drawable.ic_proj_qsxx//清算信息
            26 -> R.drawable.ic_proj_gsyg//公司员工
            27 -> R.drawable.ic_proj_cwzl//财务总览
            28 -> R.drawable.ic_proj_lrb//利润表
            29 -> R.drawable.ic_proj_zcfzb//资产负债表
            30 -> R.drawable.ic_proj_xjllb//现金流量表
            31 -> R.drawable.ic_proj_jckxyxx//进出口信用信息
            32 -> R.drawable.ic_proj_sbxx//商标信息
            33 -> R.drawable.ic_proj_zlxx//专利信息
            34 -> R.drawable.ic_proj_zzq//著作权
            35 -> R.drawable.ic_proj_rzq//软著权
            36 -> R.drawable.ic_proj_wzba//网站备案
            37 -> R.drawable.ic_proj_qyzs//企业证书
            38 -> R.drawable.ic_proj_gphq//股票行情
            39 -> R.drawable.ic_proj_qyjs//企业简介
            40 -> R.drawable.ic_proj_ggxx//高管信息
            41 -> R.drawable.ic_proj_cgkg//参股控股
            42 -> R.drawable.ic_proj_ssgg//上市公告
            43 -> R.drawable.ic_proj_sdgd//十大股东
            44 -> R.drawable.ic_proj_sdlt//十大流通股东
            45 -> R.drawable.ic_proj_fxxg//发行相关
            46 -> R.drawable.ic_proj_gbjg//股本结构
            47 -> R.drawable.ic_proj_gbbd//股本变动
            48 -> R.drawable.ic_proj_fhqk//分红情况
            49 -> R.drawable.ic_proj_pgqk//配股情况
            50 -> R.drawable.ic_proj_cwsj//财务数据
            51 -> R.drawable.ic_proj_gdzx//股东征信
            52 -> R.drawable.ic_proj_xmws//项目文书
            53 -> R.drawable.ic_proj_xmgy//项目概要
            54 -> R.drawable.ic_proj_cbxx//储备信息
            55 -> R.drawable.ic_proj_gzjl//跟踪记录
            56 -> R.drawable.ic_proj_jdsj//尽调数据
            57 -> R.drawable.ic_proj_tjsj//投决数据
            58 -> R.drawable.ic_proj_thglsj//投后管理数据
            59 -> R.drawable.ic_proj_spls//审批历史
            60 -> R.drawable.ic_proj_gqxx//股权信息
            61 -> R.drawable.ic_proj_cwbb//财务报表
            62 -> R.drawable.ic_proj_pgqk//
            else -> 0
        }
    }
}
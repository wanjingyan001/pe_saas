package com.sogukj.pe.bean

import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.widgets.FlowLayout
import org.jetbrains.anko.find
import org.jetbrains.anko.withAlpha
import java.io.Serializable


/**
 * Created by qinfei on 17/7/19.
 */
class NewsBean : Serializable, NewsType {
    var title: String? = null
    var time: String? = null
    var source: String? = null
    var tag: String? = null
    var company_id: Int? = null
    var company: String? = null
    var table_id: Int? = null
    var data_id: Int? = null
    var url: String? = null
    var imgUrl: String? = null
    var shareUrl: String = "http://pe.stockalert.cn"
    var shareTitle: String = "新闻舆情"

    fun setTags(baseActivity: BaseActivity, tags: FlowLayout) {
        tags.removeAllViews()
        tag?.split("#")
                ?.forEach { str ->
                    if (!TextUtils.isEmpty(str)) {
                        val itemTag = View.inflate(baseActivity, R.layout.item_tag_news, null)

                        val tvTag = itemTag.find<TextView>(R.id.tv_tag)
                        tvTag.text = str
                        tags.addView(itemTag)

                        //val radius = Utils.dpToPx(baseActivity, 9)
                        //val drawable = GradientDrawable()
                        //drawable.cornerRadius = radius.toFloat()
                        var color = Color.parseColor("#4e7eef")
                        try {
                            color = Color.parseColor(map[str])
                        } catch (e: Exception) {

                        }
                        //drawable.setStroke(2, color)
                        //tvTag.setBackgroundDrawable(drawable)
                        tvTag.setTextColor(color)
                        var bgColor = color.withAlpha(0x1A)
                        tvTag.setBackgroundColor(bgColor)
                    }
                }
    }

    val map = mapOf(
            "天眼负面"
                    to "#d60000",
            "司法风险"
                    to "#8d5228",
            "经营风险"
                    to "#a75122",
            "法律诉讼"
                    to "#b3622a",
            "经营异常"
                    to "#c25417",
            "法律公告"
                    to "#e16e2b",
            "行政处罚"
                    to "#e86222",
            "失信人"
                    to "#ea874c",
            "严重违法"
                    to "#ea874c",
            "被执行人"
                    to "#eda97c",
            "股权出质"
                    to "#ec9f73",
            "动产抵押"
                    to "#efc0a2",
            "欠税公告"
                    to "#ecbf9b",
            "重大"
                    to "#6a2d2b",
            "法律风险"
                    to "#833f2c",
            "工程进度慢"
                    to "#a23b36",
            "交付时间延期"
                    to "#932d30",
            "经营风险"
                    to "#873539",
            "楼盘停工"
                    to "#8d2035",
            "审计风险"
                    to "#84283f",
            "下架停售"
                    to "#812333",
            "业主维权"
                    to "#742844",
            "战略风险"
                    to "#901a56",
            "召回门"
                    to "#7a024c",
            "质量问题"
                    to "#6e0250",
            "审计"
                    to "#4b2892",
            "管理状况"
                    to "#431697",
            "审计结果"
                    to "#1f1d5d",
            "公司改制"
                    to "#221e5b",
            "舆情负面"
                    to "#fa00af",
            "财务风险"
                    to "#890566",
            "解除质押担保"
                    to "#be3c36",
            "所有者权益减少"
                    to "#af2833",
            "债务风险"
                    to "#a61632",
            "质押担保"
                    to "#9d1c3d",
            "资金问题"
                    to "#a60648",
            "质押"
                    to "#d84826",
            "资本公积"
                    to "#d5443f",
            "债务到期"
                    to "#c92d3a",
            "无力还债"
                    to "#c8303b",
            "债台高筑"
                    to "#b80538",
            "担保"
                    to "#bf1848",
            "抵押"
                    to "#bb0e4f",
            "融资难"
                    to "#bb0c4f",
            "资金链断裂"
                    to "#b0193d",
            "资金周转困难"
                    to "#aa0243",
            "法律风险"
                    to "#304fff",
            "被举报"
                    to "#3d5afe",
            "被调查"
                    to "#536dff",
            "合同纠纷"
                    to "#8c9eff",
            "纳税审计"
                    to "#1a237e",
            "审计"
                    to "#313fa0",
            "事故处理"
                    to "#3949ab",
            "诉讼当事人"
                    to "#3f51b5",
            "诉讼行为"
                    to "#7986cc",
            "诉讼判决"
                    to "#283593",
            "纳税"
                    to "#9ea8db",
            "外部审计"
                    to "#c5cae8",
            "企业风险"
                    to "#c41262",
            "经营风险"
                    to "#02569c",
            "操作风险"
                    to "#117683",
            "恶性竞争"
                    to "#0ecae3",
            "非法集资"
                    to "#01bcd5",
            "管理状况"
                    to "#01bcd5",
            "人员风险"
                    to "#178593",
            "系统风险"
                    to "#0d94a5",
            "安全生产事故"
                    to "#01b7d4",
            "管理体制"
                    to "#00e6fe",
            "环境污染事件"
                    to "#18fffe",
            "人事管理"
                    to "#b2ebf2",
            "安全生产事故"
                    to "#80deea",
            "担保品管理"
                    to "#4dd0e2",
            "效率低下"
                    to "#26c7db",
            "增长降速"
                    to "#26c7db",
            "资产减值"
                    to "#00acc2",
            "其他风险"
                    to "#831f83",
            "战略风险"
                    to "#7f3292",
            "人事变动"
                    to "#0097a8",
            "社会评价"
                    to "#01828f",
            "停产停运"
                    to "#04a9f5",
            "停牌整顿"
                    to "#0091ea",
            "停止吊销业务"
                    to "#02569c",
            "退市"
                    to "#0377be",
            "违约风险"
                    to "#0288d1",
            "信息变更"
                    to "#04a9f5",
            "业务状况"
                    to "#6200eb",
            "资本运作"
                    to "#301b92",
            "犯罪案件"
                    to "#53325f",
            "战略转型"
                    to "#502668",
            "民众态度"
                    to "#016064",
            "信用危机"
                    to "#29b6f6",
            "股权变更"
                    to "#4fc2f8",
            "库存堆积"
                    to "#4527a1",
            "生产过剩"
                    to "#4527a1",
            "销售惨淡"
                    to "#6d2da8",
            "业绩不佳"
                    to "#7f35b1",
            "生产过剩"
                    to "#673bb8",
            "业绩不佳"
                    to "#9675ce",
            "股票情况"
                    to "#6620ff",
            "内部重组"
                    to "#7c4dff",
            "公司改制"
                    to "#d64159",
            "市场进入"
                    to "#d0485a",
            "市场退出"
                    to "#dd595f"
    )
}
//[{
//    "title":"你好",
//    "time":"2017-07-26 12:01:32",
//    "source":"任命日报",
//    "tag":"娱乐",
//    "company_id":2
//    "table_id":1,
//    "data_id":1,
//}]
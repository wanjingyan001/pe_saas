package com.sogukj.pe.module.approve.baseView.viewBean

import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes

/**
 * Created by admin on 2018/9/26.
 *
 *
 */
data class ControlBean(
        /**
         * 1 单行输入框 text
         * 2 多行输入框 textarea
         * 3 数字输入框 number
         * 4 单选框 select
         * 5 多选框 multiSelect
         * 6 日期 date
         * 7 图片 photo
         * 8 说明文字 textNote
         * 9 金额(元) money
         * 10 附件 attachment
         * 11 联系人 innerContact
         * 12 部门 department
         * 13 当前地点 timeAndLocatio
         * 14 关联审批单 relateApprovalForm
         * 15 用印选择 sealSelect
         * 16 横向页面单选 radio
         * 17 横向页面多选 checkbox
         * 18 项目 projectSelect
         * 19 基金 fundSelect
         * 20 外资 foreignSelect
         * 21 基金项目关联 fundRelatePro
         * 22 日期区间 dateRange
         * 23 事件文字 actionText
         * 24 是否发短信通知审批人 radioSms
         * 25 城市选择 citySelect
         * 0 组合 group
         * -1000 组合列表(出差的行程,报销的明细) grouplist
         * -1 请假套件 holiday
         * -2 出差套件 busTrip
         * -3 外出套件 goOut
         * -21 基金用印套件 fundSeal
         * -22 管理公司用印套件 manageSeal
         * -23 外资用印套件 foreignSeal
         * -41 报销套件 reimbursement
         */
        val control: Int,//控件id
        val componentName: String,//控件名称
        val fields: String,//传值时候用的key值(父级控件名称_当前控件名称_当前控件所在index)
        var name: String?,//控件名字
        val name1: String?,//开始时间(日期区间专用)
        val name2: String?,//结束时间(日期区间专用)
        val name3: String?,//时长(日期区间专用)
        val placeholder: String?,//提示文字
        val is_must: Boolean?,//是否必填
        /**
         * 以下两项为基金项目关联中使用
         */
        val is_must_fund: Boolean?,//基金是否必填(目前固定为true)
        val is_must_pro: Boolean?,//项目是否必填
        val is_disabled: Boolean?,//  是否可编辑
        val skip: List<SkipBean>?,//跳转相关
        val is_show: Boolean?,//是否打印
        val is_scal: Boolean?,//是否自动计算时长（日期区间）
        val is_multiple: Boolean?,//是否多选
        val is_extras: Boolean?,//是否开启实时转换大写
        val is_fresh: Boolean?,//是否刷新审批人
        val is_delete: Boolean?,//是否可删除（仅针对grouplist  true=>至少要留一个/false=>不能删除）
        val uint: String?,//单位
        /**
         * 时长计算单位   year=>年 month=>月  day=> 天 hour=>小时 min=>分钟 sec=>秒 work=>按工作时长计算
         *  返回的时间单位 year=>年 month=>月 day=> 天 hour=>时 min=>时 sec=>时 work=>时
         */
        val scal_unit: String?,
        val options: List<OptionBean>?,//选项列表
        /**
         * 日期格式
         * yyyy/yyyy-MM/yyyy-MM-dd/yyyy-MM-dd hh:mm/yyyy-MM-dd HH:mm:ss
         */
        val format: String?,
        val linkText: String?,//跳转文字
        val stable: String?,//跳转链接
        val extras: ExtrasBean?,
        var value: MutableList<Any?>?,//存放值(有可能是String,obj等 obj将转为ApproveValueBean)
        val children: MutableList<ControlBean>?//套件下的子控件

        /**
         * -1	请假套件	textNote（这是本月第n次请假）	holiday_textNote_leaveCount
        select（请假类型）	holiday_select_leaveType
        textNote（剩余假期）	holiday_textNote_leaveResidue
        dateRange（开始时间-结束时间-时长）	holiday_date_timeRange
        textNote（时长将自动计入考勤统计）	holiday_textNote_ordinary

        -2	    出差套件	text（出差事由）	busTrip_text_ordinary
        grouplist	busTrip_grouplist_ordinary
        group	busTrip_grouplist_group_ordinary
        textNote（行程1）	busTrip_grouplist_group_textNote_ordinary
        checkbox（交通工具）	busTrip_grouplist_group_checkbox_ordinary
        radio（单程/往返）	busTrip_grouplist_group_radio_ordinary
        citySelect（出发城市）	busTrip_grouplist_group_select_ordinary
        citySelect（目的城市）	busTrip_grouplist_group_multiSelect_ordinary
        dateRange（开始时间-结束时间-时长）	busTrip_grouplist_group_dateRange_timeRange
        actionText（+增加行程）	busTrip_number_ordinary
        number（自动计算）	busTrip_number_total
        textarea（出差备注）	busTrip_textarea_ordinary
        innerContact（同行人）	busTrip_innerContact_ordinary

        -3	    外出套件	select（外出类型）	goOut_selecT_ordinary
        dateRange（开始时间-结束时间-时长）	goOut_dateRange_timeRange
        textNote（时长将自动计入考勤统计）	goOut_textNote_ordinary

        -41	    报销套件	grouplist	reimbursement_grouplist_ordinary
        group	reimbursement_grouplist_group_ordinary
        textNote（报销明细1）	reimbursement_grouplist_group_textNote_ordinary
        money（金额）	reimbursement_grouplist_group_money_ordinary
        select（报销类别）	reimbursement_grouplist_group_select_ordinary
        textarea（费用明细）	reimbursement_grouplist_group_textarea_ordinary
        actionText（+增加明细）	reimbursement_actionText_ordinary
        number（总报销金额）	reimbursement_money_total

        -21	    基金用印套件	projectSelect（项目名称）	fundSeal_projectSelect_project
        fundSelect（基金名称）	fundSeal_fundSelect_fund
        attachment（用印文件）	fundSeal_attachment_file
        radio（是否需要律师意见）	fundSeal_radio_ordinary
        text（律师意见）	fundSeal_text_ordinary
        photo（律师意见文件）	fundSeal_photo_image
        radioSms（是否发短信提醒审批人）	fundSeal_radio_sms

        -22	    管理公司用印套件	projectSelect（项目名称）	manageSeal_projectSelect_project
        attachment（用印文件）	manageSeal_attachment_file
        radioSms（是否发短信提醒审批人）	manageSeal_radio_sms

        -23	    外资用印套件	foreignSelect（外资名称）	foreignSeal_foreignSelect_fund
        projectSelect（项目名称）	foreignSeal_projectSelect_project
        attachment（用印文件）	foreignSeal_attachment_file
        radioSms（是否发短信提醒审批人）	foreignSeal_radio_sms
         */
)

data class SkipBean(
        /**
         * 0.不请求;1.弹窗;2.二级页面;3.获取数据直接填充;4.跳转链接
         */
        val skip_type: Int,
        /**
         * 0 无操作
         * 1 项目 web/Skip/projectList
         * 2 基金 web/Skip/fundList
         * 3 外资 web/Skip/foreignList
         * 4 请假类型 web/Skip/holidaysList
         * 5 我的假期 web/Skip/myHolidays
         * 6 时长计算 web/Skip/calcTotalTime
         * 7 城市选择 web/Skip/cityArea
         * 8 文件提交 web/Skip/uploadFile
         * 9 人员列表 web/Skip/userList
         * 10 根据基金ID获取项目
         * 11 部门 web/Skip/departList
         * 12 审批单
         */
        val skip_site: String)

data class OptionBean(val name: String,//选项名
                      val scal_unit: String? //外出套件时 选项的计时单位
)

data class ExtrasBean(val name: String,//可能是大写,当前时间等文字
                      var value: String//对应值
)

fun ControlBean.copyWithoutValue(isDelete: Boolean? = false): ControlBean {
    var newChild: MutableList<ControlBean>? = null
        (this.children == null || this.children.isEmpty()).yes {
        newChild = null
    }.otherWise {
        newChild = mutableListOf()
        this.children?.forEach {
            newChild?.add(it.copyWithoutValue(it.is_delete))
        }
    }
    return ControlBean(control = control,
            componentName = componentName,
            fields = fields, name = name, name1 = name1, name2 = name2, name3 = name3,
            is_must = is_must, is_delete = isDelete, is_disabled = is_disabled, is_extras = is_extras,
            is_fresh = is_fresh, is_multiple = is_multiple, is_must_fund = is_must_fund, is_must_pro = is_must_pro,
            is_scal = is_scal, is_show = is_show, placeholder = placeholder, skip = skip, scal_unit = scal_unit, uint = uint,
            options = options, format = format, linkText = linkText, stable = stable, extras = extras,
            value = mutableListOf(),
            children = newChild)
}
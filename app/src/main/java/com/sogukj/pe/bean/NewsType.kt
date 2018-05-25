package com.sogukj.pe.bean

/**
 * Created by qinfei on 17/7/31.
 */
interface NewsType {
    enum class _1 :NewsType{
        submittime, //:2017-07-26 19:34:22,
        title, //:111,
        casetype, //:民事案件,
        caseno, //:（2016）晋0106民初1174号,
        court, //:太原市迎泽区人民法院,
        doctype, //:民事裁定书,
        url, //:http:\/\/wenshu.com,
        uuid, //:e1bf8bb9f39446809c6c839669ad4a84
//    	submittime	Datatime	提交时间
//    	title	Varchar	标题
//    	casetype	Varchar	案件类型
//    	caseno	Varchar	案件号
//    	court	Varchar	法院
//    	doctype	Varchar	文书类型
//    	url	Varchar	原文链接地址
//    	uuid	Varchar	唯一标识符
    }

    enum class _2 :NewsType{
        publishdate, //	Datatime	刊登日期
        party1, //	Varchar	原告
        party2, //	Varchar	当事人
        bltntypename, //	Varchar	公告类型名称
        courtcode, //	Varchar	法院名
        content, //	Text	案件内容
    }

    enum class _3 :NewsType{
        iname, //	Varchar	失信人名或公司名称
        casecode, //	Varchar	执行依据文号
        cardnum, //	Varchar	身份证号／组织机构代码
        areaname, //	Varchar	省份
        courtname, //	Varchar	执行法院
        gistid, //	Varchar	案号
        regdate, //	Datetime	立案时间
        gistunit, //	Varchar	做出执行依据单位
        duty, //	Text	法律生效文书确定的义务
        performance, //	Varchar	被执行人的履行情况
        publishdate, //	Datetime	发布时间
    }

    enum class _4 :NewsType{
        caseCreateTime, //	Datetime	立案时间
        execMoney, //	Varchar	执行标的
        caseCode, //	Varchar	案号
        execCourtName, //	Varchar	执行法院
    }

    enum class _5 :NewsType{
        decisionDate, //	Datetime	行政处罚日期
        punishNumber, //	Varchar	行政处罚决定书文号
        type, //	Varchar	违法行为类型
        departmentName, //	Varchar	作出行政处罚决定机关名称
        content, //	Text	行政处罚内容
    }

    enum class _6 :NewsType{
        putDate, //	Datetime	列入日期
        putReason, //	Text	列入原因
        putDepartment, //	Varchar	决定列入部门(作出决定机关
        removeReason, //	Text	移除原因
        removeDepartment, //	Varchar	决定移除部门
    }

    enum class _7 :NewsType{
        regNumber, //	Varchar	登记编号
        pledgor, //	Varchar	出质人
        pledgee, //	Varchar	质权人
        state, //	Varchar	状态
        equityAmount, //	Smallint	出质股权数额
        certifNumberR, //	Varchar	质权人证照/证件号码
        regDate, //	Datetime	股权出质设立登记日期
    }

    enum class _8 :NewsType{
        regDate, //	Datetme	登记日期
        regNum, //	Varchar	登记编号
        type, //	Varchar	被担保债权种类
        amount, //	Varchar	被担保债权数额
        regDepartment, //	Varchar	登记机关
        term, //	Varchar	债务人履行债务的期限
        scope, //	Varchar	担保范围
        remark, //	Text	备注
        overviewType, //	Varchar	概况种类
        overviewAmount, //	Varchar	概况数额
        overviewScope, //	Varchar	概况担保的范围
        overviewTerm, //	Varchar	概况债务人履行债务的期限
        overviewRemark, //	Text	概况备注
        changeInfoList, //	Text	总抵押变更 json数据
        pawnInfoList, //	Text	json数据
        peopleInfoList, //	Text	抵押人信息json数据
    }

    enum class _9(val key: String? = null) :NewsType{
        _name("name"), // Varchar  纳税人名称
        taxCategory, // Varchar  欠税税种
        personIdNumber, // Varchar  证件号码
        legalpersonName, // Varchar  法人或负责人名称
        location, // Varchar  经营地点
        newOwnTaxBalance, // Int    当前新发生欠税余额
        ownTaxBalance, // Int    欠税余额
        taxIdNumber, // Varchar  纳税人识别号
        type, //    Tinyint  类型     0国税 1地税
        time, //     Datetime  发布时间
        tax_authority; // Varchar  税务机关

        override fun toString(): String {
            return if (key != null) key else name
        }
    }

    enum class _10 :NewsType{
        putDate, //       Datetime  列入日期
        putReason, // Varchar  列入经营异常名录原因
        putDepartment, // Varchar  列入部门
        removeDate, //    Datetime  移出日期
        removeReason, // Varchar  移出原因
        removeDepartment, // Varchar  移出部门
    }

    enum class _11 :NewsType{
        case_name, // Varchar  案由
        caseno, // Varchar  案号
        court_date, //     Datetime  开庭日期
        schedu_date, //      Datetime  排期日期
        undertake_department, // Varchar  承办部门      
        presiding_judge, // Varchar  审判长/主审人
        appellant, // Varchar  上诉人
        appellee, // Varchar  被上诉人
        court, // Varchar  法院
        courtroom, // Varchar  法庭
        area, // Varchar  地区
    }

    enum class _12 :NewsType{
        title, // Varchar   标题
        auction_time, //    Datetime  委托法院拍卖时间
        entrusted_court, // Varchar   委托法院内容
        content, //      Text      内容
    }

    enum class _13 :NewsType{
        format_content
    }
}
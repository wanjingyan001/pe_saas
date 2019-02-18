/**
 * Copyright (C), 2018-2019, 搜股科技有限公司
 * FileName: BaiWangBill
 * Author: admin
 * Date: 2019/2/15 下午4:11
 * Description: 百望纸质发票返回结果
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
package com.sogukj.pe.bean;

import java.io.Serializable

/**
 * @ClassName: BaiWangBill
 * @Description: 百望纸质发票返回结果
 * @Author: admin
 * @Date: 2019/2/15 下午4:11
 */
data class BaiWangBill(
        val FPZT: String?,
        val FP_DM: String?,
        val FP_HM: String?,
        val FP_URL: Any,
        val JSHJ: String?,
        val KPRQ: String?
) : Serializable
/**
 * Copyright (C), 2018-2018, 搜股科技有限公司
 * FileName: PayPushAttachment
 * Author: admin
 * Date: 2018/11/19 下午5:37
 * Description: 支付推送自定义附件
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
package com.sogukj.pe.module.im.msg_viewholder

import com.sogukj.pe.baselibrary.Extended.jsonStr
import com.sogukj.pe.bean.PayHistory

/**
 * @ClassName: PayPushAttachment
 * @Description: 支付推送自定义附件
 * @Author: admin
 * @Date: 2018/11/19 下午5:37
 */
class PayPushAttachment :CustomAttachment(){
    lateinit var payPushBean: PayHistory
    override fun toJson(send: Boolean): String {
        return payPushBean.jsonStr
    }
}
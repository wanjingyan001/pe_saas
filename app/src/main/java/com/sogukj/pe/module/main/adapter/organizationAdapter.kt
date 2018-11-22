/**
 * Copyright (C), 2018-2018, 搜股科技有限公司
 * FileName: organizationAdapter
 * Author: admin
 * Date: 2018/11/22 上午11:09
 * Description: 组织架构Adapter
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
package com.sogukj.pe.module.main.adapter;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity

/**
 * @ClassName: organizationAdapter
 * @Description: 组织架构Adapter
 * @Author: admin
 * @Date: 2018/11/22 上午11:09
 */
class organizationAdapter(data: MutableList<MultiItemEntity>) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(data) {
    override fun convert(helper: BaseViewHolder?, item: MultiItemEntity?) {

    }
}
package com.sogukj.pe.baselibrary.utils

import com.scwang.smartrefresh.layout.header.ClassicsHeader

/**
 * 刷新控件配置,只添加了常用属性
 * Created by admin on 2018/5/25.
 */
class RefreshConfig {
    companion object {
        val Default = RefreshConfig()
    }

    /**
     * 显示下拉高度/手指真实下拉高度=阻尼效果
     */
    var dragRate = 0.5f
    /**
     * 是否启用下拉刷新功能
     */
    var refreshEnable = true
    /**
     * 是否启用上拉加载功能
     */
    var loadMoreEnable = false
    /**
     * 是否启用列表惯性滑动到底部时自动加载更多
     */
    var autoLoadMoreEnable = false
    /**
     *是否启用嵌套滚动
     */
    var nestedScrollEnable = false
    /**
     * 是否启用越界回弹
     */
    var overScrollBounceEnable = true
    /**
     * 是否在列表不满一页时候开启上拉加载功能
     */
    var loadMoreWhenContentNotFull = true
    /**
     * /是否启用越界拖动（仿苹果效果）
     */
    var overScrollDrag = true
    /**
     *  设置是否开启在加载时候禁止操作内容视图
     */
    var disableContentWhenLoading = false
    /**
     * 设置是否开启在刷新时候禁止操作内容视图
     */
    var disableContentWhenRefresh = false
    /**
     * 设置是否在加载更多完成之后滚动内容显示新数据
     */
    var scrollContentWhenLoaded = false
    /**
     * 设置是否启用内容视图拖动效果
     */
    var footerTranslationContent = false
}
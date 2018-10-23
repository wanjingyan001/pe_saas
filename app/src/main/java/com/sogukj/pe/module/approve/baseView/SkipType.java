package com.sogukj.pe.module.approve.baseView;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by admin on 2018/9/27.
 */
@IntDef({SkipType.NO_REQUEST,SkipType.SHOW_DIALOG,SkipType.CHILD_PAGE,SkipType.DATA_INPUT,SkipType.SKIP_LINK})
@Retention(RetentionPolicy.SOURCE)
public @interface SkipType {
    /**
     * 不请求
     */
    int NO_REQUEST = 0;
    /**
     * 弹窗
     */
    int SHOW_DIALOG = 1;
    /**
     * 二级页面
     */
    int CHILD_PAGE = 2;
    /**
     * 直接填充数据
     */
    int DATA_INPUT = 3;
    /**
     * 跳转链接
     */
    int SKIP_LINK = 4;

}

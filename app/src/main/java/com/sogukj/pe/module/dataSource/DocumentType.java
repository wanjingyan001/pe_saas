package com.sogukj.pe.module.dataSource;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by admin on 2018/9/5.
 */
@IntDef({DocumentType.INTELLIGENT, DocumentType.EQUITY, DocumentType.INDUSTRY_REPORTS})
@Retention(RetentionPolicy.SOURCE)
public @interface DocumentType {
    /**
     * 智能文书
     */
    int INTELLIGENT = 1;
    /**
     * 招股书
     */
    int EQUITY = 2;
    /**
     * 热门行业研报
     */
    int INDUSTRY_REPORTS = 3;
}

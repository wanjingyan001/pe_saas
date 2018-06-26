package com.sogukj.pe.database

import com.chad.library.adapter.base.entity.SectionEntity

/**
 * Created by admin on 2018/6/22.
 */
class MainFunction : SectionEntity<MainFunIcon> {

    constructor(function: MainFunIcon) : super(function)

    constructor(isHeader: Boolean, header: String) : super(isHeader, header)
}
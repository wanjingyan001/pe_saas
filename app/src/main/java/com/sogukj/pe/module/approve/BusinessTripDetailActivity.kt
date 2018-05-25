package com.sogukj.pe.module.approve

import android.os.Bundle
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity

class BusinessTripDetailActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_trip_detail)
        setBack(true)
        title = "出差明细"
    }
}

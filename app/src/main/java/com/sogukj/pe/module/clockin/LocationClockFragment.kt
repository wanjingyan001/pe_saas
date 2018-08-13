package com.sogukj.pe.module.clockin

import android.os.Bundle
import android.view.View

import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.widgets.MyMapView
import kotlinx.android.synthetic.main.fragment_location_clock.*
import org.jetbrains.anko.support.v4.ctx

class LocationClockFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_location_clock

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var map = MyMapView(ctx)
        waichudaka.setOnClickListener {
            map.show(savedInstanceState)
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

    }
}

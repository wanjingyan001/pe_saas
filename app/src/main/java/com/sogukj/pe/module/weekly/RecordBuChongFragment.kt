package com.sogukj.pe.module.weekly


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.bean.WeeklyThisBean
import kotlinx.android.synthetic.main.buchong_full.*


/**
 * A simple [Fragment] subclass.
 */
class RecordBuChongFragment : BaseFragment() {

    override val containerViewId: Int
        get() = R.layout.fragment_record_bu_chong

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buchong_edit.visibility = View.GONE

        var week = arguments!!.getSerializable(Extras.DATA) as WeeklyThisBean.Week

        var S_TIME = week.start_time?.split("-")
        var E_TIME = week.end_time?.split("-")

        time.text = week.time
        times.text = "${S_TIME?.get(1)}.${S_TIME?.get(2)}-${E_TIME?.get(1)}.${E_TIME?.get(2)}"
        info.text = week.info
    }

    companion object {
        fun newInstance(data: WeeklyThisBean.Week? = null): RecordBuChongFragment {
            val fragment = RecordBuChongFragment()
            var args = Bundle()
            args.putSerializable(Extras.DATA, data)
            fragment.arguments = args
            return fragment
        }
    }
}

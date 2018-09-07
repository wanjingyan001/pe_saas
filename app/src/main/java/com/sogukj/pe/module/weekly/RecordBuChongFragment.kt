package com.sogukj.pe.module.weekly


import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sogukj.pe.Extras

import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.bean.WeeklyThisBean
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.widgets.CircleImageView
import kotlinx.android.synthetic.main.buchong_full.*
import org.jetbrains.anko.support.v4.ctx


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

        //time.text = week.time
        //times.text = "${S_TIME?.get(1)}.${S_TIME?.get(2)}-${E_TIME?.get(1)}.${E_TIME?.get(2)}"
        infoTv.text = week.info

        var user = Store.store.getUser(baseActivity!!)
        loadHead(buchong_full.findViewById<CircleImageView>(R.id.circleImageView))
        buchong_full.findViewById<TextView>(R.id.name).setText("${user!!.name}的周报补充记录")
        var YMD = week.date!!.split(" ")[0]
        var HMS = week.date!!.split(" ")[1]
        buchong_full.findViewById<TextView>(R.id.timeTv).text = "${YMD.split("-")[1]}月${YMD.split("-")[2]}日      ${HMS.substring(0, 5)}"
    }

    fun loadHead(header: CircleImageView) {
        val user = Store.store.getUser(baseActivity!!)
        if (user?.url.isNullOrEmpty()) {
            val ch = user?.name?.first()
            header.setChar(ch)
        } else {
            Glide.with(ctx)
                    .load(MyGlideUrl(user?.url))
                    .listener(object : RequestListener<Drawable> {
                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            header.setImageDrawable(resource)
                            return true
                        }

                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            val ch = user?.name?.first()
                            header.setChar(ch)
                            return true
                        }
                    })
                    .into(header)
        }
    }

    companion object {
        fun newInstance(data: WeeklyThisBean.Week? = null): RecordBuChongFragment {
            val fragment = RecordBuChongFragment()
            var args = Bundle()
            args.putSerializable(Extras.DATA, data)
            fragment.setArguments(args)
            return fragment
        }
    }
}

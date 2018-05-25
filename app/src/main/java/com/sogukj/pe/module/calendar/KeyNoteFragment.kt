package com.sogukj.pe.module.calendar


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.KeyNode
import com.sogukj.pe.service.CalendarService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_key_note.*
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.ctx
import java.text.SimpleDateFormat


/**
 * A simple [Fragment] subclass.
 * Use the [KeyNoteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class KeyNoteFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_key_note
    lateinit var adapter: RecyclerAdapter<KeyNode>

    private var companyId: String? = null
    private var mParam2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            companyId = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = RecyclerAdapter(ctx, { _adapter, parent, position ->
            val convertView = _adapter.getView(R.layout.item_matter_detail_list, parent)
            object : RecyclerHolder<KeyNode>(convertView) {
                val year = convertView.find<TextView>(R.id.year)
                val dayOfMonth = convertView.find<TextView>(R.id.dayOfMonth)
                val hour = convertView.find<TextView>(R.id.hour)
                val matter_content = convertView.find<TextView>(R.id.matter_content)
                override fun setData(view: View, data: KeyNode, position: Int) {
                    var date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(data.end_time)
                    year.text = Utils.getTime(date, "yyyy年")
                    dayOfMonth.text = Utils.getTime(date, "MM月dd日")
                    hour.text = Utils.getTime(date, "HH:mm")
                    matter_content.text = data.title
                }
            }
        })
        nodeList.layoutManager = LinearLayoutManager(context)
        nodeList.adapter = adapter

        companyId?.let { doRequest(it) }
    }

    fun doRequest(id: String) {
        SoguApi.getService(baseActivity!!.application,CalendarService::class.java)
                .projectMatter(company_id = id.toInt())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.let {
                            adapter.dataList.addAll(it)
                            adapter.notifyDataSetChanged()
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment KeyNoteFragment.
         */
        fun newInstance(param1: String, param2: String): KeyNoteFragment {
            val fragment = KeyNoteFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}

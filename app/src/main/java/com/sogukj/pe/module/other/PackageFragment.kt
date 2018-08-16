package com.sogukj.pe.module.other


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.PackageBean
import com.sogukj.pe.bean.PackageChild
import kotlinx.android.synthetic.main.fragment_package.*
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.ctx


/**
 * A simple [Fragment] subclass.
 * Use the [PackageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PackageFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_package
    private var mParam1: PackageBean? = null
    lateinit var payAdapter: RecyclerAdapter<PackageChild>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getParcelable(ARG_PARAM1)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        payAdapter = RecyclerAdapter(ctx) { _adapter, parent, _ ->
            val itemView = _adapter.getView(R.layout.item_package_layout, parent)
            object : RecyclerHolder<PackageChild>(itemView) {
                val amount = itemView.find<TextView>(R.id.amount)
                val money = itemView.find<TextView>(R.id.money)
                val selectImg = itemView.find<ImageView>(R.id.selectImg)
                override fun setData(view: View, data: PackageChild, position: Int) {
                    amount.text = data.name
                    money.text = data.pricestr
                    selectImg.setVisible(payAdapter.selectedPosition == position)
                    itemView.isSelected = payAdapter.selectedPosition == position
                }
            }
        }
        payAdapter.onItemClick = { v, p ->
            payAdapter.selectedPosition = p
        }
        payAdapter.selectChange = { old, new ->
            val activity = activity as PayPackageActivity
            if (payAdapter.selectedPosition != -1) {
                activity.sId = payAdapter.dataList[payAdapter.selectedPosition].id ?: 0
            } else {
                activity.sId = 0
            }
        }
        mParam1?.let {
            payAdapter.dataList.addAll(it.list)
        }
        packageList.layoutManager = GridLayoutManager(ctx, 2)
        packageList.adapter = payAdapter
    }


    companion object {
        private val ARG_PARAM1 = "param1"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PackageFragment.
         */
        fun newInstance(param1: PackageBean): PackageFragment {
            val fragment = PackageFragment()
            val args = Bundle()
            args.putParcelable(ARG_PARAM1, param1)
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor

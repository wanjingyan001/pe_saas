package com.sogukj.pe.module.im
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import kotlinx.android.synthetic.main.activity_address_search.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk25.coroutines.textChangedListener

class AddressSearchActivity : BaseActivity(), TextView.OnEditorActionListener, PoiSearch.OnPoiSearchListener, View.OnClickListener {
    private lateinit var query: PoiSearch.Query
    private lateinit var poiSearch: PoiSearch
    private lateinit var city: String
    private lateinit var mAdapter: RecyclerAdapter<PoiItem>

    companion object {
        fun start(context: Activity, city: String) {
            val intent = Intent(context, AddressSearchActivity::class.java)
            intent.putExtra(Extras.NAME, city)
            context.startActivityForResult(intent, Extras.REQUESTCODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        city = intent.getStringExtra(Extras.NAME)
        setContentView(R.layout.activity_address_search)
        Utils.setWindowStatusBarColor(this, R.color.white)
        mAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            val itemView = _adapter.getView(R.layout.item_address_search_list, parent)
            object : RecyclerHolder<PoiItem>(itemView) {
                val name = itemView.find<TextView>(R.id.poiName)
                val address = itemView.find<TextView>(R.id.poiAddress)
                override fun setData(view: View, data: PoiItem, position: Int) {
                    name.text = data.title
                    address.text = "${data.cityName}${data.adName}${data.snippet}"
                }
            }
        }
        mAdapter.onItemClick = { v, position ->
            val intent = Intent()
            intent.putExtra(Extras.DATA, mAdapter.dataList[position])
            setResult(Extras.RESULTCODE, intent)
            finish()
        }
        searchResultList.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = mAdapter
            addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
        }
        searchEdt.filters = Utils.getFilter(this)
        searchEdt.isFocusable = true
        searchEdt.isFocusableInTouchMode = true
        searchEdt.requestFocus()
        Utils.toggleSoftInput(this, searchEdt)
        searchEdt.setOnEditorActionListener(this)
        searchEdt.textChangedListener {
            onTextChanged { charSequence, start, before, count ->
                if (searchEdt.textStr.isNotEmpty()) {
                    searchClear.setVisible(true)
                } else {
                    searchClear.setVisible(false)
                }
            }
        }
        back.setOnClickListener(this)
        searchClear.setOnClickListener(this)
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        return if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            if (searchEdt.textStr.isNotEmpty()) {
                Utils.closeInput(this, searchEdt)
                initSearch(searchEdt.textStr)
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    private fun initSearch(searchKey: String) {
        query = PoiSearch.Query(searchKey, "", city)
        query.pageSize = 20
        query.pageNum = 0
        poiSearch = PoiSearch(this, query)
        poiSearch.setOnPoiSearchListener(this)
        poiSearch.searchPOIAsyn()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back -> {
                finish()
            }
            R.id.searchClear -> {
                searchEdt.setText("")
                searchEdt.isFocusable = true
                searchEdt.isFocusableInTouchMode = true
                searchEdt.requestFocus()
                Utils.toggleSoftInput(this, searchEdt)
            }
        }
    }


    override fun onPoiItemSearched(p0: PoiItem?, p1: Int) {
    }

    override fun onPoiSearched(result: PoiResult?, rCode: Int) {
        if (rCode == 1000) {
            result?.let {
                mAdapter.dataList.clear()
                mAdapter.dataList.addAll(it.pois)
                mAdapter.notifyDataSetChanged()
                if (mAdapter.dataList.isEmpty()) {
                    searchResultList.setVisible(false)
                    emptyLayout.setVisible(true)
                } else {
                    searchResultList.setVisible(true)
                    emptyLayout.setVisible(false)
                }
            }
        } else {
            searchResultList.setVisible(false)
            emptyLayout.setVisible(true)
        }
    }
}

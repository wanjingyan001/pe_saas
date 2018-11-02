package com.sogukj.pe.module.approve

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.widget.GridView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.bean.CityArea
import com.sogukj.pe.bean.CityAreaBean
import com.sogukj.pe.bean.CityBean
import com.sogukj.pe.module.approve.adapter.*
import com.sogukj.pe.module.approve.presenter.RequestCityPresenter
import com.sogukj.pe.widgets.indexbar.DividerItemDecoration
import com.sogukj.pe.widgets.indexbar.SuspensionDecoration
import kotlinx.android.synthetic.main.activity_new_dst_city.*









/**
 * Created by CH-ZH on 2018/8/28.
 */
class NewDstCityActivity : ToolbarActivity(),DstCityCallBack {
    override fun setHisCityData(city: ArrayList<CityArea.City>) {
        this.hisCityData = city
        mHeaderAdapter!!.notifyDataSetChanged()
    }

    lateinit var inflater: LayoutInflater
    private var presenter: RequestCityPresenter? = null
    private var mManager: LinearLayoutManager? = null
    private var mAdapter : NewCityAdapter? = null
    private var choseAdapter : ChosenAdapter? = null
    private var hisAdapter : HistoryAdapter? = null
    private var mHeaderAdapter : HeaderRecyclerAndFooterWrapperAdapter? = null
    private var mDecoration : SuspensionDecoration ? = null
    private var hisCityData = ArrayList<CityArea.City>()
    private var mDatas = ArrayList<CityBean>()
    private var primeChoseDatas = ArrayList<CityArea.City>()
    private var choseCitys = ArrayList<CityArea.City>()
    companion object {
        val TAG = NewDstCityActivity::class.java.simpleName
        fun invoke(ctx: Activity, id: Int, list: ArrayList<CityArea.City>) {
            val intent = Intent(ctx, NewDstCityActivity::class.java)
            intent.putExtra(Extras.ID, id)
            intent.putExtra(Extras.DATA, list)
            ctx.startActivityForResult(intent, Extras.REQUESTCODE)
        }
    }

    override fun onBackPressed() {
        if (choseAdapter!!.data.size <= 0) {
            showCustomToast(R.drawable.icon_toast_common,"未选择城市")
            setResult(Extras.RESULTCODE2)
            super.onBackPressed()
            return
        }
        var list = choseAdapter!!.data
        var intent = Intent()
        intent.putExtra(Extras.DATA, list)
        setResult(Extras.RESULTCODE, intent)
        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_dst_city)
        setBack(true)
        title = "目的城市"
        inflater = LayoutInflater.from(context)
        initView()
        initData()
        bindListener()

    }

    private fun initView() {
        mManager = LinearLayoutManager(this)
        rv.layoutManager = mManager
        indexBar.setmPressedShowTextView(tvSideBarHint)
                .setNeedRealIndex(true)
                .setmLayoutManager(mManager)
    }

    private fun initData() {
        presenter = RequestCityPresenter(this,this)
        mAdapter = NewCityAdapter(this,mDatas)
        choseAdapter = ChosenAdapter(this)
        hisAdapter = HistoryAdapter(this)
        setHeaderAdapter()
        mHeaderAdapter!!.addHeaderView(R.layout.item_city_top, null)
        rv.adapter = mHeaderAdapter
        mDecoration = SuspensionDecoration(this,mDatas)
        mDecoration!!.headerViewCount = mHeaderAdapter!!.headerViewCount
        rv.addItemDecoration(mDecoration)
        rv.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL_LIST))
        getChoseData()
        getHistoryData()
        getCityAreaData()
    }

    private fun getChoseData() {
        primeChoseDatas = intent.getSerializableExtra(Extras.DATA) as ArrayList<CityArea.City>
        if (null != primeChoseDatas && primeChoseDatas.size > 0){
            choseCitys.addAll(primeChoseDatas)
            mHeaderAdapter!!.notifyDataSetChanged()
        }
    }

    private fun getHistoryData() {
        var id = intent.getIntExtra(Extras.ID, 0)
        if (null != presenter){
            presenter!!.doRequest(id)
        }
    }

    private fun setHeaderAdapter() {
        mHeaderAdapter = object : HeaderRecyclerAndFooterWrapperAdapter(mAdapter){
            override fun onBindHeaderHolder(holder: ViewHolder?, headerPos: Int, layoutId: Int, o: Any?) {
                setHistoryData(holder)
                setChoseData(holder)
            }
        }

    }

    private fun setChoseData(holder: ViewHolder?) {
        if(null != choseCitys && choseCitys.size > 0) {
            holder!!.setVisible(R.id.empty_chosen,true)
        }else{
            holder!!.setVisible(R.id.empty_chosen,false)
        }

        val gv_chose = holder.getView<GridView>(R.id.chosenGrid)
        choseAdapter!!.data = choseCitys
        gv_chose.adapter = choseAdapter
        choseAdapter!!.notifyDataSetChanged()

        gv_chose.setOnItemClickListener { parent, view, position, id ->
            updateChoseData(position)
        }
    }

    private fun updateChoseData(position: Int) {
        var realChoseCity = ArrayList<CityArea.City>()
        val city = choseCitys[position]
        if (choseCitys.size > 0){
            for (info in choseCitys){
                if (!info.name.equals(city.name)){
                    realChoseCity.add(info)
                }
            }
            choseCitys.clear()
            choseCitys.addAll(realChoseCity)
        }

        for (bean in mDatas) {
            if (city.name.equals(bean.city)) {
                bean.isSeclected = !bean.isSeclected
            }
        }
        mHeaderAdapter!!.notifyDataSetChanged()
        mAdapter!!.notifyDataSetChanged()
    }

    private fun setHistoryData(holder: ViewHolder?) {
        if(null != hisCityData && hisCityData.size > 0) {
            holder!!.setVisible(R.id.empty_history,true)
        }else{
            holder!!.setVisible(R.id.empty_history,false)
        }

        val gv_his = holder.getView<GridView>(R.id.historyGrid)
        hisAdapter!!.data = hisCityData
        gv_his.adapter = hisAdapter
        hisAdapter!!.notifyDataSetChanged()

        gv_his.setOnItemClickListener { parent, view, position, id ->
            updateHistoryData(position)
        }
    }

    private fun updateHistoryData(position: Int) {
        var names = ""
        if (choseCitys.size > 0) {
            for (info in choseCitys) {
                names += info.name + ","
            }
        }
        if (hisCityData.size > 0) {
            val city = hisCityData[position]
            if (names.contains(city.name!!)) {
                //选择包含历史
                val realCitys = ArrayList<CityArea.City>()
                if (choseCitys.size > 0) {
                    for (info in choseCitys) {
                        if (!info.name.equals(city.name!!)) {
                            realCitys.add(info)
                        }
                    }
                    choseCitys.clear()
                    choseCitys.addAll(realCitys)
                }
                for (bean in mDatas) {
                    if (city.name.equals(bean.city)) {
                        bean.isSeclected = !bean.isSeclected
                    }
                }
                mHeaderAdapter!!.notifyDataSetChanged()
                mAdapter!!.notifyDataSetChanged()
            } else {
                //选择没有历史
                if (choseCitys.size < 6) {
                    //可以添加
                    choseCitys.add(city)
                    for (bean in mDatas) {
                        if (city.name.equals(bean.city)) {
                            bean.isSeclected = !bean.isSeclected
                        }
                    }
                    mHeaderAdapter!!.notifyDataSetChanged()
                    mAdapter!!.notifyDataSetChanged()

                } else {
                    showCustomToast(R.drawable.icon_toast_common,"目的城市数目不能超过6个")
                }
            }
        }
    }

    private fun getCityAreaData() {
        var datas = XmlDb.open(this).get(Extras.CITY_JSON, "")
        if (null == datas || "".equals(datas)){
            datas = Utils.getJson(this,"city.json")
        }
        val cityAreaBean = Utils.JsonToObject(datas, CityAreaBean::class.java)
        if (null != cityAreaBean){
            setCityAreaData(cityAreaBean)
        }
    }

    private fun setCityAreaData(cityAreaBean: CityAreaBean) {
        val list = intent.getSerializableExtra(Extras.DATA) as ArrayList<CityArea.City>
        var name = ""
        val payload = cityAreaBean.payload
        var names = ArrayList<String>()
        if (null != payload && payload.size > 0){
            for (area in payload){
                val citys = area.city
                if (null != citys && citys.size > 0){
                    for (info in citys){
                        names.add(info.name!!)
                    }
                }
            }
        }

        for (name in names){
            var cityBean = CityBean()
            cityBean.city = name
            mDatas.add(cityBean)
        }

        indexBar.setmSourceDatas(mDatas)
                .setHeaderViewCount(mHeaderAdapter!!.headerViewCount)
                .invalidate()

        mAdapter!!.datas = mDatas
        mHeaderAdapter!!.notifyDataSetChanged()
        mDecoration!!.setmDatas(mDatas)

        if (null != list && list.size > 0){
            for (info in list){
                name += info.name + ","
            }
            for (city in mDatas){
                if (name.contains(city.city)){
                    city.isSeclected = !city.isSeclected
                }
            }
            mAdapter!!.notifyDataSetChanged()
        }


    }

    private fun bindListener() {
        toolbar_menu.setOnClickListener {
            //置顶
            rv.scrollToPosition(0)
        }

        mAdapter!!.setOnCityItemClickListener { city, position ->
            updateDstCityData(city,position)
        }
    }

    private fun updateDstCityData(city: String, position: Int) {
        val cityBean = mDatas[position]
        if (choseCitys.size >= 6 && !cityBean.isSeclected) {
            showCustomToast(R.drawable.icon_toast_common,"目的城市数目不能超过6个")
        } else {
            cityBean.isSeclected = !cityBean.isSeclected
            mAdapter!!.notifyDataSetChanged()
            val realCitys = ArrayList<CityArea.City>()
            if (!cityBean.isSeclected) {
                if (choseCitys.size > 0) {
                    for (info in choseCitys) {
                        if (!info.name.equals(city)) {
                            realCitys.add(info)
                        }
                    }
                    choseCitys.clear()
                    choseCitys.addAll(realCitys)
                }
            } else {
                val choseCity = CityArea.City()
                choseCity.name = city
                choseCitys.add(choseCity)
            }
            mHeaderAdapter!!.notifyDataSetChanged()
        }
    }

}
package com.sogukj.pe.module.approve

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.CharacterParser
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.MyListView
import com.sogukj.pe.baselibrary.widgets.SideBar
import com.sogukj.pe.bean.CityArea
import com.sogukj.pe.module.approve.adapter.ChosenAdapter
import com.sogukj.pe.module.approve.adapter.CityAdapter
import com.sogukj.pe.module.approve.adapter.HistoryAdapter
import com.sogukj.pe.peUtils.CacheUtils
import com.sogukj.pe.service.ApproveService
import com.sogukj.pe.service.UserService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_dst_city.*

class DstCityActivity : ToolbarActivity() {

    lateinit var inflater: LayoutInflater

    companion object {
        val TAG = DstCityActivity::class.java.simpleName
        fun start(ctx: Activity, id: Int, list: ArrayList<CityArea.City>) {
            val intent = Intent(ctx, DstCityActivity::class.java)
            intent.putExtra(Extras.ID, id)
            intent.putExtra(Extras.DATA, list)
            ctx.startActivityForResult(intent, Extras.REQUESTCODE)
        }
    }

    override fun onBackPressed() {
        if (chosenAdapter.data.size == 0) {
            showCustomToast(R.drawable.icon_toast_common,"未选择城市")
            setResult(Extras.RESULTCODE2)
            super.onBackPressed()
            return
        }
        var list = chosenAdapter.data
        var intent = Intent()
        intent.putExtra(Extras.DATA, list)
        setResult(Extras.RESULTCODE, intent)
        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dst_city)
        setBack(true)
        title = "目的城市"

        inflater = LayoutInflater.from(context)

        cache = CacheUtils(context)

        initView()

        toolbar_menu.setOnClickListener {
            mRootScrollView.smoothScrollTo(0, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cache.close()
    }

    lateinit var cache: CacheUtils

    //实例化汉字转拼音类
    private var characterParser = CharacterParser.getInstance()

    private var loadFinish = false

    private fun initView() {
        side_bar.setTextView(dialog)
        //设置右侧触摸监听
        side_bar.setOnTouchingLetterChangedListener(SideBar.OnTouchingLetterChangedListener { s ->
            if (!loadFinish) {
                return@OnTouchingLetterChangedListener
            }
            //该字母首次出现的位置
            val position = getPositionForSection(s!![0].toInt())
            Log.e("position", "${position}")
            Log.e("char", s.toString())
            if (position != -1) {
//                    var layout = mRootScrollView.findViewWithTag("group${position}") as LinearLayout
//
//                    mRootScrollView.post(Runnable {
//                        val location = IntArray(2)
//                        layout.getLocationInWindow(location)
//                        var offset = location[1] - getStatusBarHeight() + lastOffSet
//                        lastOffSet = offset
//                        mRootScrollView.smoothScrollTo(0, offset)
//                    })
                mRootScrollView.smoothScrollTo(0, scrollDistance[position])
            }
        })

        chosenAdapter = ChosenAdapter(context)
        chosenGrid.adapter = chosenAdapter
        var list = intent.getSerializableExtra(Extras.DATA) as ArrayList<CityArea.City>
        chosenAdapter.data.addAll(list)
        if (chosenAdapter.data.size == 0) {
            empty_chosen.visibility = View.GONE
        } else {
            empty_chosen.visibility = View.VISIBLE
        }
        chosenAdapter.notifyDataSetChanged()
        chosenGrid.setOnItemClickListener { parent, view, position, id ->
            var city = chosenAdapter.data[position]
            updateList(city, false)
            chosenAdapter.data.remove(city)
            chosenAdapter.notifyDataSetChanged()
            if (chosenAdapter.data.size == 0) {
                empty_chosen.visibility = View.GONE
            }
            reCalculate()
        }

        historyAdapter = HistoryAdapter(context)
        historyGrid.adapter = historyAdapter
        historyGrid.setOnItemClickListener { parent, view, position, id ->
            var city = historyAdapter.data.get(position)
            var contains = false
            var index = 0
            var dataList = chosenAdapter.data
            for (item in 0 until dataList.size) {
                if (dataList[item].id == city.id) {
                    contains = true
                    index = item
                    break
                }
            }
            if (contains) {
                //已点过
                updateList(city, false)
                chosenAdapter.data.removeAt(index)
                chosenAdapter.notifyDataSetChanged()
                if (chosenAdapter.data.size == 0) {
                    empty_chosen.visibility = View.GONE
                }
            } else {
                //未点过
                updateList(city, true)
                chosenAdapter.data.add(city)
                chosenAdapter.notifyDataSetChanged()
                if (chosenAdapter.data.size != 0) {
                    empty_chosen.visibility = View.VISIBLE
                }
            }
            reCalculate()
        }

        doRequest()
    }

    private var groups = ArrayList<String>()
    private var childs = ArrayList<ArrayList<CityArea.City>>()
    private var adapters = ArrayList<CityAdapter>()
    private var scrollDistance = ArrayList<Int>()
    private var cellHeight = ArrayList<Int>()

    fun reCalculate() {
        cityLayout.post {
            var childs = mRootScrollView.getChildAt(0) as LinearLayout
            var h0 = childs.getChildAt(0).measuredHeight//15dp
            var h1 = childs.getChildAt(1).measuredHeight//已选
            var h2 = childs.getChildAt(2).measuredHeight//divider
            var h3 = childs.getChildAt(3).measuredHeight//历史
            var h4 = childs.getChildAt(4).measuredHeight//divider

            scrollDistance.clear()

            if (empty_chosen.visibility == View.GONE) {
                scrollDistance.add(h0 + h2 + h3 + h4)
            } else {
                scrollDistance.add(h0 + h1 + h2 + h3 + h4)
            }
            for (distance in cellHeight) {
                var previous = scrollDistance.get(scrollDistance.lastIndex)
                scrollDistance.add((distance + previous))
            }
        }
    }

    fun calculateScroolDis() {
        cityLayout.post {
            //先找到cityLayout之前的距离
            var childs = mRootScrollView.getChildAt(0) as LinearLayout
            var h0 = childs.getChildAt(0).measuredHeight//15dp
            var h1 = childs.getChildAt(1).measuredHeight//已选
            var h2 = childs.getChildAt(2).measuredHeight//divider
            var h3 = childs.getChildAt(3).measuredHeight//历史
            var h4 = childs.getChildAt(4).measuredHeight//divider

            var beforeDis = h0 + h1 + h2 + h3 + h4
            scrollDistance.add(beforeDis)
            for (viewIndex in 0 until (cityLayout.childCount - 2) step 2) {
                var groupH = cityLayout.getChildAt(viewIndex).measuredHeight
                var childH = cityLayout.getChildAt(viewIndex + 1).measuredHeight
                var previous = scrollDistance.get(scrollDistance.lastIndex)
                cellHeight.add(groupH + childH)
                scrollDistance.add((groupH + childH + previous))
            }
        }
    }

    fun doRequest() {
        var cacheData = cache.getCityCache("city")
        if (cacheData == null || cacheData.size == 0) {
            //showToast("获取城市列表，请等待")
            SoguApi.getService(application, UserService::class.java)
                    .getCityArea()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {

                            cache.addToCityCache("city", payload.payload!!)

                            filledData(payload.payload!!)

                            side_bar.setB(groups)

                            initAdapter()

                            var list = intent.getSerializableExtra(Extras.DATA) as ArrayList<CityArea.City>
                            for (city in list) {
                                updateList(city, true)
                            }

                            calculateScroolDis()
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                    })
        } else {
            filledData(cacheData)

            side_bar.setB(groups)

            initAdapter()

            var list = intent.getSerializableExtra(Extras.DATA) as ArrayList<CityArea.City>
            for (city in list) {
                updateList(city, true)
            }

            calculateScroolDis()
        }

        var id = intent.getIntExtra(Extras.ID, 0)
        SoguApi.getService(application,ApproveService::class.java)
                .getHistoryCity(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        var list = payload.payload
                        if (list == null || list.isEmpty()) {
                            empty_history.visibility = View.GONE
                        } else {
                            empty_history.visibility = View.VISIBLE
                            historyAdapter.data.addAll(list)
                            historyAdapter.notifyDataSetChanged()
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }

    fun initAdapter() {
        loadFinish = false
        for (groupIndex in 0 until groups.size) {
            var Groupview = inflater.inflate(R.layout.item, null)
            var tvLetter = Groupview.findViewById<TextView>(R.id.catalog) as TextView
            tvLetter.text = groups[groupIndex]
            Groupview.tag = "group${groupIndex}"
            cityLayout.addView(Groupview)

            var Childview = inflater.inflate(R.layout.child_item_dstcity, null)
            cityLayout.addView(Childview)
            Childview.tag = "child${groupIndex}"
            var mCityLv = Childview.findViewById<MyListView>(R.id.city) as MyListView
            var adapter = CityAdapter(context, childs.get(groupIndex))
            adapters.add(adapter)
            mCityLv.adapter = adapter
            mCityLv.setOnItemClickListener { parent, view, position, id ->
                var city = childs.get(groupIndex)[position]
                city.seclected = !city.seclected
                addToCurrent(city)
                adapter.notifyDataSetChanged()
            }
        }
        loadFinish = true
    }

    lateinit var chosenAdapter: ChosenAdapter

    fun addToCurrent(city: CityArea.City) {
        if (city.seclected == false) {//点两次
            var index = 0
            var dataList = chosenAdapter.data
            for (item in 0 until dataList.size) {
                if (dataList[item].id == city.id) {
                    index = item
                    break
                }
            }
            chosenAdapter.data.removeAt(index)
            chosenAdapter.notifyDataSetChanged()
            if (chosenAdapter.data.size == 0) {
                empty_chosen.visibility = View.GONE
            }
            reCalculate()
            return
        }
        if (chosenAdapter.data.size == 6) {
            showCustomToast(R.drawable.icon_toast_common,"目的城市数目不能超过6个")
            //添加不了，注意selected需要更新
            updateList(city, false)
            return
        }
        chosenAdapter.data.add(city)
        chosenAdapter.notifyDataSetChanged()
        if (chosenAdapter.data.size != 0) {
            empty_chosen.visibility = View.VISIBLE
        }
        reCalculate()
    }

    fun updateList(city: CityArea.City, newState: Boolean) {
        for (fatherIndex in 0 until childs.size) {
            var child = childs[fatherIndex]
            for (childIndex in 0 until child.size) {
                if (child[childIndex].id == city.id) {
                    childs[fatherIndex][childIndex].seclected = newState
                    adapters[fatherIndex].notifyDataSetChanged()
                    break
                }
            }
        }
    }

    lateinit var historyAdapter: HistoryAdapter

    /**
     * 为ListView填充数据
     */
    private fun filledData(list: List<CityArea>) {
        //java 用for循环为一个字符串数组输入从a到z的值。//groups
        var result = ""
        var i = 0
        var j = 'a'
        while (i < 26) {
            groups.add((j.toString() + "").toUpperCase())
            result += j.toString() + " "//连起来，空格隔开
            i++
            j++
        }

        //获取所有城市列表
        var cities = ArrayList<CityArea.City>()
        for (provIndex in list.indices) {
            var province = list[provIndex]
            for (cityIndex in province.city!!.indices) {
                var city = province.city!![cityIndex]

                //汉字转换成拼音
                val sortString = characterParser.getAlpha(city.name).toUpperCase().substring(0, 1)
                // 正则表达式，判断首字母是否是英文字母
                if (sortString.matches("[A-Z]".toRegex())) {
                    city.sortLetters = sortString.toUpperCase()
                } else {
                    city.sortLetters = "#"
                }

                cities.add(city)
            }
        }

        //组装格式
        for (index in groups.indices) {
            var innerList = ArrayList<CityArea.City>()
            for (cityIndex in cities.indices) {
                if (cities[cityIndex].sortLetters.equals(groups[index])) {
                    innerList.add(cities[cityIndex])
                }
            }
            childs.add(innerList)
        }
        for (i in (childs.size - 1) downTo 0) {
            if (childs[i].size == 0) {
                childs.removeAt(i)
                groups.removeAt(i)
            }
        }
    }

    /**
     * 根据ListView的当前位置获取匪类的首字母的Char ascii值
     *
     * @param position
     * @return
     */
    fun getSectionForPosition(position: Int): Int {
        return groups.get(position).toInt()
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     *
     * @param section
     * @return
     */
    fun getPositionForSection(section: Int): Int {
        for (i in 0 until groups.size) {
            val sortStr = groups.get(i)
            val firstChar = sortStr.toUpperCase().get(0)
            if (firstChar.toInt() == section) {
                return i
            }
        }
        return -1
    }
}

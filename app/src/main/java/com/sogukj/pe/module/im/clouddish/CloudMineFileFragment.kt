package com.sogukj.pe.module.im.clouddish

import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.BaseRefreshFragment
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.CloudFileBean
import com.sogukj.pe.bean.CloudLevel1
import com.sogukj.pe.bean.CloudLevel2
import kotlinx.android.synthetic.main.fragment_mine_file.*
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.startActivity

/**
 * Created by CH-ZH on 2018/10/25.
 */
class CloudMineFileFragment : BaseRefreshFragment() {
    private var fileInfos = ArrayList<CloudFileBean>()
    private lateinit var adapter : RecyclerAdapter<CloudFileBean>
    private lateinit var alreadySelected: MutableSet<CloudFileBean>
    private var busAdapter : CloudExpandableAdapter ? = null
    private var type = 1 //1 : 我的文件 2 : 企业文件
    private var fileCount = 0
    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        config.disableContentWhenRefresh = true
        return config
    }

    override val containerViewId: Int
        get() = R.layout.fragment_mine_file

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments!!.getInt("type")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initData()
        bindListener()
    }

    private fun initData() {
        alreadySelected = ArrayList<CloudFileBean>().toMutableSet()
        Glide.with(ctx)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        recycler_view.layoutManager = LinearLayoutManager(activity)
        when(type){
            1 -> {
                for (i in 0 .. 15){
                    if (i == 0){
                        val fileBean = CloudFileBean()
                        fileBean.data = "2017/07/06 今天 13:09 "
                        fileBean.fileName = "WechatIM_文件夹"
                        fileBean.isFileDir = true
                        fileInfos.add(fileBean)
                    }else{
                        val fileBean = CloudFileBean()
                        fileBean.data = "2017/07/06 今天 13:09 "
                        fileBean.fileName = "关于投资宏大信息科技公司的….pdf${i}"
                        fileBean.isFileDir = false
                        fileInfos.add(fileBean)
                    }
                }
                recycler_view.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
                adapter = RecyclerAdapter(activity!!){_adapter,parent,_ ->
                    CloudFileHolder(_adapter.getView(R.layout.item_cloud_file,parent))
                }
                adapter.dataList.addAll(fileInfos)
                recycler_view.adapter = adapter
            }

            2 -> {
                setBusExpandableData()
            }
        }
    }

    private fun setBusExpandableData() {
        val res = ArrayList<MultiItemEntity>()
        val cloudName1 = CloudLevel1("搜股(上海)科技有限公司")

        val cloudItem1 = CloudLevel2()
        cloudItem1.name = "PE项目组"
        cloudItem1.time = "2017/07/06 今天 13:09"
        cloudItem1.size = "15.5M"
        val cloudItem2 = CloudLevel2()
        cloudItem2.name = "搜股大家庭"
        cloudItem2.time = "2017/07/06 今天 13:09"
        cloudItem2.size = "15.5M"
        val cloudItem3 = CloudLevel2()
        cloudItem3.name = "运动群"

        cloudName1.addSubItem(cloudItem1)
        cloudName1.addSubItem(cloudItem2)
        cloudName1.addSubItem(cloudItem3)

        res.add(cloudName1)
        busAdapter = CloudExpandableAdapter(res,activity!!)
        recycler_view.adapter = busAdapter
        busAdapter!!.expandAll()
    }

    private fun bindListener() {
        tv_comit.clickWithTrigger {
            //发送文件
            if (fileCount > 0){

            }else{
                showCommonToast("请至少选择一个文件")
            }
        }
    }

    override fun doRefresh() {
        dofinishRefresh()
    }

    override fun doLoadMore() {
        dofinishLoadMore()
    }

    fun showEmpty(){
        iv_empty.setVisible(true)
    }

    fun goneEmpty(){
        iv_empty.visibility = View.INVISIBLE
    }

    fun showLoadding(){
        view_recover.visibility = View.VISIBLE
    }

    fun goneLoadding(){
        view_recover.visibility = View.INVISIBLE
    }

    fun dofinishRefresh() {
        goneLoadding()
        if (null != refresh && refresh.isRefreshing) {
            refresh.finishRefresh()
        }
    }

    fun dofinishLoadMore() {
        if (null != refresh && refresh.isLoading) {
            refresh.finishLoadMore()
        }
    }

    inner class CloudFileHolder(itemView: View) : RecyclerHolder<CloudFileBean>(itemView) {
        val iv_select = itemView.find<ImageView>(R.id.iv_select)
        val file_icon = itemView.find<ImageView>(R.id.file_icon)
        val tv_summary = itemView.find<TextView>(R.id.tv_summary)
        val tv_fileSize = itemView.find<TextView>(R.id.tv_fileSize)
        val tv_time = itemView.find<TextView>(R.id.tv_time)
        override fun setData(view: View, data: CloudFileBean, position: Int) {
            if (null == data)return
            iv_select.isSelected = data.isSelect
            if (data.isFileDir){
                //文件夹
                iv_select.setVisible(false)
                file_icon.setImageResource(R.drawable.folder_zip)
            }else{
                iv_select.setVisible(true)
                file_icon.setImageResource(R.drawable.icon_pdf)
            }
            tv_summary.text = data.fileName
            tv_time.text = data.data

            itemView.clickWithTrigger {
                if (data.isFileDir){
                    startActivity<FileDirDetailActivity>(Extras.TITLE to data.fileName)
                }else{
                    if (alreadySelected.contains(data)){
                        alreadySelected.remove(data)
                        fileCount--
                        if (fileCount <= 0){
                            fileCount = 0
                        }
                    }else{
                        alreadySelected.add(data)
                        fileCount++
                    }

                    data.isSelect = !data.isSelect
                    iv_select.isSelected = data.isSelect
                    tv_total.text = "${fileCount}个"
                    if (fileCount > 0){
                        tv_all_title.setTextColor(resources.getColor(R.color.blue_17))
                        tv_total.setTextColor(resources.getColor(R.color.blue_17))
                        tv_comit.setBackgroundResource(R.drawable.selector_sure)
                    }else{
                        tv_all_title.setTextColor(resources.getColor(R.color.gray_d9))
                        tv_total.setTextColor(resources.getColor(R.color.gray_d9))
                        tv_comit.setBackgroundResource(R.drawable.selector_sure_gray)
                    }
                }
            }
        }
    }

    companion object {
        val TAG = CloudMineFileFragment::class.java.simpleName
        fun newInstance(type:Int): CloudMineFileFragment {
            val fragment = CloudMineFileFragment()
            val bundle = Bundle()
            bundle.putInt("type",type)
            fragment.arguments = bundle
            return fragment
        }
    }
}


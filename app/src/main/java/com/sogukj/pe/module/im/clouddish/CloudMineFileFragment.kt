package com.sogukj.pe.module.im.clouddish

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.BaseRefreshFragment
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.CloudFileBean
import com.sogukj.pe.bean.CloudLevel1
import com.sogukj.pe.bean.CloudLevel2
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.fragment_mine_file.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.startActivity
import java.io.File

/**
 * Created by CH-ZH on 2018/10/25.
 */
class CloudMineFileFragment : BaseRefreshFragment() {
    private var fileInfos = ArrayList<CloudFileBean>()
    private lateinit var adapter : RecyclerAdapter<CloudFileBean>
    private lateinit var qyAdapter: RecyclerAdapter<CloudFileBean>
    private lateinit var alreadySelected: MutableSet<CloudFileBean>
    private var busAdapter : CloudExpandableAdapter ? = null
    private var type = 1 //1 : 我的文件 2 : 企业文件
    private var invokeType = 1 // 1:加密云盘按钮跳转 2：保存到云盘
    private var fileCount = 0
    private var path = ""
    private var dir = ""
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
        invokeType = arguments!!.getInt("invokeType")
        path = arguments!!.getString("path")
        dir = arguments!!.getString("dir")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initData()
        bindListener()
    }

    private fun initData() {
        if (invokeType == 1){
            rl_submit.setVisible(true)
            ll_save.setVisible(false)
        }else{
            if (type == 1){
                rl_submit.setVisible(false)
                ll_save.setVisible(true)
            }else{
                rl_submit.setVisible(false)
                ll_save.setVisible(false)
            }
        }
        alreadySelected = ArrayList<CloudFileBean>().toMutableSet()
        Glide.with(ctx)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        recycler_view.layoutManager = LinearLayoutManager(activity)
        recycler_view.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        showLoadding()
        when(type){
            1 -> {
                if (invokeType == 1){
                    adapter = RecyclerAdapter(activity!!){_adapter,parent,_ ->
                        CloudFileHolder(_adapter.getView(R.layout.item_cloud_file,parent))
                    }
                }else{
                    adapter = RecyclerAdapter(activity!!){_adapter,parent,_ ->
                        SaveCloudFileHolder(_adapter.getView(R.layout.item_cloud_file,parent))
                    }
                }
                recycler_view.adapter = adapter
                getMineCloudFileData()
            }

            2 -> {
                setBusExpandableData()
                if (invokeType == 1){
                    qyAdapter = RecyclerAdapter(activity!!){_adapter,parent,_ ->
                        CloudFileHolder(_adapter.getView(R.layout.item_cloud_file,parent))
                    }
                }else{
                    qyAdapter = RecyclerAdapter(activity!!){_adapter,parent,_ ->
                        SaveCloudFileHolder(_adapter.getView(R.layout.item_cloud_file,parent))
                    }
                }
                recycler_view.adapter = qyAdapter
                getBusCloudFileData()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("TAG","  dir ==" + dir )
    }
    private fun getBusCloudFileData() {
        SoguApi.getService(activity!!.application,OtherService::class.java)
                .getMineCloudDishData(dir)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val datas = payload.payload
                            if (null != datas && datas.size > 0){
                                qyAdapter.dataList.clear()
                                qyAdapter.dataList.addAll(datas)
                                qyAdapter.notifyDataSetChanged()
                                goneEmpty()
                            }else{
                                showEmpty()
                            }
                        }else{
                            getCloudDishActivity().showErrorToast(payload.message)
                            showEmpty()
                        }
                        goneLoadding()
                    }

                    onError {
                        it.printStackTrace()
                        getCloudDishActivity().showErrorToast("获取数据失败")
                        goneLoadding()
                        showEmpty()
                    }
                }
    }

    private fun getMineCloudFileData() {
        SoguApi.getService(activity!!.application,OtherService::class.java)
                .getMineCloudDishData(dir)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val datas = payload.payload
                            if (null != datas && datas.size > 0){
                                adapter.dataList.clear()
                                adapter.dataList.addAll(datas)
                                adapter.notifyDataSetChanged()
                                goneEmpty()
                            }else{
                                showEmpty()
                            }
                        }else{
                            getCloudDishActivity().showErrorToast(payload.message)
                            showEmpty()
                        }
                        goneLoadding()
                    }

                    onError {
                        it.printStackTrace()
                        getCloudDishActivity().showErrorToast("获取数据失败")
                        goneLoadding()
                        showEmpty()
                    }
                }
    }

    private fun getCloudDishActivity():CloudDishActivity{
        return (activity as CloudDishActivity?)!!
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

        tv_newdir.clickWithTrigger {
            //新建文件夹
            NewDirActivity.invokeForResult(activity!!, NEW_DIR_REQUEST)
        }

        tv_current.clickWithTrigger {
            //保存到当前目录
            val file = File(path)
            val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file_name", file.name, RequestBody.create(MediaType.parse("*/*"), file))
                    .addFormDataPart("save_file_path",dir) //1 项目文件 2审批文件
            val body = builder.build()
            showProgress("正在上传")
            SoguApi.getService(getCloudDishActivity().application,OtherService::class.java)
                    .uploadImFileToCloud(body)
                    .execute {
                        onNext { payload ->
                            if (payload.isOk){

                            }
                            hideProgress()
                        }

                        onError {
                            it.printStackTrace()
                            hideProgress()
                        }
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
        ll_empty.setVisible(true)
    }

    fun goneEmpty(){
        ll_empty.visibility = View.INVISIBLE
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
            if (data.file_type.equals("Folder")){
                //文件夹
                iv_select.setVisible(false)
                file_icon.setImageResource(R.drawable.folder_zip)
            }else{
                iv_select.setVisible(true)
                file_icon.setImageResource(R.drawable.icon_pdf)
            }
            tv_summary.text = data.file_name
            tv_time.text = data.create_time

            itemView.clickWithTrigger {
                if (data.file_type.equals("Folder")){
                    startActivity<FileDirDetailActivity>(Extras.TITLE to data.file_name,Extras.TYPE to invokeType,
                                Extras.TYPE1 to path,Extras.TYPE2 to dir)
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

    inner class SaveCloudFileHolder(itemView: View) : RecyclerHolder<CloudFileBean>(itemView) {
        val iv_select = itemView.find<ImageView>(R.id.iv_select)
        val file_icon = itemView.find<ImageView>(R.id.file_icon)
        val tv_summary = itemView.find<TextView>(R.id.tv_summary)
        val tv_fileSize = itemView.find<TextView>(R.id.tv_fileSize)
        val tv_time = itemView.find<TextView>(R.id.tv_time)
        override fun setData(view: View, data: CloudFileBean, position: Int) {
            if (null == data)return
            iv_select.setVisible(false)
            if (data.file_type.equals("Folder")){
                //文件夹
                file_icon.setImageResource(R.drawable.folder_zip)
            }else{
                file_icon.setImageResource(R.drawable.icon_pdf)
            }
            tv_summary.text = data.file_name
            tv_time.text = data.create_time

            itemView.clickWithTrigger {
                if (data.file_type.equals("Folder")){
                    startActivity<FileDirDetailActivity>(Extras.TITLE to data.file_name,Extras.TYPE to invokeType,
                            Extras.TYPE1 to path,Extras.TYPE2 to dir)
                }
            }
        }

    }

    companion object {
        val TAG = CloudMineFileFragment::class.java.simpleName
        val NEW_DIR_REQUEST = 1008
        fun newInstance(type:Int,invokeType:Int,path:String,dir:String): CloudMineFileFragment {
            val fragment = CloudMineFileFragment()
            val bundle = Bundle()
            bundle.putInt("type",type)
            bundle.putInt("invokeType",invokeType)
            bundle.putString("path",path)
            bundle.putString("dir",dir)
            fragment.arguments = bundle
            return fragment
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            when(requestCode){
                NEW_DIR_REQUEST -> {
                    if (null != data){
                        val dirName = data.getStringExtra(Extras.DATA)
                        createDir(dirName)
                    }
                }
            }
        }
    }

    private fun createDir(dirName: String?) {
        SoguApi.getService(getCloudDishActivity().application,OtherService::class.java)
                .createNewDir(dir,dirName!!)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){

                        }else{

                        }
                    }

                    onError {
                        it.printStackTrace()
                        getCloudDishActivity().showErrorToast("创建文件夹失败")
                    }
                }
    }

}



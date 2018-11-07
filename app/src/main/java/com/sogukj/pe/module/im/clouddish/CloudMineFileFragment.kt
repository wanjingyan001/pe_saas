package com.sogukj.pe.module.im.clouddish

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.netease.nim.uikit.common.util.file.FileUtil
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.BaseRefreshFragment
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.DownloadUtil
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.BatchRemoveBean
import com.sogukj.pe.bean.CloudFileBean
import com.sogukj.pe.bean.CloudLevel1
import com.sogukj.pe.bean.CloudLevel2
import com.sogukj.pe.module.im.clouddish.FileDirDetailActivity.Companion.FILEDIR_ACTION
import com.sogukj.pe.peUtils.FileTypeUtils
import com.sogukj.pe.peUtils.Store
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.fragment_mine_file.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.startActivity
import java.io.File

/**
 * Created by CH-ZH on 2018/10/25.
 */
class CloudMineFileFragment : BaseRefreshFragment() {
    private var fileInfos = ArrayList<CloudFileBean>()
    private lateinit var adapter: RecyclerAdapter<CloudFileBean>
    private lateinit var qyAdapter: RecyclerAdapter<CloudFileBean>
    private lateinit var alreadySelected: MutableSet<CloudFileBean>
    private var filePaths = ArrayList<String>()
    private var busAdapter: CloudExpandableAdapter? = null
    private var type = 1 //1 : 我的文件 2 : 企业文件
    private var invokeType = 1 // 1:IM云盘按钮跳转 2：保存到云盘
    private var fileCount = 0
    private var path = ""
    private var isSave = true //是否是保存
    private var dir = ""
    private var isBusi = true //是否是一级企业目录
    private var previousPath = "" //文件复制(移动)前的目录
    private var fileName = "" //要复制(移动)的文件名
    private var isCopy = false //是否是复制
    private var isRemove = false
    private var batchPath : BatchRemoveBean ? = null
    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = false
        config.autoLoadMoreEnable = false
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
        isSave = arguments!!.getBoolean("isSave", true)
        isBusi = arguments!!.getBoolean("isBusi",true)
        isCopy = arguments!!.getBoolean("isCopy",false)
        isRemove = arguments!!.getBoolean("isRemove",false)
        fileName = arguments!!.getString("fileName")
        previousPath = arguments!!.getString("previousPath")
        batchPath = arguments!!.getSerializable("batchPath") as BatchRemoveBean?
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initData()
        bindListener()
    }

    private fun initData() {
        if (isSave) {
            tv_current.text = "保存到当前目录"
        } else {
            tv_current.text = "移动到当前目录"
        }
        if (invokeType == 1) {
            rl_submit.setVisible(true)
            ll_save.setVisible(false)
        } else {
            if (type == 1) {
                rl_submit.setVisible(false)
                ll_save.setVisible(true)
            } else {
                rl_submit.setVisible(false)
                if (isBusi){
                    ll_save.setVisible(false)
                }else{
                    ll_save.setVisible(true)
                }

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
        when (type) {
            1 -> {
                if (invokeType == 1) {
                    adapter = RecyclerAdapter(activity!!) { _adapter, parent, _ ->
                        CloudFileHolder(_adapter.getView(R.layout.item_cloud_file, parent))
                    }
                } else {
                    adapter = RecyclerAdapter(activity!!) { _adapter, parent, _ ->
                        SaveCloudFileHolder(_adapter.getView(R.layout.item_cloud_file, parent))
                    }
                }
                recycler_view.adapter = adapter
                getMineCloudFileData()
            }

            2 -> {
//                setBusExpandableData()
                if (invokeType == 1) {
                    qyAdapter = RecyclerAdapter(activity!!) { _adapter, parent, _ ->
                        CloudFileHolder(_adapter.getView(R.layout.item_cloud_file, parent))
                    }
                } else {
                    qyAdapter = RecyclerAdapter(activity!!) { _adapter, parent, _ ->
                        SaveCloudFileHolder(_adapter.getView(R.layout.item_cloud_file, parent))
                    }
                }
                recycler_view.adapter = qyAdapter
                getBusCloudFileData()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("TAG", "  dir ==" + dir)
    }

    private fun getBusCloudFileData() {
        SoguApi.getStaticHttp(activity!!.application)
                .getMineCloudDishData(dir, Store.store.getUser(activity!!)!!.phone)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            val datas = payload.payload
                            if (null != datas && datas.size > 0) {
                                qyAdapter.dataList.clear()
                                qyAdapter.dataList.addAll(datas)
                                qyAdapter.notifyDataSetChanged()
                                goneEmpty()
                            } else {
                                showEmpty()
                            }
                        } else {
                            getBaseActivity().showErrorToast(payload.message)
                            showEmpty()
                        }
                        dofinishRefresh()
                    }

                    onError {
                        it.printStackTrace()
                        getBaseActivity().showErrorToast("获取数据失败")
                        dofinishRefresh()
                        showEmpty()
                    }
                }
    }

    private fun getMineCloudFileData() {
        SoguApi.getStaticHttp(activity!!.application)
                .getMineCloudDishData(dir, Store.store.getUser(activity!!)!!.phone)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            val datas = payload.payload
                            if (null != datas && datas.size > 0) {
                                adapter.dataList.clear()
                                adapter.dataList.addAll(datas)
                                adapter.notifyDataSetChanged()
                                goneEmpty()
                            } else {
                                showEmpty()
                            }
                        } else {
                            getBaseActivity().showErrorToast(payload.message)
                            showEmpty()
                        }
                        dofinishRefresh()
                    }

                    onError {
                        it.printStackTrace()
                        getBaseActivity().showErrorToast("获取数据失败")
                        dofinishRefresh()
                        showEmpty()
                    }
                }
    }

    private fun getBaseActivity(): ToolbarActivity {
        return (activity as ToolbarActivity?)!!
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
        busAdapter = CloudExpandableAdapter(res, activity!!)
        recycler_view.adapter = busAdapter
        busAdapter!!.expandAll()
    }

    private fun bindListener() {
        tv_comit.clickWithTrigger {
            //发送文件
            if (fileCount > 0) {
                alreadySelected.forEach {
                    downloadCloudFile(it)
                }
            } else {
                showCommonToast("请至少选择一个文件")
            }
        }

        tv_newdir.clickWithTrigger {
            //新建文件夹
            NewDirActivity.invokeForResult(this, dir, NEW_DIR_REQUEST, 0, "")
        }

        tv_current.clickWithTrigger {
            if (isSave) {
                if (isCopy){
                    //复制到当前目录
                    showProgress("正在保存")
                    SoguApi.getStaticHttp(getBaseActivity().application)
                            .copyCloudFile(previousPath+"/",dir+"/",Store.store.getUser(activity!!)!!.phone,fileName)
                            .execute {
                                onNext { payload ->
                                    if (payload.isOk){
                                        showSuccessToast("保存成功")
                                        setFinishBrocast()
                                        cloudDishFinish()
                                    }else{
                                        showErrorToast(payload.message)
                                    }
                                    hideProgress()
                                }
                                onError {
                                    hideProgress()
                                    showErrorToast("保存失败")
                                }
                            }
                }else{
                    //保存到当前目录
                    val file = File(path)
                    val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("file_name", file.name, RequestBody.create(MediaType.parse("*/*"), file))
                            .addFormDataPart("save_file_path", dir + "/") //1 项目文件 2审批文件
                            .addFormDataPart("phone", Store.store.getUser(activity!!)!!.phone)
                    val body = builder.build()
                    showProgress("正在上传")
                    SoguApi.getStaticHttp(getBaseActivity().application)
                            .uploadImFileToCloud(body)
                            .execute {
                                onNext { payload ->
                                    if (payload.isOk) {
                                        showSuccessToast("上传文件成功")
                                        setFinishBrocast()
                                        cloudDishFinish()
                                    } else {
                                        showErrorToast(payload.message)
                                    }
                                    hideProgress()
                                }

                                onError {
                                    it.printStackTrace()
                                    hideProgress()
                                    showErrorToast("上传文件失败")
                                }
                            }
                }
            } else {
                //批量移动
                showProgress("正在移动")
                if (null != batchPath && null != batchPath!!.path && null != batchPath!!.names && batchPath!!.names!!.size > 0){
                    //批量移动
                    val path = batchPath!!.path
                    val names = batchPath!!.names
                    var filePath = ""
                    var realFilePath = ""
                    names?.forEach {
                        val path1 = path + "/${it}"
                        val path2 = dir + "/${it}"
                        val endPath = path1 + ":::" + path2
                        filePath += endPath+";?"
                    }
                    realFilePath = FileUtil.getDeleteFilePath(filePath)
                    SoguApi.getStaticHttp(getBaseActivity().application)
                            .removeBatchCloudFile(realFilePath,Store.store.getUser(activity!!)!!.phone)
                            .execute {
                                onNext {payload ->
                                    if (payload.isOk){
                                        showSuccessToast("移动成功")
                                        setRemoveSuccessBrocast()
                                        setFinishBrocast()
                                    }else{
                                        showErrorToast(payload.message)
                                    }
                                    hideProgress()
                                }

                                onError {
                                    it.printStackTrace()
                                    showErrorToast("移动失败")
                                    hideProgress()
                                }
                            }
                }else{
                    val filePath = previousPath + "/${fileName}"
                    val new_file_path = dir + "/${fileName}"
                    SoguApi.getStaticHttp(getBaseActivity().application)
                            .cloudFileRemoveOrRename(filePath,new_file_path,Store.store.getUser(activity!!)!!.phone)
                            .execute {
                                onNext { payload ->
                                    if (payload.isOk){
                                        showSuccessToast("移动成功")
                                        setRemoveSuccessBrocast()
                                        setFinishBrocast()
                                    }else{
                                        showErrorToast(payload.message)
                                    }
                                    hideProgress()
                                }

                                onError {
                                    it.printStackTrace()
                                    showErrorToast("移动失败")
                                    hideProgress()
                                }
                            }
                }
            }
        }
    }

    private fun setRemoveSuccessBrocast() {
        val intent = Intent()
        intent.setAction(MineFileActivity.ACTION_STATE)
        LocalBroadcastManager.getInstance(activity!!).sendBroadcast(intent)
    }

    private fun setFinishBrocast(){
        val intent = Intent()
        intent.setAction(FILEDIR_ACTION)
        LocalBroadcastManager.getInstance(activity!!).sendBroadcast(intent)
    }

    private fun cloudDishFinish(){
        val intent = Intent()
        intent.setAction(CloudDishActivity.CLOUDDISH_ACTION)
        LocalBroadcastManager.getInstance(activity!!).sendBroadcast(intent)
    }
    private fun downloadCloudFile(bean: CloudFileBean) {
        DownloadUtil.getInstance().download(bean.preview_url.substring(0, bean.preview_url.indexOf("?")),
                activity!!.externalCacheDir.toString(),
                bean.file_name,
                object : DownloadUtil.OnDownloadListener {
                    override fun onDownloadSuccess(path: String) {
                        Log.e("TAG", "onDownloadSuccess --  path ==" + path)
                        filePaths.add(path)
                        if (filePaths.size == fileCount) {
                            sendFilePathToIm(filePaths)
                        }
                    }

                    override fun onDownloading(progress: Int) {
                        Log.e("TAG", "onDownloading -- progress ==" + progress)
                    }

                    override fun onDownloadFailed() {
                        Log.e("TAG", "onDownloadFailed --")
                    }

                })
    }

    private fun sendFilePathToIm(filePaths: ArrayList<String>) {
        val intent = Intent()
        intent.putStringArrayListExtra(Extras.DATA, filePaths)
        activity!!.setResult(Activity.RESULT_OK, intent)
        activity!!.finish()
    }

    override fun doRefresh() {
        when (type) {
            1 -> {
                getMineCloudFileData()
            }

            2 -> {
                getBusCloudFileData()
            }
        }
    }

    override fun doLoadMore() {

    }

    fun showEmpty() {
        ll_empty.setVisible(true)
        recycler_view.visibility = View.INVISIBLE
    }

    fun goneEmpty() {
        ll_empty.visibility = View.INVISIBLE
        recycler_view.visibility = View.VISIBLE
    }

    fun showLoadding() {
        view_recover.visibility = View.VISIBLE
    }

    fun goneLoadding() {
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
            if (null == data) return
            iv_select.isSelected = data.isSelect
            if (data.file_type.equals("Folder")) {
                //文件夹
                iv_select.setVisible(false)
                file_icon.setImageResource(R.drawable.folder_zip)
            } else {
                iv_select.setVisible(true)
                file_icon.imageResource = FileTypeUtils.getFileType(data.file_name).icon
            }
            tv_summary.text = data.file_name
            tv_time.text = data.create_time

            itemView.clickWithTrigger {
                if (data.file_type.equals("Folder")) {
                    startActivity<FileDirDetailActivity>(Extras.TITLE to data.file_name, Extras.TYPE to invokeType,
                            Extras.TYPE1 to path, Extras.TYPE2 to dir, "isSave" to isSave,"isCopy" to isCopy,"isRemove" to isRemove,
                            "fileName" to fileName,"previousPath" to previousPath,"batchPath" to batchPath!!)
                } else {
                    if (alreadySelected.contains(data)) {
                        alreadySelected.remove(data)
                        fileCount--
                        if (fileCount <= 0) {
                            fileCount = 0
                        }
                    } else {
                        alreadySelected.add(data)
                        fileCount++
                    }

                    data.isSelect = !data.isSelect
                    iv_select.isSelected = data.isSelect
                    tv_total.text = "${fileCount}个"
                    if (fileCount > 0) {
                        tv_all_title.setTextColor(resources.getColor(R.color.blue_17))
                        tv_total.setTextColor(resources.getColor(R.color.blue_17))
                        tv_comit.setBackgroundResource(R.drawable.selector_sure)
                    } else {
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
            if (null == data) return
            iv_select.setVisible(false)
            if (data.file_type.equals("Folder")) {
                //文件夹
                file_icon.setImageResource(R.drawable.folder_zip)
                tv_summary.setTextColor(resources.getColor(R.color.black_28))
                tv_time.setTextColor(resources.getColor(R.color.gray_a0))
            } else {
                file_icon.imageResource = FileTypeUtils.getFileType(data.file_name).icon
                tv_summary.setTextColor(resources.getColor(R.color.gray_d8))
                tv_time.setTextColor(resources.getColor(R.color.gray_d8))
            }
            tv_summary.text = data.file_name
            tv_time.text = data.create_time

            itemView.clickWithTrigger {
                if (data.file_type.equals("Folder")){
                    startActivity<FileDirDetailActivity>(Extras.TITLE to data.file_name, Extras.TYPE to invokeType,
                            Extras.TYPE1 to path, Extras.TYPE2 to dir, "isSave" to isSave,"isCopy" to isCopy,"isRemove" to isRemove,
                            "fileName" to fileName,"previousPath" to previousPath,"batchPath" to batchPath!!)
//                    if(isRemove){
//                        activity!!.finish()
//                    }
//                    if (invokeType == 2 && isSave){
//                        activity!!.finish()
//                    }
                }
            }
        }
    }

    companion object {
        val TAG = CloudMineFileFragment::class.java.simpleName
        val NEW_DIR_REQUEST = 1008

        fun newInstance(type: Int, invokeType: Int, path: String, dir: String, isSave: Boolean,
                        isBusi : Boolean,isCopy:Boolean,fileName:String,previousPath:String,batchPath: BatchRemoveBean,isRemove:Boolean): CloudMineFileFragment {
            val fragment = CloudMineFileFragment()
            val bundle = Bundle()
            bundle.putInt("type", type)
            bundle.putInt("invokeType", invokeType)
            bundle.putString("path", path)
            bundle.putString("dir", dir)
            bundle.putBoolean("isSave", isSave)
            bundle.putBoolean("isBusi",isBusi)
            bundle.putBoolean("isCopy",isCopy)
            bundle.putString("fileName",fileName)
            bundle.putString("previousPath",previousPath)
            bundle.putSerializable("batchPath",batchPath)
            bundle.putBoolean("isRemove",isRemove)
            fragment.arguments = bundle
            return fragment
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                NEW_DIR_REQUEST -> {
                    when (type) {
                        1 -> {
                            getMineCloudFileData()
                        }

                        2 -> {
                            getBusCloudFileData()
                        }
                    }
                }
            }
        }
    }
}



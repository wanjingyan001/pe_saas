package com.sogukj.pe.module.im.clouddish

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.netease.nim.uikit.common.util.file.FileUtil
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.CloudFileBean
import com.sogukj.pe.module.other.OnlinePreviewActivity
import com.sogukj.pe.peUtils.FileTypeUtils
import com.sogukj.pe.peUtils.Store
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_cloud_sort.*
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.startActivity

/**
 * Created by CH-ZH on 2018/10/31.
 * 分类
 */
class CloudFileSortActivity : BaseRefreshActivity(),UploadCallBack {
    private var dir : String = ""
    private var sort : String = ""
    private lateinit var adapter : RecyclerAdapter<CloudFileBean>
    private var page = 1
    private var type = ""
    private var filePath = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_sort)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        setTitle("分类")
        setBack(true)
        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        dir = intent.getStringExtra(Extras.TITLE)
        sort = intent.getStringExtra(Extras.SORT)
        type = intent.getStringExtra(Extras.TYPE)
        filePath = intent.getStringExtra(Extras.PATH)
        rv_express.layoutManager = LinearLayoutManager(this)
        rv_express.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun initData() {
        tv_dir.text = "${dir}中的所有${sort}"
        adapter = RecyclerAdapter(this){_adapter,parent,_ ->
            CloudFileSortHolder(_adapter.getView(R.layout.item_mine_file,parent))
        }
        rv_express.adapter = adapter
        showLoadding()
    }

    override fun onResume() {
        super.onResume()
        getCloudSortData(false)
    }

    private fun getCloudSortData(isLoadMore : Boolean) {
        if (isLoadMore){
            page++
        }else{
            page = 1
        }
        SoguApi.getStaticHttp(application)
                .getFileFillterData(page, Store.store.getUser(this)!!.phone,type,filePath)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val infos = payload.payload
                            if (null != infos && infos.size > 0){
                                if (isLoadMore){
                                    adapter.dataList.addAll(infos)
                                    adapter.notifyDataSetChanged()
                                }else{
                                    adapter.dataList.clear()
                                    adapter.dataList.addAll(infos)
                                    adapter.notifyDataSetChanged()
                                }
                                goneEmpty()
                            }else{
                                if (!isLoadMore){
                                   showEmpty()
                                }
                            }
                        }
                        if (isLoadMore){
                            dofinishLoadMore()
                        }else{
                            dofinishRefresh()
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                        if (!isLoadMore){
                            showEmpty()
                        }
                        if (isLoadMore){
                            dofinishLoadMore()
                        }else{
                            dofinishRefresh()
                        }
                    }
                }
    }

    inner class CloudFileSortHolder(itemView: View) : RecyclerHolder<CloudFileBean>(itemView) {
        val file_icon = itemView.find<ImageView>(R.id.file_icon)
        val tv_summary = itemView.find<TextView>(R.id.tv_summary)
        val tv_fileSize = itemView.find<TextView>(R.id.tv_fileSize)
        val tv_time = itemView.find<TextView>(R.id.tv_time)
        val iv_filter = itemView.find<ImageView>(R.id.iv_filter)
        val iv_select = itemView.find<ImageView>(R.id.iv_select)
        override fun setData(view: View, data: CloudFileBean, position: Int) {
            if (null == data) return
            tv_summary.text = data.file_name
            if (Utils.getTimeYmd(System.currentTimeMillis()).equals(Utils.getTimeYmd(Utils.getTime(data.create_time)))){
                tv_time.text = "今天 ${Utils.getTimeHm(Utils.getTime(data.create_time))}"
            }else{
                tv_time.text = data.create_time
            }
            if (data.file_type.equals("Folder")) {
                file_icon.setImageResource(R.drawable.folder_zip)
                tv_fileSize.setVisible(false)
            } else {
                file_icon.imageResource =  FileTypeUtils.getFileType(data.file_name).icon
                if (data.used_bytes.isNullOrEmpty()){
                    tv_fileSize.setVisible(false)
                }else{
                    tv_fileSize.setVisible(true)
                    tv_fileSize.text = FileUtil.formatFileSize(data.used_bytes.toLong(), FileUtil.SizeUnit.Auto)
                }
            }
            itemView.clickWithTrigger {
                if (data.file_type.equals("Folder")) {
                    //文件夾
                    startActivity<MineFileActivity>(Extras.TITLE to data.file_name,Extras.DIR to filePath+"/${data.file_name}")
                } else {
                    //预览
                    OnlinePreviewActivity.start(this@CloudFileSortActivity,data.preview_url,data.file_name,false)
                }
            }

            iv_filter.clickWithTrigger {
                if (data.file_type.equals("Folder")){
                    MineFileDialog.showFileItemDialog(this@CloudFileSortActivity,true,data,MODIFI_DIR,filePath,this@CloudFileSortActivity)
                }else{
                    MineFileDialog.showFileItemDialog(this@CloudFileSortActivity,false,data,MODIFI_FILE,filePath,this@CloudFileSortActivity)
                }
            }
        }

    }

    companion object {
        val MODIFI_DIR = 1001
        val MODIFI_FILE = 1002
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            when(requestCode){
                MODIFI_DIR,MODIFI_FILE -> {
                    getCloudSortData(false)
                }
            }
        }
    }

    private fun bindListener() {

    }

    private fun showLoadding(){
        view_recover.visibility = View.VISIBLE
    }

    private fun goneLoadding(){
        view_recover.visibility = View.INVISIBLE
    }

    private fun showEmpty(){
        fl_empty.visibility = View.VISIBLE
        rv_express.visibility = View.INVISIBLE
    }

    private fun goneEmpty(){
        fl_empty.visibility = View.INVISIBLE
        rv_express.visibility = View.VISIBLE
    }

    override fun uploadFile() {

    }

    override fun newDir() {

    }

    override fun deleteFile() {
        getCloudSortData(false)
    }

    override fun batchDeleteFile() {

    }

    override fun initRefreshConfig(): RefreshConfig?{
        val config = RefreshConfig()
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        config.scrollContentWhenLoaded = true
        config.disableContentWhenRefresh = true
        return config
    }

    override fun doRefresh() {
        getCloudSortData(false)
    }

    override fun doLoadMore() {
        getCloudSortData(true)
    }

    fun dofinishRefresh() {
        if (this::refresh.isLateinit && refresh.isRefreshing) {
            refresh.finishRefresh()
        }
        goneLoadding()
    }

    fun dofinishLoadMore() {
        if (this::refresh.isLateinit && refresh.isLoading) {
            refresh.finishLoadMore()
        }
        goneLoadding()
    }
}


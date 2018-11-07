package com.sogukj.pe.module.im.clouddish

import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.FileDynamicBean
import com.sogukj.pe.module.other.OnlinePreviewActivity
import com.sogukj.pe.peUtils.FileTypeUtils
import com.sogukj.pe.peUtils.FileUtil
import com.sogukj.pe.peUtils.Store
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_file_dynamic.*
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource

/**
 * Created by CH-ZH on 2018/10/26.
 * 文件动态
 */
class FileDynamicActivity : BaseRefreshActivity(){
    private lateinit var adapter: RecyclerAdapter<FileDynamicBean>
    private var infos = ArrayList<FileDynamicBean>()
    private var page = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_dynamic)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        title = "文件动态"
        setBack(true)
        rv_dynamic.layoutManager = LinearLayoutManager(this)
        rv_dynamic.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        infos = intent.getSerializableExtra(Extras.DATA) as ArrayList<FileDynamicBean>
    }

    private fun initData() {
        if (null != infos && infos.size > 0){
            goneEmpty()
        }else{
            showEmpty()
        }
        adapter = RecyclerAdapter(this){_adapter,parent,_ ->
            FileDynamicHolder(_adapter.getView(R.layout.item_file_dynamic,parent))
        }
        adapter.dataList.clear()
        adapter.dataList.addAll(infos)
        rv_dynamic.adapter = adapter
    }

    private fun getDynamicData(isLoadMore: Boolean) {
        if (isLoadMore){
            page++
        }else{
            page = 1
        }
        SoguApi.getStaticHttp(application)
                .getFileDynamicData(page, Store.store.getUser(this)!!.phone)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val data = payload.payload
                            if (null != data && data.size > 0){
                                goneEmpty()
                                if (isLoadMore){
                                    adapter.dataList.addAll(data)
                                    adapter.notifyDataSetChanged()
                                }else{
                                    adapter.dataList.clear()
                                    adapter.dataList = data as MutableList<FileDynamicBean>
                                    adapter.notifyDataSetChanged()
                                }
                            }else{
                                if (isLoadMore){
                                    goneEmpty()
                                }else{
                                    showEmpty()
                                }
                            }
                        }else{
                            showErrorToast(payload.message)
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
                        if (isLoadMore){
                            dofinishLoadMore()
                        }else{
                            dofinishRefresh()
                            showEmpty()
                        }
                        goneLoadding()
                    }
                }
    }

    private fun bindListener() {
        adapter.onItemClick = {v,p ->
            //预览
            val dataList = adapter.dataList
            if (null != dataList && dataList.size > 0){
                val dynamicBean = dataList[p]
                if (null != dynamicBean){
                    if (null != dynamicBean.size){
                        //预览
                        getFilePreviewPath(dynamicBean.fullpath,dynamicBean.filename)
                    }
                }
            }
        }
    }

    private fun getFilePreviewPath(filePath: String,fileName:String) {
        SoguApi.getStaticHttp(application)
                .getFilePreviewPath(filePath,Store.store.getUser(this)!!.phone)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val jsonObject = payload.payload
                            jsonObject?.let {
                                val previewUrl = it.get("preview_url").asString
                                previewUrl
                                OnlinePreviewActivity.start(this@FileDynamicActivity,previewUrl,fileName,false)
                            }
                        }else{
                            showErrorToast(payload.message)
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取预览路径失败")
                    }
                }
    }

    inner class FileDynamicHolder(itemView: View) : RecyclerHolder<FileDynamicBean>(itemView) {
        val iv_user = itemView.find<ImageView>(R.id.iv_user)
        val tv_title = itemView.find<TextView>(R.id.tv_title)
        val tv_time = itemView.find<TextView>(R.id.tv_time)
        val iv_image = itemView.find<ImageView>(R.id.iv_image)
        val ll_files = itemView.find<LinearLayout>(R.id.ll_files)
        val iv_file = itemView.find<ImageView>(R.id.iv_file)
        val tv_name = itemView.find<TextView>(R.id.tv_name)
        val tv_size = itemView.find<TextView>(R.id.tv_size)
        override fun setData(view: View, data: FileDynamicBean, position: Int) {
            if (null == data)return
            tv_time.text = data.add_time
            iv_file.imageResource = FileTypeUtils.getFileType(data.filename).icon
            tv_name.text = data.filename
            if (data.size.isNullOrEmpty()){
                ll_files.setVisible(false)
            }else{
                ll_files.setVisible(true)
                tv_size.text = FileUtil.formatFileSize(data.size.toLong(), FileUtil.SizeUnit.Auto)
            }
            val title = SpannableString( data.display_name + data.show)
            title.setSpan(StyleSpan(android.graphics.Typeface.BOLD),0,data.display_name.length,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            tv_title.text = title
        }

    }

    fun showLoadding(){
        view_recover.visibility = View.VISIBLE
    }

    fun goneLoadding(){
        view_recover.visibility = View.INVISIBLE
    }

    fun showEmpty(){
        fl_empty.visibility = View.VISIBLE
        rv_dynamic.visibility = View.INVISIBLE
    }

    fun goneEmpty(){
        fl_empty.visibility = View.INVISIBLE
        rv_dynamic.visibility = View.VISIBLE
    }
    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        config.scrollContentWhenLoaded = true
        config.disableContentWhenRefresh = true
        return config
    }

    override fun doRefresh() {
        getDynamicData(false)
    }

    override fun doLoadMore() {
        getDynamicData(true)
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

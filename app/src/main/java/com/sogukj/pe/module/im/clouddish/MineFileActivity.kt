package com.sogukj.pe.module.im.clouddish

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.huantansheng.easyphotos.EasyPhotos
import com.netease.nim.uikit.support.glide.GlideEngine
import com.sogukj.pe.BuildConfig
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.CloudFileBean
import com.sogukj.pe.module.fileSelector.FileMainActivity
import kotlinx.android.synthetic.main.activity_mine_file.*
import kotlinx.android.synthetic.main.normal_img_toolbar.*
import org.jetbrains.anko.find

/**
 * Created by CH-ZH on 2018/10/26.
 * 我的文件
 */
class MineFileActivity : BaseRefreshActivity(),UploadCallBack {
    private lateinit var nameAdapter : RecyclerAdapter<CloudFileBean>
    private var infos = ArrayList<CloudFileBean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mine_file)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        setBack(true)
        setTitle("我的文件")
        toolbar_title.visibility = View.INVISIBLE
        toolbar_menu.setVisible(true)
        rv_dynamic.layoutManager = LinearLayoutManager(this)
        rv_dynamic.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
    }

    private fun initData() {
        nameAdapter = RecyclerAdapter(this){_adapter,parent,_ ->
            NameFilterHolder(_adapter.getView(R.layout.item_mine_file,parent))
        }
        for (i in 0 .. 20){
            infos.add(CloudFileBean())
        }
        nameAdapter.dataList = infos
        rv_dynamic.adapter = nameAdapter
    }

    private fun bindListener() {
        toolbar_menu.clickWithTrigger {
            //筛选、全选
        }

        toolbar_search.clickWithTrigger {
            //搜索
        }

        rl_search.clickWithTrigger {
            //搜索
        }

        scroll_view.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            Log.e("TAG","scrollY  ==" + scrollY + "  scrollY dp ==" + Utils.px2dip(this,scrollY.toFloat()))
            val scroll = Utils.px2dip(this, scrollY.toFloat())
            if (scroll >= 35){
                toolbar_title.visibility = View.VISIBLE
                if (scroll >= 90){
                    toolbar_search.visibility = View.VISIBLE
                }else{
                    toolbar_search.visibility = View.GONE
                }
            }else{
                toolbar_title.visibility = View.INVISIBLE
                toolbar_search.visibility = View.GONE
            }
        }

        iv_upload.clickWithTrigger {
            //上传弹框
            MineFileDialog.showUploadFile(this,this)
        }
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

    }

    override fun doLoadMore() {

    }

    override fun uploadFile() {
        showUploadFileDialog()
    }

    override fun onResume() {
        super.onResume()
        hideProgress()
    }

    private fun showUploadFileDialog() {
        val dialog = Dialog(context, R.style.AppTheme_Dialog)
        dialog.setContentView(R.layout.dialog_upload)
        val lay = dialog.getWindow()!!.getAttributes()
        lay.height = WindowManager.LayoutParams.WRAP_CONTENT
        lay.width = WindowManager.LayoutParams.MATCH_PARENT
        lay.gravity = Gravity.CENTER
        dialog.getWindow()!!.setAttributes(lay)
        dialog.show()

        dialog.find<TextView>(R.id.tv_photo)
                .clickWithTrigger {
                    //图片
                    EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                            .setFileProviderAuthority(BuildConfig.FILEPROVIDER)
                            .setPuzzleMenu(false)
                            .start(Extras.REQUESTCODE)
                    if (dialog.isShowing){
                        dialog.dismiss()
                    }
                }
        dialog.find<TextView>(R.id.tv_file)
                .clickWithTrigger {
                    showProgress("正在读取内存文件")
                    FileMainActivity.start(this, 9, false, GET_LOCAL_FILE)
                    if (dialog.isShowing){
                        dialog.dismiss()
                    }
                }
    }

    override fun newDir() {
        //新建文件夹
        NewDirActivity.invokeForResult(this,NEW_DIR_REQUEST)
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

    fun showLoadding(){
        view_recover.visibility = View.VISIBLE
    }

    fun goneLoadding(){
        view_recover.visibility = View.INVISIBLE
    }

    fun showEmpty(){
        fl_empty.visibility = View.VISIBLE
    }

    fun goneEmpty(){
        fl_empty.visibility = View.INVISIBLE
    }

    inner class NameFilterHolder(itemView: View) : RecyclerHolder<CloudFileBean>(itemView) {
        val file_icon = itemView.find<ImageView>(R.id.file_icon)
        val tv_summary = itemView.find<TextView>(R.id.tv_summary)
        val tv_fileSize = itemView.find<TextView>(R.id.tv_fileSize)
        val tv_time = itemView.find<TextView>(R.id.tv_time)
        val iv_filter = itemView.find<ImageView>(R.id.iv_filter)
        override fun setData(view: View, data: CloudFileBean, position: Int) {

        }
    }

    companion object {
        val GET_LOCAL_FILE = 1001
        val NEW_DIR_REQUEST = 1002
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            when(requestCode){
                GET_LOCAL_FILE -> {

                }

                Extras.REQUESTCODE -> {

                }

                NEW_DIR_REQUEST -> {

                }
            }
        }
    }

}

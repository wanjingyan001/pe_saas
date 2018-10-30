package com.sogukj.pe.module.im.clouddish

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
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
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.bumptech.glide.Glide
import com.huantansheng.easyphotos.EasyPhotos
import com.netease.nim.uikit.support.glide.GlideEngine
import com.sogukj.pe.BuildConfig
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.CloudFileBean
import com.sogukj.pe.module.fileSelector.FileMainActivity
import com.sogukj.pe.module.im.ImSearchResultActivity
import kotlinx.android.synthetic.main.activity_mine_file.*
import kotlinx.android.synthetic.main.normal_img_toolbar.*
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity

/**
 * Created by CH-ZH on 2018/10/26.
 * 我的文件
 */
class MineFileActivity : BaseRefreshActivity(), UploadCallBack {
    private lateinit var nameAdapter: RecyclerAdapter<CloudFileBean>
    private var infos = ArrayList<CloudFileBean>()
    private var canSelectInfos = ArrayList<CloudFileBean>()
    private var title = ""
    private var isSelectStatus = false //是否是多选的状态
    private var scroll = 0
    private lateinit var alreadySelected: MutableSet<CloudFileBean>
    private var selectCount = 0
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
        title = intent.getStringExtra(Extras.TITLE)
        setBack(true)
        setTitle(title)
        tv_file_title.text = title
        toolbar_title.visibility = View.INVISIBLE
        toolbar_menu.setVisible(true)
        rv_dynamic.layoutManager = LinearLayoutManager(this)
        rv_dynamic.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        alreadySelected = ArrayList<CloudFileBean>().toMutableSet()
    }

    private fun initData() {
        nameAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            NameFilterHolder(_adapter.getView(R.layout.item_mine_file, parent))
        }
        for (i in 0..20) {
            if (i == 0){
                val bean = CloudFileBean()
                bean.file_type = "Folder"
                bean.file_name = "Chzh"
                bean.create_time = "2017/07/06 今天 13:09"
                infos.add(bean)
            }else{
                infos.add(CloudFileBean())
            }
        }
        nameAdapter.dataList = infos
        rv_dynamic.adapter = nameAdapter
    }

    private fun bindListener() {
        toolbar_menu.clickWithTrigger {
            //筛选、全选
            fl_cover.visibility = View.VISIBLE
            showFillterPup()
        }

        toolbar_search.clickWithTrigger {
            //搜索
            ImSearchResultActivity.invoke(this, 3)
        }

        rl_search.clickWithTrigger {
            //搜索
            ImSearchResultActivity.invoke(this, 3)
        }

        scroll_view.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            Log.e("TAG", "scrollY  ==" + scrollY + "  scrollY dp ==" + Utils.px2dip(this, scrollY.toFloat()))
            if (isSelectStatus)return@setOnScrollChangeListener
            scroll = Utils.px2dip(this, scrollY.toFloat())
            if (scroll >= 35) {
                toolbar_title.visibility = View.VISIBLE
                if (scroll >= 90) {
                    toolbar_search.visibility = View.VISIBLE
                } else {
                    toolbar_search.visibility = View.GONE
                }
            } else {
                toolbar_title.visibility = View.INVISIBLE
                toolbar_search.visibility = View.GONE
            }
        }

        iv_upload.clickWithTrigger {
            //上传弹框
            MineFileDialog.showUploadFile(this, this)
        }

        tv_select.clickWithTrigger {
            if ("全选".equals(tv_select.textStr)){
                tv_select.text = "全不选"
                selectCount = nameAdapter.dataList.size
                setTitle("已选择(${selectCount})")
                alreadySelected.clear()
                nameAdapter.dataList.forEach {
                    it.isSelect = true
                    alreadySelected.add(it)
                }
                nameAdapter.notifyDataSetChanged()
                tv_delete.setTextColor(resources.getColor(R.color.red_f01))
                tv_remove.setTextColor(resources.getColor(R.color.blue_17))
            }else{
                tv_select.text = "全选"
                selectCount = 0
                setTitle("已选择")
                alreadySelected.clear()
                nameAdapter.dataList.forEach {
                    it.isSelect = false
                }
                nameAdapter.notifyDataSetChanged()
                tv_delete.setTextColor(resources.getColor(R.color.gray_a0))
                tv_remove.setTextColor(resources.getColor(R.color.gray_a0))
            }
        }
        tv_delete.clickWithTrigger {
            //删除
            if (selectCount > 0){

            }
        }

        tv_remove.clickWithTrigger {
            //移动
            if (selectCount > 0){

            }
        }
    }

    private fun showFillterPup() {
        val contentView = View.inflate(this, R.layout.cloud_fillter_pup, null)
        val ll_select = contentView.findViewById<LinearLayout>(R.id.ll_select)
        val ll_fillter = contentView.findViewById<LinearLayout>(R.id.ll_fillter)
        val popupWindow = PopupWindow(contentView, Utils.dip2px(this, 140f), Utils.dip2px(this, 110f), true)
        popupWindow.isTouchable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(0x00000000))
        popupWindow.isOutsideTouchable = true
        popupWindow.setOnDismissListener {

            fl_cover.visibility = View.GONE
        }
        val showPos = IntArray(2)
        toolbar_menu.getLocationOnScreen(showPos)
//        popupWindow.showAtLocation(iv_fillter, Gravity.RIGHT,
//                Utils.dip2px(this, 10f), showPos[1] + Utils.dip2px(this, 20f))

        popupWindow.showAsDropDown(toolbar_menu, 0, 0)
        ll_select.clickWithTrigger {
            //多选
            fl_cover.visibility = View.GONE
            if (popupWindow.isShowing) {
                popupWindow.dismiss()
            }
           setMoreSelectStatus()
        }

        ll_fillter.clickWithTrigger {
            //筛选
            fl_cover.visibility = View.GONE
            if (popupWindow.isShowing) {
                popupWindow.dismiss()
            }
        }
    }

    override fun onBackPressed() {
        if (isSelectStatus){
            nameAdapter.dataList.forEach {
                it.canSelect = false
            }
            nameAdapter.notifyDataSetChanged()
            isSelectStatus = false
            iv_upload.setVisible(true)
            rl_select.setVisible(false)
            tv_select.setVisible(false)
            toolbar_menu.setVisible(true)
            setTitle(title)
            if (scroll >= 35) {
                toolbar_title.visibility = View.VISIBLE
                if (scroll >= 90) {
                    toolbar_search.visibility = View.VISIBLE
                } else {
                    toolbar_search.visibility = View.GONE
                }
            } else {
                toolbar_title.visibility = View.INVISIBLE
                toolbar_search.visibility = View.GONE
            }
        }else{
            super.onBackPressed()
        }
    }
    private fun setMoreSelectStatus() {
        isSelectStatus = true
        nameAdapter.dataList.forEach {
            it.canSelect = true
        }
        nameAdapter.notifyDataSetChanged()
        toolbar_menu.setVisible(false)
        toolbar_search.setVisible(false)
        tv_select.setVisible(true)
        tv_select.setText("全选")
        toolbar_title.visibility = View.VISIBLE
        setTitle("已选择")
        iv_upload.setVisible(false)
        rl_select.setVisible(true)
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
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                }
        dialog.find<TextView>(R.id.tv_file)
                .clickWithTrigger {
                    showProgress("正在读取内存文件")
                    FileMainActivity.start(this, 9, false, GET_LOCAL_FILE)
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                }
    }

    override fun newDir() {
        //新建文件夹
        NewDirActivity.invokeForResult(this, NEW_DIR_REQUEST)
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

    fun showLoadding() {
        view_recover.visibility = View.VISIBLE
    }

    fun goneLoadding() {
        view_recover.visibility = View.INVISIBLE
    }

    fun showEmpty() {
        fl_empty.visibility = View.VISIBLE
    }

    fun goneEmpty() {
        fl_empty.visibility = View.INVISIBLE
    }

    inner class NameFilterHolder(itemView: View) : RecyclerHolder<CloudFileBean>(itemView) {
        val file_icon = itemView.find<ImageView>(R.id.file_icon)
        val tv_summary = itemView.find<TextView>(R.id.tv_summary)
        val tv_fileSize = itemView.find<TextView>(R.id.tv_fileSize)
        val tv_time = itemView.find<TextView>(R.id.tv_time)
        val iv_filter = itemView.find<ImageView>(R.id.iv_filter)
        val iv_select = itemView.find<ImageView>(R.id.iv_select)
        override fun setData(view: View, data: CloudFileBean, position: Int) {
            if (data.canSelect) {
                iv_select.setVisible(true)
            } else {
                iv_select.setVisible(false)
            }
            iv_select.isSelected = data.isSelect
            if (data.file_type.equals("Folder")) {
                file_icon.setImageResource(R.drawable.folder_zip)
            } else {
                file_icon.setImageResource(R.drawable.icon_pdf)
            }
            itemView.clickWithTrigger {
                if (data.canSelect){
                    if (alreadySelected.contains(data)){
                        alreadySelected.remove(data)
                        selectCount --
                        if (selectCount <= 0){
                            selectCount = 0
                        }
                    }else{
                        alreadySelected.add(data)
                        selectCount++
                    }
                    data.isSelect = !data.isSelect
                    iv_select.isSelected = data.isSelect
                    if (selectCount <= 0){
                        setTitle("已选择")
                        tv_delete.setTextColor(resources.getColor(R.color.gray_a0))
                        tv_remove.setTextColor(resources.getColor(R.color.gray_a0))
                    }else{
                        setTitle("已选择(${selectCount})")
                        tv_delete.setTextColor(resources.getColor(R.color.red_f01))
                        tv_remove.setTextColor(resources.getColor(R.color.blue_17))
                    }
                }else{
                    if (data.file_type.equals("Folder")) {
                        //文件夾
                        startActivity<MineFileActivity>(Extras.TITLE to data.file_name)
                    } else {
                        //预览

                    }
                }
            }
        }
    }

    companion object {
        val GET_LOCAL_FILE = 1001
        val NEW_DIR_REQUEST = 1002
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
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

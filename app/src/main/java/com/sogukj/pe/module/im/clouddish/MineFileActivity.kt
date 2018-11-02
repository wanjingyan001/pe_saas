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
import android.widget.*
import com.bumptech.glide.Glide
import com.huantansheng.easyphotos.EasyPhotos
import com.netease.nim.uikit.support.glide.GlideEngine
import com.sogukj.pe.BuildConfig
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
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
import com.sogukj.pe.module.other.OnlinePreviewActivity
import com.sogukj.pe.peUtils.FileTypeUtils
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_mine_file.*
import kotlinx.android.synthetic.main.normal_img_toolbar.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.startActivity
import java.io.File
import java.text.Collator
import java.util.*

/**
 * Created by CH-ZH on 2018/10/26.
 * 我的文件
 */
class MineFileActivity : BaseRefreshActivity(), UploadCallBack {
    private lateinit var nameAdapter: RecyclerAdapter<CloudFileBean>
    private var folderInfos = ArrayList<CloudFileBean>() //文件夹相关
    private var fileInfos = ArrayList<CloudFileBean>() //文件相关
    private var allInfos = ArrayList<CloudFileBean>()
    private var primeInfos = ArrayList<CloudFileBean>()
    private var title = ""
    private var isSelectStatus = false //是否是多选的状态
    private var scroll = 0
    private lateinit var alreadySelected: MutableSet<CloudFileBean>
    private var selectCount = 0
    private var dir = ""
    private var isNameSort = true //默认是名称排序
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
        dir = intent.getStringExtra(Extras.DIR)
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
        rv_dynamic.adapter = nameAdapter
        showLoadding()
        getMineFileData()
    }

    private fun getMineFileData() {
        SoguApi.getService(application,OtherService::class.java)
                .getMineCloudDishData(dir, Store.store.getUser(this)!!.phone)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val infos = payload.payload
                            if (null != infos && infos.size > 0){
                                primeInfos = infos as ArrayList<CloudFileBean>
                                sortMineFileInfos(infos)
                                goneEmpty()
                            }else{
                                showEmpty()
                            }
                        }else{
                            showErrorToast(payload.message)
                            showEmpty()
                        }
                        dofinishRefresh()
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                        showEmpty()
                        dofinishRefresh()
                    }
                }
    }

    private fun sortMineFileInfos(infos: List<CloudFileBean>) {
        folderInfos.clear()
        fileInfos.clear()
        infos.forEach {
            if (it.file_type.equals("Folder")){
                folderInfos.add(it)
            }else{
                fileInfos.add(it)
            }
        }
        if (isNameSort){
            if (folderInfos.size > 0){
                Collections.sort(folderInfos,object : Comparator<CloudFileBean>{
                    override fun compare(info1: CloudFileBean?, info2: CloudFileBean?): Int {
                        val collator = Collator.getInstance(java.util.Locale.CHINA)
                        return collator.getCollationKey(info1!!.file_name).compareTo(collator.getCollationKey(info2!!.file_name))
                    }
                })
            }
            if (fileInfos.size > 0){
                Collections.sort(fileInfos,object : Comparator<CloudFileBean>{
                    override fun compare(info1: CloudFileBean?, info2: CloudFileBean?): Int {
                        val collator = Collator.getInstance(java.util.Locale.CHINA)
                        return collator.getCollationKey(info1!!.file_name).compareTo(collator.getCollationKey(info2!!.file_name))
                    }
                })
            }
        }else{
            if (folderInfos.size > 0){
                Collections.sort(folderInfos,object : Comparator<CloudFileBean>{
                    override fun compare(o1: CloudFileBean?, o2: CloudFileBean?): Int {
                        val time1 = Utils.getTime(o1!!.create_time)
                        val time2 = Utils.getTime(o2!!.create_time)
                        if (time1 > time2){
                            return -1
                        }else if (time1 == time2){
                            return 0
                        }else{
                            return 1
                        }
                    }
                })
            }

            if (fileInfos.size > 0){
                Collections.sort(fileInfos,object : Comparator<CloudFileBean>{
                    override fun compare(o1: CloudFileBean?, o2: CloudFileBean?): Int {
                        val time1 = Utils.getTime(o1!!.create_time)
                        val time2 = Utils.getTime(o2!!.create_time)
                        if (time1 > time2){
                            return -1
                        }else if (time1 == time2){
                            return 0
                        }else{
                            return 1
                        }
                    }
                })
            }
        }

        allInfos.clear()
        allInfos.addAll(folderInfos)
        allInfos.addAll(fileInfos)

        nameAdapter.dataList.clear()
        nameAdapter.dataList.addAll(allInfos)
        nameAdapter.notifyDataSetChanged()
    }

    private var popupWindow : PopupWindow ? = null
    private fun bindListener() {
        toolbar_menu.clickWithTrigger {
            //筛选、全选(无内容是隐藏全选)
            fl_cover.visibility = View.VISIBLE
            if (null != nameAdapter.dataList && nameAdapter.dataList.size > 0){
                showFillterPup()
            }else{
                showOnlyFitterPup()
            }
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
//            Log.e("TAG", "scrollY  ==" + scrollY + "  scrollY dp ==" + Utils.px2dip(this, scrollY.toFloat()))
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
                startActivity<FileDirDetailActivity>(Extras.TITLE to "",Extras.TYPE to 2,
                        Extras.TYPE1 to "",Extras.TYPE2 to dir,"isSave" to false)
            }
        }

        tv_sort.clickWithTrigger {
            if (null != nameAdapter.dataList && nameAdapter.dataList.size > 0){
                //排序
                if (null != popupWindow && popupWindow!!.isShowing) {
                    popupWindow!!.dismiss()
                    fl_sort_cover.visibility = View.GONE
                    tv_sort.setTextColor(resources.getColor(R.color.gray_a0))
                }else{
                    showSortPup()
                    fl_sort_cover.visibility = View.VISIBLE
                    tv_sort.setTextColor(resources.getColor(R.color.blue_3c))
                }
            }
        }
    }

    private fun showSortPup() {
        val contentView = View.inflate(this, R.layout.sort_pup, null)
        val rl_time = contentView.findViewById<RelativeLayout>(R.id.rl_time)
        val rl_name = contentView.find<RelativeLayout>(R.id.rl_name)
        val tv_time = contentView.find<TextView>(R.id.tv_time)
        val tv_name = contentView.find<TextView>(R.id.tv_name)
        if (isNameSort){
            tv_name.setTextColor(resources.getColor(R.color.blue_3c))
            tv_time.setTextColor(resources.getColor(R.color.black_28))
        }else{
            tv_name.setTextColor(resources.getColor(R.color.black_28))
            tv_time.setTextColor(resources.getColor(R.color.blue_3c))
        }
        popupWindow = PopupWindow(contentView,WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow!!.isTouchable = true
        popupWindow!!.setBackgroundDrawable(ColorDrawable(0x00000000))
        popupWindow!!.isOutsideTouchable = true
        popupWindow!!.setOnDismissListener {
            fl_sort_cover.visibility = View.GONE
            tv_sort.setTextColor(resources.getColor(R.color.gray_a0))
        }
        val showPos = IntArray(2)
        tv_sort.getLocationOnScreen(showPos)
        popupWindow!!.showAsDropDown(tv_sort, 0, 0)

        rl_time.clickWithTrigger {
            //时间排序
            fl_sort_cover.visibility = View.GONE
            if (popupWindow!!.isShowing) {
                popupWindow!!.dismiss()
            }
            tv_sort.setTextColor(resources.getColor(R.color.gray_a0))
            tv_sort.text = "时间排序"
            isNameSort = false

            if (null != primeInfos){
                sortMineFileInfos(primeInfos)
            }
        }

        rl_name.clickWithTrigger {
            //名称排序
            fl_sort_cover.visibility = View.GONE
            if (popupWindow!!.isShowing) {
                popupWindow!!.dismiss()
            }
            tv_sort.setTextColor(resources.getColor(R.color.gray_a0))
            tv_sort.text = "名称排序"
            isNameSort = true
            if (null != primeInfos){
                sortMineFileInfos(primeInfos)
            }
        }
    }

    private fun showOnlyFitterPup() {
        val contentView = View.inflate(this, R.layout.only_fillter_pup, null)
        val ll_fillter = contentView.findViewById<LinearLayout>(R.id.ll_fillter)
        val popupWindow = PopupWindow(contentView, Utils.dip2px(this, 125f), Utils.dip2px(this, 50f), true)
        popupWindow.isTouchable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(0x00000000))
        popupWindow.isOutsideTouchable = true
        popupWindow.setOnDismissListener {

            fl_cover.visibility = View.GONE
        }
        val showPos = IntArray(2)
        toolbar_menu.getLocationOnScreen(showPos)
        popupWindow.showAsDropDown(toolbar_menu, 0, 0)

        ll_fillter.clickWithTrigger {
            //筛选
            fl_cover.visibility = View.GONE
            if (popupWindow.isShowing) {
                popupWindow.dismiss()
            }
            MineFileDialog.showFillterDialog(this,title)
        }
    }

    private fun showFillterPup() {
        val contentView = View.inflate(this, R.layout.cloud_fillter_pup, null)
        val ll_select = contentView.findViewById<LinearLayout>(R.id.ll_select)
        val ll_fillter = contentView.findViewById<LinearLayout>(R.id.ll_fillter)

        val popupWindow = PopupWindow(contentView, Utils.dip2px(this, 125f), Utils.dip2px(this, 90f), true)
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
            MineFileDialog.showFillterDialog(this,title)
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
        config.loadMoreEnable = false
        config.autoLoadMoreEnable = false
        config.scrollContentWhenLoaded = true
        config.disableContentWhenRefresh = true
        return config
    }

    override fun doRefresh() {
        getMineFileData()
    }

    override fun doLoadMore() {

    }

    override fun uploadFile() {
        showUploadFileDialog()
    }

    override fun onResume() {
        super.onResume()
        Log.e("TAG","dir ==" + dir)
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
//                    showProgress("正在读取内存文件")
                    FileMainActivity.start(this, 1, false, GET_LOCAL_FILE)
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                }
    }

    override fun newDir() {
        //新建文件夹
        NewDirActivity.invokeForResult(this, dir,NEW_DIR_OPO,0,"")
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
        ll_empty.visibility = View.VISIBLE
        rv_dynamic.visibility = View.INVISIBLE
    }

    fun goneEmpty() {
        ll_empty.visibility = View.INVISIBLE
        rv_dynamic.visibility = View.VISIBLE
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
                iv_filter.setVisible(false)
            } else {
                iv_select.setVisible(false)
                iv_filter.setVisible(true)
            }
            iv_select.isSelected = data.isSelect
            tv_summary.text = data.file_name
            tv_time.text = data.create_time
            if (data.file_type.equals("Folder")) {
                file_icon.setImageResource(R.drawable.folder_zip)
            } else {
                file_icon.imageResource =  FileTypeUtils.getFileType(data.file_name).icon
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
                        startActivity<MineFileActivity>(Extras.TITLE to data.file_name,Extras.DIR to dir+"/${data.file_name}")
                    } else {
                        //预览
                        OnlinePreviewActivity.start(this@MineFileActivity,data.preview_url,data.file_name,false)
                    }
                }
            }

            iv_filter.clickWithTrigger {
                if (data.file_type.equals("Folder")){
                    MineFileDialog.showFileItemDialog(this@MineFileActivity,true,data,MODIFI_DIR)
                }else{
                    MineFileDialog.showFileItemDialog(this@MineFileActivity,false,data,MODIFI_FILE)
                }
            }
        }
    }

    companion object {
        val GET_LOCAL_FILE = 1002
        val NEW_DIR_OPO = 1003
        val MODIFI_DIR = 1004
        val MODIFI_FILE = 1005
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GET_LOCAL_FILE -> {
                    val paths = data?.getStringArrayListExtra(Extras.LIST)
                    if (null != paths && paths.size > 0){
                        val filePath = paths!![0]
                        if (filePath.isNullOrEmpty()) return
                        val file = File(filePath)
                        if (null == file) return
                        uploadFileToCloud(file)
                    }
                }

                Extras.REQUESTCODE -> {
                    if (null == data) return
                    val resultPaths = data.getStringArrayListExtra(EasyPhotos.RESULT_PATHS)
                    val filePath = resultPaths[0]
                    if (filePath.isNullOrEmpty()){
                        showCommonToast("文件路径不正确")
                    }else{
                        uploadFileToCloud(File(filePath))
                    }
                }

                NEW_DIR_OPO -> {
                    //新建文件夹
                    getMineFileData()
                }

                MODIFI_DIR,MODIFI_FILE -> {
                    getMineFileData()
                }
            }
        }
    }

    private fun uploadFileToCloud(file:File) {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file_name", file.name, RequestBody.create(MediaType.parse("*/*"), file))
                .addFormDataPart("save_file_path",dir+"/")
                .addFormDataPart("phone", Store.store.getUser(this)!!.phone)
        val body = builder.build()
        showProgress("正在上传")
        SoguApi.getService(application,OtherService::class.java)
                .uploadImFileToCloud(body)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            showSuccessToast("上传成功")
                            getMineFileData()
                        }else{
                            showErrorToast(payload.message)
                        }
                        hideProgress()
                    }

                    onError {
                        it.printStackTrace()
                        hideProgress()
                        showErrorToast("上传失败")
                    }
                }
    }

}

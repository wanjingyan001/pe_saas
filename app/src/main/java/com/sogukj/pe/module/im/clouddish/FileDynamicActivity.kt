package com.sogukj.pe.module.im.clouddish

import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.FileDynamicBean
import kotlinx.android.synthetic.main.activity_file_dynamic.*
import org.jetbrains.anko.find

/**
 * Created by CH-ZH on 2018/10/26.
 * 文件动态
 */
class FileDynamicActivity : BaseRefreshActivity(){
    private lateinit var adapter: RecyclerAdapter<FileDynamicBean>
    private var infos = ArrayList<FileDynamicBean>()
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
    }

    private fun initData() {
        adapter = RecyclerAdapter(this){_adapter,parent,_ ->
            FileDynamicHolder(_adapter.getView(R.layout.item_file_dynamic,parent))
        }
        for (i in 0 .. 15){
            if (i == 0){
                val dynamicBean = FileDynamicBean()
                dynamicBean.img = "img"
                dynamicBean.user = "黄凯唯"
                dynamicBean.title = "在“宏大科技投资项目组”添加了1张图片"
                dynamicBean.time = "今天 14:34"
                infos.add(dynamicBean)
            }else if (i == 1){
                val dynamicBean = FileDynamicBean()
                val files = ArrayList<FileDynamicBean.FileOther>()
                dynamicBean.user = "张元"
                dynamicBean.title = "在“宏大科技投资项目组”添加了3个文件"
                dynamicBean.time = "今天 16:34"
                for (j in 0 .. 2){
                    val file = FileDynamicBean.FileOther()
                    file.fileName = "投资文件.pdf"
                    file.fileSize = "5.5M"
                    files.add(file)
                }
                dynamicBean.file = files
                infos.add(dynamicBean)
            }else{
                val dynamicBean = FileDynamicBean()
                val files = ArrayList<FileDynamicBean.FileOther>()
                dynamicBean.user = "张元"
                dynamicBean.title = "在“宏大科技投资项目组”添加了3个文件"
                dynamicBean.time = "今天 16:34"

                val file = FileDynamicBean.FileOther()
                file.fileName = "投资文件.pdf"
                file.fileSize = "2.1M"
                files.add(file)

                dynamicBean.file = files
                infos.add(dynamicBean)
            }
        }

        adapter.dataList = infos
        rv_dynamic.adapter = adapter
    }

    private fun bindListener() {

    }

    inner class FileDynamicHolder(itemView: View) : RecyclerHolder<FileDynamicBean>(itemView) {
        val iv_user = itemView.find<ImageView>(R.id.iv_user)
        val tv_user = itemView.find<TextView>(R.id.tv_user)
        val tv_title = itemView.find<TextView>(R.id.tv_title)
        val tv_time = itemView.find<TextView>(R.id.tv_time)
        val iv_image = itemView.find<ImageView>(R.id.iv_image)
        val ll_files = itemView.find<LinearLayout>(R.id.ll_files)
//        val iv_file = itemView.find<ImageView>(R.id.iv_file)
//        val tv_name = itemView.find<TextView>(R.id.tv_name)
//        val tv_size = itemView.find<TextView>(R.id.tv_size)
        override fun setData(view: View, data: FileDynamicBean, position: Int) {
            if (null == data)return
            tv_user.text = data.user
            tv_title.text = data.title
            tv_time.text = data.time
            if (!data.img.isNullOrEmpty()){
                iv_image.setVisible(true)
                ll_files.setVisible(false)
            }else{
                iv_image.setVisible(false)
                if (null != data.file && data.file!!.size > 0){
                    ll_files.setVisible(true)
                    ll_files.removeAllViews()
                    for (i in 0 .. data.file!!.size - 1){
                        val fileView = View.inflate(this@FileDynamicActivity,R.layout.file_item_load,null)
                        val iv_file  = fileView.find<ImageView>(R.id.iv_file)
                        val tv_name = fileView.find<TextView>(R.id.tv_name)
                        val tv_size = fileView.find<TextView>(R.id.tv_size)
                        val view_line = fileView.find<View>(R.id.view_line)
                        tv_name.text = data.file!![i].fileName
                        tv_size.text = data.file!![i].fileSize
                        if (data.file!!.size == 1){
                            view_line.setVisible(false)
                        }else{
                            if (i == data.file!!.size - 1){
                                view_line.setVisible(false)
                            }else{
                                view_line.setVisible(true)
                            }
                        }
                        ll_files.addView(fileView)
                    }
                }else{
                    ll_files.setVisible(false)
                }
            }
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
    }

    fun goneEmpty(){
        fl_empty.visibility = View.INVISIBLE
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

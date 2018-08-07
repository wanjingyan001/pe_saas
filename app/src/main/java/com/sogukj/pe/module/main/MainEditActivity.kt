package com.sogukj.pe.module.main

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.amap.api.mapcore.util.it
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.google.gson.Gson
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.database.*
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main_edit.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.*

@Route(path = ARouterPath.MainEditActivity)
class MainEditActivity : ToolbarActivity() {
    private lateinit var touchHelper: ItemTouchHelper
    private var mainModule = mutableSetOf<MainFunIcon>()
    private var allModule = mutableListOf<MainFunction>()
    private lateinit var model: FunctionViewModel
    private lateinit var allModuleAdapter: AllModuleAdapter
    private lateinit var mainModuleAdapter: MainModuleAdapter
    private lateinit var subscribe: Disposable

    private lateinit var oldData: List<MainFunIcon>

    companion object {
        private var isEdit = true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_edit)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        title = "首页按钮编辑"
        setBack(true)
        getData()

        mainModuleAdapter = MainModuleAdapter(mainModule)
        val callback = ItemDragAndSwipeCallback(mainModuleAdapter)
        touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(mainModuleList)
        callback.setSwipeMoveFlags(ItemTouchHelper.START or ItemTouchHelper.END)
        mainModuleAdapter.disableDragItem()
        mainModuleAdapter.setOnItemDragListener(object : OnItemDragListener {
            override fun onItemDragMoving(source: RecyclerView.ViewHolder, from: Int, target: RecyclerView.ViewHolder, to: Int) {
                info { "move from: " + source.adapterPosition + " to: " + target.adapterPosition + " fromIndex:" + from + " toIndex:" + to }
                info { "数据排序:from${mainModuleAdapter.data[from].jsonStr}===>to${mainModuleAdapter.data[to].jsonStr}" }
            }

            override fun onItemDragStart(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                info { "drag start" }
            }

            override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                info { "drag end" }
            }
        })
        mainModuleAdapter.enableDragItem(touchHelper)
        mainModuleList.apply {
            layoutManager = GridLayoutManager(ctx, 4)
            adapter = mainModuleAdapter
        }

        allModuleAdapter = AllModuleAdapter(allModule)
        allModuleList.apply {
            layoutManager = GridLayoutManager(ctx, 4)
            adapter = allModuleAdapter
        }



        mainModuleAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, _, position ->
            if (isEdit) {
                val funIcon = adapter.data[position] as MainFunIcon
                val find = allModule.find { it.t == funIcon }
                find?.let {
                    if (!it.t.editable) {
                        return@OnItemClickListener
                    }
                    it.t.isCurrent = false
                    model.updateFunction(it.t)
                    allModuleAdapter.notifyItemChanged(allModule.indexOf(it))
                }
            }
        }
        allModuleAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, _, position ->
            val function = allModule[position]
            if (!function.isHeader and isEdit) {
                val funIcon = function.t
                if (!funIcon.editable) {
                    return@OnItemClickListener
                }
                funIcon.isCurrent = !funIcon.isCurrent
                model.updateFunction(funIcon)
                allModuleAdapter.notifyItemChanged(position)
            }
        }

        toolbar_menu.clickWithTrigger {
            isEdit = !isEdit
            toolbar_menu.text = if (isEdit) "完成" else "编辑"
            if (isEdit) {
                mainModuleAdapter.enableDragItem(touchHelper)
            } else {
                mainModuleAdapter.disableDragItem()
                submitModules()
            }
            mainModuleAdapter.notifyDataSetChanged()
            allModuleAdapter.notifyDataSetChanged()
        }
    }


    private fun getData() = runBlocking {
        val factory = Injection.provideViewModelFactory(this@MainEditActivity)
        model = ViewModelProviders.of(this@MainEditActivity, factory).get(FunctionViewModel::class.java)
        model.getMainModules().observe(this@MainEditActivity, Observer<List<MainFunIcon>> { select ->
            select?.filter { it.name != "调整" }?.let {
                mainModuleAdapter.data.clear()
                mainModuleAdapter.data.addAll(it)
                mainModuleAdapter.notifyDataSetChanged()
            }
        })

        doAsync {
            oldData = model.getCurrentFunctions()
        }

        val flowable0 = model.getModuleFunctions(MainFunIcon.Project)
        val flowable = model.getModuleFunctions(MainFunIcon.Fund)
        val flowable1 = model.getModuleFunctions(MainFunIcon.Default)
        subscribe = flowable0.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap {
                    if (it.isNotEmpty() && allModule.find { it.header == "项目功能" } == null) {
                        allModule.add(MainFunction(true, "项目功能"))
                        it.forEach {
                            info { "项目功能" + it.jsonStr }
                            allModule.add(MainFunction(it))
                        }
                    }
                    return@flatMap flowable
                }.flatMap {
            if (it.isNotEmpty() && allModule.find { it.header == "基金功能" } == null) {
                allModule.add(MainFunction(true, "基金功能"))
                it.forEach {
                    info { "基金功能" + it.jsonStr }
                    allModule.add(MainFunction(it))
                }
            }
            return@flatMap flowable1
        }.subscribe {
            if (it.isNotEmpty() && allModule.find { it.header == "默认功能" } == null) {
                allModule.add(MainFunction(true, "默认功能"))
                it?.forEach {
                    info { "默认功能" + it.jsonStr }
                    allModule.add(MainFunction(it))
                }
                allModuleList.post {
                    allModuleAdapter.notifyDataSetChanged()
                }
            }
        }
    }


    /**
     * 以协程的形式获取的数据不保证顺序
     */
    private suspend fun initModule() {
        launch(CommonPool) {
            model.getAllData(MainFunIcon.Project).observe(this@MainEditActivity, Observer {
                if (allModule.find { it.header == "项目功能" } == null) {
                    allModule.add(MainFunction(true, "项目功能"))
                    it?.forEach {
                        allModule.add(MainFunction(it))
                    }
                    allModule.distinct()
                    allModuleAdapter.notifyDataSetChanged()
                }
            })
        }.join()
        launch(CommonPool) {
            model.getAllData(MainFunIcon.Fund).observe(this@MainEditActivity, Observer {
                if (allModule.find { it.header == "基金功能" } == null) {
                    allModule.add(MainFunction(true, "基金功能"))
                    it?.forEach {
                        allModule.add(MainFunction(it))
                    }
                    allModule.distinct()
                    allModuleAdapter.notifyDataSetChanged()
                }
            })
        }.join()
        launch(CommonPool) {
            model.getAllData(MainFunIcon.Default).observe(this@MainEditActivity, Observer {
                if (allModule.find { it.header == "默认功能" } == null) {
                    allModule.add(MainFunction(true, "默认功能"))
                    it?.forEach {
                        allModule.add(MainFunction(it))
                    }
                    allModule.distinct()
                    allModuleAdapter.notifyDataSetChanged()
                }
            })
        }.join()
    }


    override fun onBackPressed() {
        super.onBackPressed()
        if (oldData.jsonStr != mainModuleAdapter.data.jsonStr) {
            submitModules()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        subscribe.dispose()
    }

    private fun submitModules() {
        val list = ArrayList<FuncReqBean>()
        mainModuleAdapter.data.forEachIndexed { index, mainFunIcon ->
            mainFunIcon.seq = (index + 1).toLong()
            model.updateFunction(mainFunIcon)
            list.add(FuncReqBean(mainFunIcon.id, index + 1))
        }
        //修改调整的seq
        doAsync {
            val editBtn = model.findEditBtn()
            editBtn.seq = list.size + 1L
            model.updateFunction(editBtn)
        }
        info { list.jsonStr }
        SoguApi.getService(application, OtherService::class.java).homeModuleButton(HomeFunctionReq(2, list))
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {

                        } else {
                            showTopSnackBar(payload.message)
                        }
                    }
                    onError {
                        finish()
                    }
                    onComplete {
                        showSuccessToast("修改已提交")
                        finish()
                    }
                }
    }


    inner class AllModuleAdapter(data: List<MainFunction>) :
            BaseSectionQuickAdapter<MainFunction, BaseViewHolder>(R.layout.item_function_icon, R.layout.item_main_module_edt, data) {
        override fun convertHead(helper: BaseViewHolder, item: MainFunction) {
            val header = helper.getView<TextView>(R.id.letter)
            header.text = item.header
        }

        override fun convert(helper: BaseViewHolder, item: MainFunction) {
            val icon = helper.getView<ImageView>(R.id.funIcon)
            val aAndR = helper.getView<ImageView>(R.id.addAndRemove)
            val functionName = helper.getView<TextView>(R.id.functionName)
            item.t.apply {
                Glide.with(ctx)
                        .load(this.icon)
                        .into(icon)
                aAndR.setVisible(isEdit && this.editable)
                functionName.text = name
                if (isCurrent) {
                    aAndR.imageResource = R.mipmap.icon_remove_function
                } else {
                    aAndR.imageResource = R.mipmap.icon_add_function
                }
            }
        }
    }


    inner class MainModuleAdapter(data: Set<MainFunIcon>)
        : BaseItemDraggableAdapter<MainFunIcon, BaseViewHolder>(R.layout.item_function_icon, data.toMutableList()) {
        override fun convert(helper: BaseViewHolder, item: MainFunIcon) {
            val icon = helper.getView<ImageView>(R.id.funIcon)
            val aAndR = helper.getView<ImageView>(R.id.addAndRemove)
            val functionName = helper.getView<TextView>(R.id.functionName)
            item.apply {
                Glide.with(ctx)
                        .load(this.icon)
                        .into(icon)
                aAndR.setVisible(isEdit && this.editable)
                functionName.text = name
            }
        }
    }
}

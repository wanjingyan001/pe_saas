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
import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.database.FunctionViewModel
import com.sogukj.pe.database.Injection
import com.sogukj.pe.database.MainFunIcon
import com.sogukj.pe.database.MainFunction
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main_edit.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.ctx
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.info

@Route(path = "/main/edit")
class MainEditActivity : ToolbarActivity() {
    private lateinit var touchHelper: ItemTouchHelper
    private var mainModule = mutableSetOf<MainFunIcon>()
    private var allModule = mutableListOf<MainFunction>()
    private lateinit var model: FunctionViewModel
    private lateinit var allModuleAdapter: AllModuleAdapter
    private lateinit var mainModuleAdapter: MainModuleAdapter

    companion object {
        private var isEdit = false

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
            }

            override fun onItemDragStart(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                info { "drag start" }
            }

            override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                info { "drag end" }
            }
        })
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
            }
            mainModuleAdapter.notifyDataSetChanged()
            allModuleAdapter.notifyDataSetChanged()
        }
    }

    private fun getData() {
        val factory = Injection.provideViewModelFactory(this)
        model = ViewModelProviders.of(this, factory).get(FunctionViewModel::class.java)
        model.getMainModules().observe(this, Observer<List<MainFunIcon>> { select ->
            select?.let {
                val filter = it.filter { it.editable }
                AnkoLogger("WJY").info { "当前功能:${filter.jsonStr}" }
                mainModuleAdapter.data.clear()
                mainModuleAdapter.data.addAll(filter)
                mainModuleAdapter.notifyDataSetChanged()
            }
        })
        model.getModuleFunctions("/project")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (allModule.find { it.header == "项目功能" } == null) {
                        allModule.add(MainFunction(true, "项目功能"))
                        it.forEach {
                            allModule.add(MainFunction(it))
                        }
                        allModule.distinct()
                        AnkoLogger("WJY").info { "项目功能:${allModuleAdapter.data.jsonStr}" }
                        allModuleAdapter.notifyDataSetChanged()
                    }
                }, { e ->
                    e.printStackTrace()
                })
        model.getModuleFunctions("/fund")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (allModule.find { it.header == "基金功能" } == null){
                        allModule.add(MainFunction(true, "基金功能"))
                        it.forEach {
                            allModule.add(MainFunction(it))
                        }
                        allModule.distinct()
                        AnkoLogger("WJY").info { "基金功能:${allModuleAdapter.data.jsonStr}" }
                        allModuleAdapter.notifyDataSetChanged()
                    }
                }, { e ->
                    e.printStackTrace()
                })
    }

    override fun onDestroy() {
        super.onDestroy()
        isEdit = false
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
                icon.imageResource = item.t.icon
                aAndR.setVisible(isEdit)
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
                icon.imageResource = this.icon
                aAndR.setVisible(isEdit)
                functionName.text = name
            }
        }
    }
}

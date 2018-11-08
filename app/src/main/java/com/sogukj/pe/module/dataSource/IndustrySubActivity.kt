package com.sogukj.pe.module.dataSource

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.flexbox.FlexboxLayoutManager
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.HotPostInfo
import com.sogukj.pe.module.hotpost.HotPostActivity
import com.sogukj.pe.service.DataSourceService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_industry_sub.*
import kotlinx.android.synthetic.main.item_hot_industry_tag.view.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.startActivity

class IndustrySubActivity : ToolbarActivity() {
    private lateinit var tagAdapter: RecyclerAdapter<HotPostInfo>
    private val type: Int by extraDelegate(Extras.TYPE, -1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_industry_sub)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        title = "行业订阅"
        setBack(true)
        tagAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            IndustryTagHolder(_adapter.getView(R.layout.item_hot_industry_tag, parent))
        }
        hot_recycle.layoutManager = FlexboxLayoutManager(ctx)
        hot_recycle.adapter = tagAdapter
        tagAdapter.onItemClick = { v, position ->
            tagAdapter.selectedItems.contains(position).yes {
                if (tagAdapter.selectedItems.size > 1) {
                    tagAdapter.selectedItems.remove(position)
                } else {
                    showCommonToast("最少保留一个标签")
                }
            }.otherWise {
                tagAdapter.selectedItems.add(position)
            }
        }
        getAllTag()
        toolbar_menu.clickWithTrigger {
            submitTag()
        }
    }

    private fun getAllTag() {
        SoguApi.getService(application, DataSourceService::class.java)
                .getAllTag()
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                it.none { it.select == 1 }.yes {
                                    it.forEachIndexed { index, _ ->
                                        tagAdapter.selectedItems.add(index)
                                    }
                                }.otherWise {
                                    it.forEachIndexed { index, hotPostInfo ->
                                        if (hotPostInfo.select == 1) {
                                            tagAdapter.selectedItems.add(index)
                                        }
                                    }
                                }
                                tagAdapter.refreshData(it)
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }


    private fun submitTag() {
        SoguApi.getService(application, DataSourceService::class.java)
                .submitTags(tagAdapter.getSelectedBean().joinToString(",") { "${it.id}" })
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            showSuccessToast("提交成功")
                            when (type) {
                                -1-> {
                                    setResult(Extras.RESULTCODE)
                                }
                                else -> {
                                    startActivity<DocumentsListActivity>(
                                            Extras.TYPE to DocumentType.INDUSTRY_REPORTS)
                                }
                            }
                            finish()
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }

    }

    inner class IndustryTagHolder(itemView: View) : RecyclerHolder<HotPostInfo>(itemView) {
        override fun setData(view: View, data: HotPostInfo, position: Int) {
            view.isSelected = tagAdapter.selectedItems.contains(position)
            view.tagName.text = data.name
            view.selectedIcon.setVisible(tagAdapter.selectedItems.contains(position))
        }

    }
}

package com.sogukj.pe.database

import android.arch.lifecycle.*
import com.amap.api.mapcore.util.it
import com.sogukj.pe.R
import com.sogukj.pe.R.id.main
import com.sogukj.pe.baselibrary.Extended.jsonStr
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info

/**
 * Created by admin on 2018/6/21.
 */
class FunctionViewModel(private val funDao: FunctionDao) : ViewModel() {

    fun getMainModules(): LiveData<List<MainFunIcon>> {
        return funDao.getSelectFunctions(true)
    }


    fun getModuleFunctions(module: String): Flowable<List<MainFunIcon>> {
        return funDao.getModuleFunction(module)
    }

    fun updateFunction(function: MainFunIcon) {
        doAsync {
            funDao.updateFunction(function)
        }
    }


    fun generateData() {
        val functions = mutableListOf<MainFunIcon>()
        functions.add(MainFunIcon(R.drawable.ap, "安排", "/calendar", "/main", true, false, 1))
        functions.add(MainFunIcon(R.drawable.qb, "情报", "/news", "/main", true, false, 2))
        functions.add(MainFunIcon(R.drawable.sp, "审批", "/approve", "/main", true, false, 3))
        functions.add(MainFunIcon(R.drawable.zb, "周报", "/weekly", "/main", true, false, 4))
        functions.add(MainFunIcon(R.drawable.zx, "征信", "/credit", "/main", true, false, 5))
        functions.add(MainFunIcon(R.mipmap.icon_adjustment, "调整", "/main", "/edit", true, false, 99))
        functions.add(MainFunIcon(R.mipmap.icon_cbxx2, "储备信息", "/project", "/cbxx", false, true, 6))
        functions.add(MainFunIcon(R.mipmap.icon_gzjl, "跟踪记录", "/project", "/gzjl", false, true, 7))
        functions.add(MainFunIcon(R.mipmap.icon_jdsj, "尽调数据", "/project", "/jdsj", false, true, 8))
        functions.add(MainFunIcon(R.mipmap.icon_xmws, "项目文书", "/project", "/xmws", false, true, 9))
        functions.add(MainFunIcon(R.mipmap.icon_tjsj, "投决数据", "/project", "/tjsj", false, true, 10))
        functions.add(MainFunIcon(R.mipmap.icon_thgl, "投后管理", "/project", "/thgl", false, true, 11))
        functions.add(MainFunIcon(R.mipmap.icon_jjztz, "基金总台账", "/fund", "/jjztz", false, true, 12))
        functions.add(MainFunIcon(R.mipmap.icon_jjws, "基金文书", "/fund", "/jjws", false, true, 13))
        functions.add(MainFunIcon(R.mipmap.icon_jjxm, "基金项目", "/fund", "/jjxm", false, true, 14))
        doAsync {
            functions.forEach {
                funDao.insertFunction(it)
            }
        }
    }
}
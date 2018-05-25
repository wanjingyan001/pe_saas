package com.sogukj.pe.peUtils
import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.bean.UserBean
import java.util.*
import kotlin.collections.HashSet

/**
 * Created by qinff on 2016/5/20.
 * 小数据存取工具类
 */
class Store private constructor() {
    private var _user: UserBean? = null


    fun checkLogin(ctx: Context): Boolean {
        return null != getUser(ctx) && null != _user?.uid
    }

    fun getUToken(ctx: Context): String {
        return XmlDb.open(ctx).get("uToken", "")
    }

    fun setUToken(ctx: Context, token: String) {
        XmlDb.open(ctx).set("uToken", token)
    }

    private val resultNews = LinkedList<String>()
    fun newsSearch(ctx: Context): Collection<String> {
        this.resultNews.clear()
        try {
            val strJson = XmlDb.open(ctx).get("his.news", "")
            if (!TextUtils.isEmpty(strJson)) {
                val hisProjects = GSON.fromJson<Array<String>>(strJson, Array<String>::class.java)
                this.resultNews.addAll(Arrays.asList<String>(*hisProjects))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return resultNews
    }

    fun newsSearch(ctx: Context, resultProject: Collection<String>): Collection<String> {
        if (resultProject.size > 0)
            for (info in resultProject) {
                if (!this.resultNews.contains(info)) this.resultNews.addFirst(info)
            }
        while (this.resultNews.size > 20) {
            this.resultNews.removeLast()
        }
        XmlDb.open(ctx).set("his.news", GSON.toJson(this.resultNews.toArray()))
        return resultProject
    }

    fun newsSearchClear(ctx: Context): Collection<String> {
        XmlDb.open(ctx).set("his.news", "[]")
        return resultNews
    }

    fun newsSearchRemover(ctx: Context, position: Int){
        if (position<resultNews.size){
            resultNews.removeAt(position)
            XmlDb.open(ctx).set("his.news", GSON.toJson(this.resultNews.toArray()))
        }
    }

    private val resultProject = LinkedList<String>()
    fun projectSearch(ctx: Context): Collection<String> {
        this.resultProject.clear()
        try {
            val strJson = XmlDb.open(ctx).get("his.project", "")
            if (!TextUtils.isEmpty(strJson)) {
                val hisProjects = GSON.fromJson<Array<String>>(strJson, Array<String>::class.java)
                this.resultProject.addAll(Arrays.asList<String>(*hisProjects))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return resultProject
    }

    fun projectSearch(ctx: Context, resultProject: Collection<String>): Collection<String> {
        Log.d("WJY", "resultProject:${resultProject.size}")
        if (resultProject.isNotEmpty())
            for (info in resultProject) {
                if (!this.resultProject.contains(info)) this.resultProject.addFirst(info)
            }
        while (this.resultProject.size > 20) {
            this.resultProject.removeLast()
        }
        XmlDb.open(ctx).set("his.project", GSON.toJson(this.resultProject.toArray()))
        return resultProject
    }

    fun projectSearchClear(ctx: Context): Collection<String> {
        XmlDb.open(ctx).set("his.project", "[]")
        return resultProject
    }

    fun projectSearchRemover(ctx: Context, position: Int) {
        if (position < resultProject.size) {
            resultProject.removeAt(position)
            XmlDb.open(ctx).set("his.project", GSON.toJson(this.resultProject.toArray()))
        }
    }

    var readList = HashSet<String>()
    fun getRead(ctx: Context): HashSet<String> {
        try {
            val strJson = XmlDb.open(ctx).get("isRead", "")
            if (!TextUtils.isEmpty(strJson)) {
                this.readList.clear()
                val tmp = GSON.fromJson<Array<String>>(strJson, Array<String>::class.java)
                this.readList.addAll(Arrays.asList<String>(*tmp))
            }
        } catch (e: Exception) {
        }
        return readList
    }

    fun setRead(ctx: Context, readList: HashSet<String>): HashSet<String> {
        this.readList.clear()
        this.readList.addAll(readList)
        XmlDb.open(ctx).set("isRead", GSON.toJson(this.readList.toArray()))
        return readList
    }

    fun getUser(ctx: Context): UserBean? {
        try {
            if (null == _user) {
                val jsonUser = XmlDb.open(ctx).get(UserBean::class.java.simpleName, "")
                if (!TextUtils.isEmpty(jsonUser)) {
                    this._user = GSON.fromJson(jsonUser, UserBean::class.java)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return _user
    }

    fun setUser(ctx: Context, user: UserBean) {
        this._user = user
        XmlDb.open(ctx).set(UserBean::class.java.simpleName, GSON.toJson(user))
    }

    fun clearUser(ctx: Context) {
        this._user = null
        XmlDb.open(ctx).set(UserBean::class.java.simpleName, "{}")
    }

    /**
     * 获取基金的查询记录
     */
    private var fundSearchList = LinkedList<String>()

    fun getFundSearch(ctx: Context): Collection<String> {
        fundSearchList.clear()
        val strJson = XmlDb.open(ctx).get("fund.search", "")
        if (!strJson.isEmpty()) {
            val searchData = GSON.fromJson<Array<String>>(strJson, Array<String>::class.java)
            fundSearchList.addAll(searchData)
        }
        return fundSearchList
    }

    /**
     * 存储基金的查询记录
     */
    fun saveFundSearch(ctx: Context, resultProject: Collection<String>) {
        if (resultProject.isNotEmpty()) {
            for (info in resultProject) {
                if (!fundSearchList.contains(info)) {
                    fundSearchList.addFirst(info)
                }
            }
        }
        while (this.fundSearchList.size > 20) {
            this.fundSearchList.removeLast()
        }
        XmlDb.open(ctx).set("fund.search", GSON.toJson(this.fundSearchList.toArray()))
    }

    fun clearFundSearch(ctx: Context) {
        fundSearchList.clear()
        XmlDb.open(ctx).set("fund.search", GSON.toJson(this.fundSearchList.toArray()))
    }

    class SizeList<E> : LinkedList<E>() {

        fun distinct() {

        }
    }

    companion object {
        val store = Store()
        internal val GSON = Gson()
    }
}

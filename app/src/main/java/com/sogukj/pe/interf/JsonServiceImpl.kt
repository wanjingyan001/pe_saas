package com.sogukj.pe.interf

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.facade.service.SerializationService
import com.google.gson.Gson
import com.sogukj.pe.baselibrary.Extended.fromJson
import com.sogukj.pe.baselibrary.Extended.jsonStr
import java.io.Serializable
import java.lang.reflect.Type

/**
 * Created by admin on 2018/6/26.
 */
@Route(path = "/service/json")
class JsonServiceImpl : SerializationService {
    private lateinit var gson: Gson
    override fun <T : Any?> json2Object(input: String?, clazz: Class<T>?): T {
        return gson.fromJson(input, clazz)
    }

    override fun init(context: Context?) {
        gson = Gson()
    }

    override fun object2Json(instance: Any?): String {
        return instance.jsonStr
    }

    override fun <T : Any?> parseObject(input: String?, clazz: Type?): T {
        return gson.fromJson(input, clazz)
    }
}
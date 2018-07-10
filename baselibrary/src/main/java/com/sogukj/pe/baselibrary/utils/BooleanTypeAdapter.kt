package com.sogukj.pe.baselibrary.utils

import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

/**
 * Created by admin on 2018/7/9.
 */
class BooleanTypeAdapter : TypeAdapter<Boolean>() {
    override fun write(out: JsonWriter, value: Boolean?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    override fun read(inJson: JsonReader) : Boolean {
        val peek = inJson.peek()
        return when (peek) {
            JsonToken.BOOLEAN -> inJson.nextBoolean()
            JsonToken.NUMBER -> inJson.nextInt() != 0
            JsonToken.STRING -> inJson.nextString().toBoolean()
            else -> false
        }
    }
}
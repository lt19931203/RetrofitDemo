package com.test.demo

import com.squareup.moshi.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class MoshiApiResultConverterFactory : JsonAdapter.Factory {


    override fun create(
        type: Type,
        annotations: MutableSet<out Annotation>,
        moshi: Moshi
    ): JsonAdapter<*>? {
        val rawType = type.rawType
        if (rawType != ApiResult::class.java) return null

        // 获取 ApiResult 的泛型参数，比如 User
        val dataType: Type = (type as? ParameterizedType)
            ?.actualTypeArguments?.firstOrNull()
            ?: return null

        // 获取 User 的 JsonAdapter
        val dataTypeAdapter = moshi.nextAdapter<Any>(
            this, dataType, annotations
        )

        return ApiResultTypeAdapter(rawType, dataTypeAdapter)
    }

    class ApiResultTypeAdapter<T>(
        private val outerType: Type,
        private val dataTypeAdapter: JsonAdapter<T>
    ) : JsonAdapter<T>() {
        override fun fromJson(reader: JsonReader): T? {
            reader.beginObject()

            var code: Int? = null
            var msg: String? = null
            var data: Any? = null

            //待优化。。
            val nullableStringAdapter: JsonAdapter<String?> = Moshi.Builder().build().adapter(String::class.java,
                emptySet(), "message")

            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "code" -> code = reader.nextString().toIntOrNull()

                    //可空类型与非空类型
//                    "message" -> msg = reader.nextString()
                    "message" -> msg = nullableStringAdapter.fromJson(reader)
                    "data" -> data = dataTypeAdapter.fromJson(reader)
                    else -> reader.skipValue()
                }
            }

            reader.endObject()

            return if (code != 0)
                ApiResult.BizError(
                    code ?: -1,
                    msg ?: "N/A"
                ) as T
            else ApiResult.BizSuccess(
                code,
                msg ?: "N/A",
                data
            ) as T?
        }

        // 不需要序列化的逻辑
        override fun toJson(writer: JsonWriter, value: T?): Unit = TODO("Not yet implemented")
    }
}
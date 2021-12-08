package com.test.demo


import com.squareup.moshi.Types
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.ResponseBody
import okio.BufferedSource
import okio.Timeout
import retrofit2.*
import java.lang.IllegalStateException
import java.lang.StringBuilder
import java.lang.reflect.*
import java.util.*

class ApiResultCallAdapterFactory() :
    CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        //检查returnType是否是Call<T>类型的
        if (getRawType(returnType) != Call::class.java) return null
        check(returnType is ParameterizedType) { "$returnType must be parameterized. Raw types are not supported" }
        //取出Call<T> 里的T，检查是否是ApiResult<T>
        val apiResultType = getParameterUpperBound(0, returnType)
        // 如果不是 ApiResult 则不由本 CallAdapter.Factory 处理
        if (getRawType(apiResultType) != ApiResult::class.java) return null
        check(apiResultType is ParameterizedType) { "$apiResultType must be parameterized. Raw types are not supported" }

        //采用Gson自定义解析时需要dataType，如果是moshi自定义解析，采用apiResultType
//        val dataType = getParameterUpperBound(0, apiResultType)
        return ApiResultCallAdapter(apiResultType)
    }
}

class ApiResultCallAdapter(
    private val responseType: Type
) : CallAdapter<Any, Call<Any>> {
    override fun responseType(): Type = responseType

    override fun adapt(call: Call<Any>): Call<Any> {
        return ApiResultCall(call)
    }
}

class ApiResultCall(
    private val delegate: Call<Any>
) : Call<Any> {

    /**
     * KotlinExtensions#await()中会调用enqueue()方法
     */
    override fun enqueue(callback: Callback<Any>) {
        //delegate为OkHttpCall
        delegate.enqueue(object : Callback<Any> {
            //网络请求成功返回，会回调该方法（无论status code是不是200）
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                //无论请求响应成功还是失败都回调 Response.success
                if (response.isSuccessful) {
                    val body = response.body()
                    //经过MoshiApiResultConverterFactory和MoshiConverterFactory转换，response变成了ApiResult
//                    if (body is ApiResult.BizError) {
//                        errorHandler?.onBizError(body.code, body.message)
//                    }
                    if (body == null) {
                        callback.onResponse(this@ApiResultCall, Response.success(ApiResult.BizError(-1,"错误")))
                    } else {
                        if (body is ApiResult.BizSuccess<*>) {
                            if (body.errorCode == 0) {
                                callback.onResponse(this@ApiResultCall, Response.success(body))
                            }
                        }
                    }

                } else {
                    val throwable = HttpException(response)
                    callback.onResponse(
                        this@ApiResultCall,
                        Response.success(ApiResult.BizError(-1,"错误"))
                    )
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                t.printStackTrace()
                callback.onResponse(this@ApiResultCall, Response.success(ApiResult.OtherError(t)))
            }
        })
    }


    override fun clone(): Call<Any> = ApiResultCall(delegate.clone())

    override fun execute(): Response<Any> {
        throw UnsupportedOperationException("ApiResultCall does not support synchronous execution")
    }

    override fun isExecuted(): Boolean {
        return delegate.isExecuted
    }

    override fun cancel() {
        delegate.cancel()
    }

    override fun isCanceled(): Boolean {
        return delegate.isCanceled
    }

    override fun request(): Request {
        return delegate.request()
    }

    override fun timeout(): Timeout {
        return delegate.timeout()
    }


}
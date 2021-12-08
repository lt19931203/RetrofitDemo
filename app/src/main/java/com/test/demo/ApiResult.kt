package com.test.demo


sealed class ApiResult<T>() {
    data class BizSuccess<T>(val errorCode: Int, val errorMsg: String, val data: T) : ApiResult<T>()
    data class BizError(val errorCode: Int, val errorMsg: String) : ApiResult<Nothing>()
    data class OtherError(val throwable: Throwable) : ApiResult<Nothing>()
}
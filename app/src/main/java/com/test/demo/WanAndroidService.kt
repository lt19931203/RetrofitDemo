package com.test.demo

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface WanAndroidService {
    @GET("banner/json")
    suspend fun banner(): ApiResult<List<Banner>>

    @GET("banner/json")
    suspend fun banner2(): Banner2
}
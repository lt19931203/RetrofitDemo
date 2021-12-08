package com.test.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.squareup.moshi.Moshi
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btRequest = findViewById<Button>(R.id.bt_request)

        btRequest.setOnClickListener {

            val moshi = Moshi.Builder()
                    //对ApiResult的自定义解析
                .add(MoshiApiResultConverterFactory())
                .build()

            val interceptor = HttpLoggingInterceptor()
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()

            val contentType = "application/json".toMediaType()

            val retrofit = Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://www.wanandroid.com/")
                .addCallAdapterFactory(ApiResultCallAdapterFactory())
                //第一种：采用Gson自定义解析
//                .addConverterFactory(CustomGsonConverterFactory.create())
                //第二种：采用moshi自定义解析
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                //第三种：采用kotlinx-serialization自定义解析，有bug
//                .addConverterFactory(Json.asConverterFactory(contentType))
                .build()
            val service: WanAndroidService = retrofit.create(WanAndroidService::class.java)
            lifecycleScope.launch {

                val banner = service.banner()
                if (banner is ApiResult.BizSuccess) {
                    Log.i(TAG, "onCreate: " + banner.toString())
                } else {
                    Log.i(TAG, "onCreate: fail ")
                }
            }

        }
    }
}
/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.test.demo;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

final class CustomGsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final Gson gson;
    private final TypeAdapter<T> adapter;

    CustomGsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }

    /*
    更多逻辑请查看原版GsonResponseBodyConverter
     */
    @Override
    public T convert(ResponseBody value) throws IOException {
        JsonReader jsonReader = gson.newJsonReader(value.charStream());
        jsonReader.beginObject();
        int errorCode = -1;
        String errorMsg = "嘎嘎";
        T data = null;
        while (jsonReader.hasNext()) {
            String nextName = jsonReader.nextName();
            if (TextUtils.equals(nextName, "errorCode")) {
                errorCode = jsonReader.nextInt();
            } else if (TextUtils.equals(nextName, "errorMsg")) {
                errorMsg = jsonReader.nextString();
            } else if (TextUtils.equals(nextName, "data")) {
                data = adapter.read(jsonReader);
            }
        }
        jsonReader.endObject();
        return ((T) new ApiResult.BizSuccess(errorCode, errorMsg, data));
    }
}

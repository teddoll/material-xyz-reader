/*
 * Copyright 2015 Theodore Doll
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example.xyzreader.remote;


import android.content.Context;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Singleton for the app to use to get access to okhttpClient.
 */
public class HttpClientProvider {

    private final OkHttpClient httpClient;
    private static HttpClientProvider instance;

    public static synchronized HttpClientProvider getInstance(Context context) {
        if (instance == null) {
            instance = new HttpClientProvider(context.getApplicationContext());
        }
        return instance;
    }

    private HttpClientProvider(Context context) {
        httpClient = new OkHttpClient();
        File httpCacheDir = new File(context.getCacheDir(), "http_cache");
        long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
        httpClient.setCache(new Cache(httpCacheDir, httpCacheSize));
        httpClient.setReadTimeout(10, TimeUnit.SECONDS);
        httpClient.setConnectTimeout(5, TimeUnit.SECONDS);
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }
}

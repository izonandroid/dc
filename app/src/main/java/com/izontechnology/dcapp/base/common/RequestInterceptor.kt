package com.izontechnology.dcapp.base.common

import okhttp3.Interceptor
import okhttp3.Response

class RequestInterceptor constructor(private val pref: SharedPrefs) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = pref.getToken()
        val simHeader = pref.getSimHeader()
        val newRequest = chain.request().newBuilder().apply {
            if (token.isNotEmpty()) {
                addHeader("Authorization", token)
            }
            if (simHeader.isNotEmpty()) {
                addHeader("simheader", simHeader)
            }
        }.build()
        return chain.proceed(newRequest)
    }
}
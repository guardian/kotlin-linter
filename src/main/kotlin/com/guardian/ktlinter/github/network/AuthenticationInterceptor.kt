package com.guardian.ktlinter.github.network

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AuthenticationInterceptor(private val authToken: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()
        val builder: Request.Builder = original.newBuilder()
            .header("Authorization", authToken)
            .header("Accept", "application/vnd.github.v3.raw")
        val request: Request = builder.build()
        return chain.proceed(request)
    }
}
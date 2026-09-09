package br.com.fiap.aguiaexpress.data.remote.interceptor

import br.com.fiap.aguiaexpress.core.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = SessionManager.token

        println("URL REQUEST: ${originalRequest.url}")
        println("TOKEN NO INTERCEPTOR: $token")

        val newRequest = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        println("AUTH HEADER: ${newRequest.header("Authorization")}")

        return chain.proceed(newRequest)
    }
}
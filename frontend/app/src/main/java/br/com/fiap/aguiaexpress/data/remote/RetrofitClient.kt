package br.com.fiap.aguiaexpress.data.remote

import br.com.fiap.aguiaexpress.data.remote.api.AuthApi
import br.com.fiap.aguiaexpress.data.remote.api.BadgeApi
import br.com.fiap.aguiaexpress.data.remote.api.CategoriaApi
import br.com.fiap.aguiaexpress.data.remote.api.IdeiaApi
import br.com.fiap.aguiaexpress.data.remote.api.OrientacaoApi
import br.com.fiap.aguiaexpress.data.remote.api.ProjetoApi
import br.com.fiap.aguiaexpress.data.remote.api.UsuarioBadgeApi
import br.com.fiap.aguiaexpress.data.remote.api.VotoIdeiaApi
import br.com.fiap.aguiaexpress.data.remote.interceptor.AuthInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val badgeApi: BadgeApi by lazy {
        retrofit.create(BadgeApi::class.java)
    }

    val categoriaApi: CategoriaApi by lazy {
        retrofit.create(CategoriaApi::class.java)
    }

    val ideiaApi: IdeiaApi by lazy {
        retrofit.create(IdeiaApi::class.java)
    }

    val orientacaoApi: OrientacaoApi by lazy {
        retrofit.create(OrientacaoApi::class.java)
    }

    val projetoApi: ProjetoApi by lazy {
        retrofit.create(ProjetoApi::class.java)
    }

    val usuarioBadgeApi: UsuarioBadgeApi by lazy {
        retrofit.create(UsuarioBadgeApi::class.java)
    }

    val votoIdeiaApi: VotoIdeiaApi by lazy {
        retrofit.create(VotoIdeiaApi::class.java)
    }
}
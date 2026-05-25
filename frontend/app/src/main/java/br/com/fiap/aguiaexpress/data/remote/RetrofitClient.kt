package br.com.fiap.aguiaexpress.data.remote

import br.com.fiap.aguiaexpress.data.remote.api.AuthApi
import br.com.fiap.aguiaexpress.data.remote.api.BadgeApi
import br.com.fiap.aguiaexpress.data.remote.api.CategoriaApi
import br.com.fiap.aguiaexpress.data.remote.api.IdeiaApi
import br.com.fiap.aguiaexpress.data.remote.api.OrientacaoApi
import br.com.fiap.aguiaexpress.data.remote.api.ProjetoApi
import br.com.fiap.aguiaexpress.data.remote.api.UsuarioBadgeApi
import br.com.fiap.aguiaexpress.data.remote.api.VotoIdeiaApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val categoriaApi: CategoriaApi = retrofit.create(CategoriaApi::class.java)
    val orientacaoApi: OrientacaoApi = retrofit.create(OrientacaoApi::class.java)
    val ideiaApi: IdeiaApi = retrofit.create(IdeiaApi::class.java)
    val votoIdeiaApi: VotoIdeiaApi = retrofit.create(VotoIdeiaApi::class.java)

    val badgeApi: BadgeApi = retrofit.create(BadgeApi::class.java)

    val usuarioBadgeApi: UsuarioBadgeApi = retrofit.create(UsuarioBadgeApi::class.java)

    val projetoApi: ProjetoApi = retrofit.create(ProjetoApi::class.java)

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }
}
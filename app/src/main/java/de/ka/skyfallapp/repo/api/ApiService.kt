package de.ka.skyfallapp.repo.api

import android.app.Application
import de.ka.skyfallapp.R
import de.ka.skyfallapp.repo.ProfileManagerImpl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.standalone.KoinComponent
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * A service for making api calls.
 */
class ApiService(val app: Application, val profileManager: ProfileManagerImpl) : KoinComponent {

    //
    // Setup
    //

    private val api: Api by lazy {
        buildApi()
    }

    private fun buildApi(): Api {
        val retrofit = Retrofit.Builder()
            .client(buildOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .baseUrl(app.getString(R.string.api_url))
            .build()

        return retrofit.create(Api::class.java)
    }

    private fun buildOkHttpClient() = OkHttpClient
        .Builder()
        .addInterceptor(HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { Timber.e(it) }).apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor(tokenInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val tokenInterceptor = Interceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder()

        profileManager.currentProfile?.token?.let {
            request.addHeader("Authorization", "Bearer $it")
        }

        request.method(original.method(), original.body())
        chain.proceed(request.build())
    }

    //
    // Actual api calls
    //

    /**
     * Retrieves all consensus.
     */
    fun getConsensus() = api.getConsensus()

    /**
     * Retrieves the details of a consensus.
     */
    fun getConsensusDetail(id: String) = api.getConsensusDetails(id)

    /**
     * Sends a consensus.
     */
    fun postConsensus(consensus: Consensus) = api.postConsensus(consensus)

    /**
     * Sends a login or register request.
     */
    fun loginRegister(loginRegister: LoginRegister) = api.loginRegister(loginRegister)
}
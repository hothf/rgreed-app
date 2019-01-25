package de.ka.skyfallapp.repo.api

import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Interface for all possible api calls.
 */
interface Api {

    @GET("consensus")
    fun getConsensus(): Single<Response<List<Consensus>?>>

    @GET("consensus/{id}")
    fun getConsensusDetails(@Path("id") id: String): Single<Response<ConsensusDetail?>>

    @POST("consensus")
    fun postConsensus(@Body consensus: Consensus): Single<Response<Consensus?>>

    @POST("login-register")
    fun loginRegister(@Body loginRegister: LoginRegister): Single<Response<Token?>>

}


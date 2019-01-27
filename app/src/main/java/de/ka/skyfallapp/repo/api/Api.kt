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

    @GET("consensus/{id}/accept")
    fun getConsensusParticipation(@Path("id") id: String): Single<Response<ConsensusDetail?>>

    @POST("consensus")
    fun postConsensus(@Body consensus: Consensus): Single<Response<Consensus?>>

    @GET("personal-created-only")
    fun getCreatedConsensus(): Single<Response<List<Consensus>?>>

    @GET("personal-participated-only")
    fun getParticipatingConsensus(): Single<Response<List<Consensus>?>>

    @POST("login-register")
    fun loginRegister(@Body loginRegister: LoginRegister): Single<Response<Token?>>

}


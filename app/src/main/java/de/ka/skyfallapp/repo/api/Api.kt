package de.ka.skyfallapp.repo.api

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Interface for all possible api calls.
 */
interface Api {

    @GET("consensus")
    fun getConsensus(): Single<Response<List<Consensus>?>>

    @POST("consensus")
    fun postConsensus(@Body consensus: ConsensusDetail): Single<Response<ConsensusDetail?>>

    @GET("consensus/{id}")
    fun getConsensusDetails(@Path("id") id: String): Single<Response<ConsensusDetail?>>

    @DELETE("consensus/{id}")
    fun deleteConsensus(@Path("id") id: String): Single<Response<ResponseBody?>>

    @GET("personal-created-only")
    fun getCreatedConsensus(): Single<Response<List<Consensus>?>>

    @GET("personal-participated-only")
    fun getParticipatingConsensus(): Single<Response<List<Consensus>?>>

    @POST("login-register")
    fun loginRegister(@Body loginRegister: LoginRegister): Single<Response<Token?>>

}


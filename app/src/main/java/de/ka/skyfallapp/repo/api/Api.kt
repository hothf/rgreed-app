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
    fun getConsensus(): Single<Response<List<ConsensusResponse>?>>

    @POST("consensus")
    fun postConsensus(@Body consensusBody: ConsensusBody): Single<Response<ConsensusResponse?>>

    @PUT("consensus/{id}")
    fun putConsensus(@Path("id") id: Int, @Body consensusBody: ConsensusBody): Single<Response<ConsensusResponse?>>

    @GET("consensus/{id}")
    fun getConsensusDetails(@Path("id") id: Int): Single<Response<ConsensusResponse?>>

    @DELETE("consensus/{id}")
    fun deleteConsensus(@Path("id") id: Int): Single<Response<ResponseBody?>>

    @GET("consensus/{id}/suggestions")
    fun getConsensusSuggestions(@Path("id") id: Int): Single<Response<List<SuggestionResponse>?>>

    @GET("suggestions")
    fun getSuggestions(): Single<Response<List<SuggestionResponse>?>>

    @GET("suggestions/{id}")
    fun getSuggestionDetail(@Path("id") id: Int): Single<Response<SuggestionResponse?>>

    @POST("suggestions")
    fun postSuggestion(@Body suggestionBody: SuggestionBody): Single<Response<SuggestionResponse?>>

    @PUT("suggestions/{id}")
    fun putSuggestion(@Path("id") id: Int, @Body suggestionBody: SuggestionBody): Single<Response<SuggestionResponse?>>

    @POST("suggestions/{id}/vote")
    fun postSuggestionVote(@Path("id") id: Int, @Body  voteBody: VoteBody): Single<Response<SuggestionResponse?>>

    @DELETE("suggestions/{id}")
    fun deleteSuggestion(@Path("id") id: Int): Single<Response<ResponseBody?>>

    @POST("register")
    fun postRegister(@Body registerBody: RegisterBody): Single<Response<RegisterResponse?>>

    @POST("login")
    fun postLogin(@Body loginBody: LoginBody): Single<Response<ProfileResponse?>>

}


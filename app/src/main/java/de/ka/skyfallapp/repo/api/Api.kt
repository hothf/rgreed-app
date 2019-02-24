package de.ka.skyfallapp.repo.api

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Interface for all possible api calls.
 */
interface Api {

    @GET("consensus/personal")
    fun getPersonalConsensus(): Single<Response<List<ConsensusResponse>?>>

    @GET("consensus")
    fun getConsensus(@Query("limit") limit: Int, @Query("offset") offset: Int): Single<Response<List<ConsensusResponse>?>>

    @POST("consensus")
    fun postConsensus(@Body consensusBody: ConsensusBody): Single<Response<ConsensusResponse?>>

    @PUT("consensus/{id}")
    fun putConsensus(@Path("id") id: Int, @Body consensusBody: ConsensusBody): Single<Response<ConsensusResponse?>>

    @GET("consensus/{id}")
    fun getConsensusDetails(@Path("id") id: Int): Single<Response<ConsensusResponse?>>

    @DELETE("consensus/{id}")
    fun deleteConsensus(@Path("id") id: Int): Single<Response<ResponseBody?>>

    @GET("consensus/{consensusId}/suggestions")
    fun getConsensusSuggestions(@Path("consensusId") consensusId: Int): Single<Response<List<SuggestionResponse>?>>

    @GET("consensus/{consensusId}/suggestions/{id}")
    fun getSuggestionDetail(@Path("consensusId") consensusId: Int, @Path("id") id: Int): Single<Response<SuggestionResponse?>>

    @POST("consensus/{consensusId}/suggestions")
    fun postSuggestion(@Path("consensusId") consensusId: Int, @Body suggestionBody: SuggestionBody): Single<Response<SuggestionResponse?>>

    @PUT("consensus/{consensusId}/suggestions/{id}")
    fun putSuggestion(@Path("consensusId") consensusId: Int, @Path("id") id: Int, @Body suggestionBody: SuggestionBody): Single<Response<SuggestionResponse?>>

    @POST("consensus/{consensusId}/suggestions/{id}/vote")
    fun postSuggestionVote(@Path("consensusId") consensusId: Int, @Path("id") id: Int, @Body voteBody: VoteBody): Single<Response<SuggestionResponse?>>

    @DELETE("consensus/{consensusId}/suggestions/{id}")
    fun deleteSuggestion(@Path("consensusId") consensusId: Int, @Path("id") id: Int): Single<Response<ResponseBody?>>

    @POST("register")
    fun postRegister(@Body registerBody: RegisterBody): Single<Response<RegisterResponse?>>

    @POST("login")
    fun postLogin(@Body loginBody: LoginBody): Single<Response<LoginResponse?>>

}


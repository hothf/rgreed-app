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
        .addInterceptor(tokenInterceptor)
        .addInterceptor(HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { Timber.e(it) }).apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
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
     * Retrieves all personal consensus, where the user either is voting, an admin or has created a suggestion.
     */
    fun getPersonalConsensus() = api.getPersonalConsensus()

    /**
     * Retrieves all consensus.
     */
    fun getConsensus(limit: Int, offset: Int) = api.getConsensus(limit, offset)

    /**
     * Retrieves the details of a consensus.
     */
    fun getConsensusDetail(id: Int) = api.getConsensusDetails(id)

    /**
     * Deletes the given consensus.
     */
    fun deleteConsensus(id: Int) = api.deleteConsensus(id)

    /**
     * Updates the consensus with the given id.
     */
    fun updateConsensus(id: Int, consensus: ConsensusBody) = api.putConsensus(id, consensus)

    /**
     * Sends a consensus.
     */
    fun postConsensus(consensus: ConsensusBody) = api.postConsensus(consensus)

    /**
     * Retrieves the suggestions of a consensus.
     */
    fun getConsensusSuggestions(consensusId: Int) = api.getConsensusSuggestions(consensusId)

    /**
     * Retrieves the details of a suggestion.
     */
    fun getSuggestionDetail(consensusId: Int, id: Int) = api.getSuggestionDetail(consensusId, id)

    /**
     * Posts the given suggestion.
     */
    fun postSuggestion(consensusId: Int, suggestionBody: SuggestionBody) =
        api.postSuggestion(consensusId, suggestionBody)

    /**
     * Updates the given suggestion.
     */
    fun updateSuggestion(consensusId: Int, id: Int, suggestionBody: SuggestionBody) =
        api.putSuggestion(consensusId, id, suggestionBody)

    /**
     * Deletes the suggestion with the given id.
     */
    fun deleteSuggestion(consensusId: Int, id: Int) = api.deleteSuggestion(consensusId, id)

    /**
     * Votes for the given suggestion id with the given vote.
     */
    fun voteForSuggestion(consensusId: Int, id: Int, voteBody: VoteBody) =
        api.postSuggestionVote(consensusId, id, voteBody)

    /**
     * Sends a login.
     */
    fun postLogin(loginBody: LoginBody) = api.postLogin(loginBody)

    /**
     * Sens a registration.
     */
    fun postRegistration(registerBody: RegisterBody) = api.postRegister(registerBody)


}
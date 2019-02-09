package de.ka.skyfallapp.repo.api

import androidx.annotation.Keep
import java.io.Serializable

// api dao models

@Keep
data class ConsensusResponse(
    val id: Int,
    val title: String,
    val admin: Boolean = false,
    val suggestionsCount: Int
) : Serializable

@Keep
data class ConsensusBody(
    val title: String
) : Serializable

@Keep
data class SuggestionBody(val consensusId: Int, val title: String) : Serializable

@Keep
data class SuggestionResponse(
    val id: Int,
    val title: String,
    val overallAcceptance: Float = 0.0f,
    val creationDate: Long,
    val admin: Boolean = false
) : Serializable

@Keep
data class VoteBody(val acceptance: Float) : Serializable

@Keep
data class Profile(val username: String? = null, val token: String? = null)

@Keep
data class RegisterBody(val userName: String, val email: String, val password: String)

@Keep
data class RegisterResponse(val id: Int, val userName: String, val email: String)

@Keep
data class LoginBody(val name: String, val password: String)

@Keep
data class ProfileResponse(val id: Int, val userName: String, val email: String, val token: String)

package de.ka.skyfallapp.repo.api

import androidx.annotation.Keep
import java.io.Serializable

// api dao models

@Keep
data class ConsensusResponse(
    val id: Int,
    val title: String,
    val admin: Boolean = false,
    val suggestionsCount: Int,
    val public: Boolean,
    val creator: String,
    val creationDate: Long,
    val description: String? = null,
    val endDate: Long,
    val finished: Boolean,
    val hasAccess: Boolean
) : Serializable

@Keep
data class ConsensusBody(
    val title: String,
    val description: String? = null,
    val isPublic: Boolean = false,
    val endDate: Long,
    val privatePassword: String = ""
) : Serializable

@Keep
data class SuggestionBody(
    val title: String,
    val description: String? = null,
    val voteStartDate: Long
) : Serializable

@Keep
data class SuggestionResponse(
    val id: Int,
    val title: String,
    val consensusId: Int,
    val description: String? = null,
    val overallAcceptance: Float = 0.0f,
    val creationDate: Long,
    val voteStartDate: Long,
    val admin: Boolean = false
) : Serializable

@Keep
data class RequestAccessBody(val password: String) : Serializable

@Keep
data class VoteBody(val acceptance: Float) : Serializable

@Keep
data class RegisterBody(val userName: String, val email: String, val password: String)

@Keep
data class RegisterResponse(val id: Int, val userName: String, val email: String)

@Keep
data class LoginBody(val name: String, val password: String)

@Keep
data class LoginResponse(val id: Int? = null, val userName: String, val email: String, val token: String? = null)

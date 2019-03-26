package de.ka.skyfallapp.repo.api.models

import java.io.Serializable

// api dao models

data class ConsensusResponse(
    val id: Int,
    val title: String,
    val admin: Boolean = false,
    val suggestionsCount: Int,
    val public: Boolean,
    val creator: String,
    val creationDate: Long,
    val votingStartDate: Long,
    val description: String? = null,
    val endDate: Long,
    val voters: List<String> = listOf(),
    val finished: Boolean,
    val following: Boolean,
    val hasAccess: Boolean
) : Serializable

data class ConsensusBody(
    val title: String,
    val description: String? = null,
    val isPublic: Boolean = false,
    val votingStartDate: Long,
    val endDate: Long,
    val privatePassword: String = ""
) : Serializable

data class SuggestionBody(
    val title: String
) : Serializable

data class SuggestionResponse(
    val id: Int,
    val title: String,
    val consensusId: Int,
    val overallAcceptance: Float? = null,
    val creationDate: Long,
    val ownAcceptance: Float? = null,
    val voters: List<String> = listOf(),
    val admin: Boolean = false,
    val heavyObjectionsCount: Int? = null
) : Serializable

data class PushTokenBody(val pushToken: String)

data class FollowBody(val follow: Boolean)

data class RequestAccessBody(val password: String) : Serializable

data class VoteBody(val acceptance: Float) : Serializable

data class RegisterBody(val userName: String, val email: String, val password: String, var pushToken: String? = null)

data class LoginBody(val userName: String, val password: String, var pushToken: String? = null)

data class LoginResponse(val id: Int? = null, val userName: String, val email: String, val token: String? = null)

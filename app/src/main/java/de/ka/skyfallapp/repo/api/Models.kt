package de.ka.skyfallapp.repo.api

import androidx.annotation.Keep
import java.io.Serializable
import java.util.*

@Keep
data class Consensus(
    val id: String,
    val title: String = "untitled",
    val creationDate: Long = Calendar.getInstance().time.time,
    val participantsCount: Int,
    val suggestionsCount: Int,
    val isParticipating: Boolean = false,
    val isAdministrating: Boolean = false
) : Serializable

@Keep
data class ConsensusDetail(
    val id: String,
    val title: String = "untitled",
    val creationDate: Long = Calendar.getInstance().time.time,
    var participants: List<Participant>,
    val suggestions: List<Suggestion>,
    var isParticipating: Boolean = false,
    var isAdministrating: Boolean = false
) : Serializable

@Keep
data class Participant(val name: String) : Serializable

@Keep
data class Suggestion(
    val id: String,
    val title: String,
    val description: String?,
    val acceptance: Float,
    val participantCount: Int
) : Serializable

@Keep
data class Profile(val username: String? = null, val token: String? = null)

@Keep
data class LoginRegister(val user: String, val password: String)

@Keep
data class Token(val token: String)

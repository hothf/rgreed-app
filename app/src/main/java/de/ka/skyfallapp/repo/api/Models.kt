package de.ka.skyfallapp.repo.api

import androidx.annotation.Keep
import java.io.Serializable
import java.util.*

// api dao models
@Keep
data class Consensus(
    var id: String? = null,
    var creator: String = "server",
    var title: String = "untitled",
    var creationDate: Long = Calendar.getInstance().time.time,
    var participantsCount: Int = 0,
    var suggestionsCount: Int = 0,
    var isParticipating: Boolean = false,
    var isAdministrating: Boolean = false
) : Serializable

@Keep
data class ConsensusDetail(
    var id: String? = null,
    var creator: String? = null,
    var title: String? = null,
    var creationDate: Long = Calendar.getInstance().time.time,
    var participants: List<Participant> = listOf(),
    var suggestions: List<Suggestion> = listOf(),
    var isParticipating: Boolean? = null,
    var isAdministrating: Boolean? = null
) : Serializable

@Keep
data class Participant(val name: String) : Serializable

@Keep
data class Suggestion(
    var id: String? = null,
    var creator: String? = null,
    var title: String,
    var description: String? = "",
    var acceptance: Float = 0.0f,
    var participantCount: Int = 0
) : Serializable

@Keep
data class Profile(val username: String? = null, val token: String? = null)

@Keep
data class LoginRegister(val user: String, val password: String)

@Keep
data class Token(val token: String)

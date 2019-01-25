package de.ka.skyfallapp.repo.db

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class SnippetDao(
    @Id var id: Long = 142547635L,
    val user: String,
    val text: String
)

@Entity
data class ProfileDao(
    @Id var id: Long = 1L,
    val username: String? = null,
    val token: String? = null
)

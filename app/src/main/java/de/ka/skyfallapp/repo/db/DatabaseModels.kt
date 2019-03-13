package de.ka.skyfallapp.repo.db

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class ProfileDao(
    @Id var id: Long = 1L,
    val username: String? = null,
    val token: String? = null
)

@Entity
data class SearchHistoryDao(
    @Id var id: Long = 142547639L,
    val text: String
)

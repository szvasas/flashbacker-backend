package dev.vasas.flashbacker.domain

import java.time.LocalDateTime

data class Memory(
        val id: String,
        val userName: String,
        val location: String,
        val date: LocalDateTime,
        val text: String
)

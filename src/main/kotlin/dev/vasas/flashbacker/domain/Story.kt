package dev.vasas.flashbacker.domain

import java.time.LocalDateTime

data class Story(
        val id: String,
        val userId: String,
        val location: String,
        val date: LocalDateTime,
        val text: String
)

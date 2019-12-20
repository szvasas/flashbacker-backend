package dev.vasas.flashbacker.domain

import java.time.LocalDate

data class Story(
        val id: String,
        val userId: String,
        val location: String? = null,
        val dateHappened: LocalDate,
        val text: String
)

package dev.vasas.flashbacker.domain

import java.time.LocalDate
import java.time.ZonedDateTime

data class Story(
        val id: String,
        val userId: String,
        val location: String? = null,
        val dateHappened: LocalDate,
        val timestampCreated: ZonedDateTime,
        val text: String
)

data class StoryKey(
        val id: String,
        val userId: String,
        val dateHappened: LocalDate
)

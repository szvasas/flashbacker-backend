package dev.vasas.flashbacker.testtooling

import dev.vasas.flashbacker.domain.Memory
import java.time.LocalDateTime

val johnsGreatMemory = Memory(
        id = "test-id-1",
        userName = "John",
        location = "The Beach",
        date = LocalDateTime.of(2017, 12, 3, 15, 2, 3, 1_000_000),
        text = "Great things"
)

val johnsAwesomeMemory = johnsGreatMemory.copy(
        id = "test-id-2",
        location = "Home",
        date = johnsGreatMemory.date.minusDays(10),
        text = "Awesome stuff"
)

val johnsNiceMemory = johnsAwesomeMemory.copy(
        id = "test-id-3",
        location = "Airport",
        date = johnsGreatMemory.date.minusDays(20),
        text = "Nice things"
)

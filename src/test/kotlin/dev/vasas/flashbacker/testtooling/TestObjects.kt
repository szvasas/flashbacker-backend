package dev.vasas.flashbacker.testtooling

import dev.vasas.flashbacker.domain.Memory
import java.time.LocalDateTime

const val USER_ID_OF_BOB = "abcdef-12345"
const val USER_ID_OF_ALICE = "abcdef-67891"

val greatMemoryOfBob = Memory(
        id = "test-id-1",
        userId = USER_ID_OF_BOB,
        location = "The Beach",
        date = LocalDateTime.of(2017, 12, 3, 15, 2, 3, 1_000_000),
        text = "Great things"
)

val awesomeMemoryOfBob = greatMemoryOfBob.copy(
        id = "test-id-2",
        location = "Home",
        date = greatMemoryOfBob.date.minusDays(10),
        text = "Awesome stuff"
)

val niceMemoryOfBob = awesomeMemoryOfBob.copy(
        id = "test-id-3",
        location = "Airport",
        date = greatMemoryOfBob.date.minusDays(20),
        text = "Nice things"
)

val niceMemoryOfAlice = Memory(
        id = "test-id-4",
        userId = USER_ID_OF_ALICE,
        location = "in another country",
        date = LocalDateTime.of(2012, 5, 3, 15, 2, 1, 1_000_000),
        text = "Nice memories of Alice"
)

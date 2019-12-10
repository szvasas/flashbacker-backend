package dev.vasas.flashbacker.testtooling

import dev.vasas.flashbacker.domain.Story
import java.time.LocalDateTime

const val USER_ID_OF_BOB = "abcdef-12345"
const val USER_ID_OF_ALICE = "abcdef-67891"

val greatStoryOfBob = Story(
        id = "test-id-1",
        userId = USER_ID_OF_BOB,
        location = "The Beach",
        date = LocalDateTime.of(2017, 12, 3, 15, 2, 3, 1_000_000),
        text = "Great things"
)

val awesomeStoryOfBob = greatStoryOfBob.copy(
        id = "test-id-2",
        location = "Home",
        date = greatStoryOfBob.date.minusDays(10),
        text = "Awesome stuff"
)

val niceStoryOfBob = awesomeStoryOfBob.copy(
        id = "test-id-3",
        location = "Airport",
        date = greatStoryOfBob.date.minusDays(20),
        text = "Nice things"
)

val niceStoryOfAlice = Story(
        id = "test-id-4",
        userId = USER_ID_OF_ALICE,
        location = "in another country",
        date = LocalDateTime.of(2012, 5, 3, 15, 2, 1, 1_000_000),
        text = "Nice story of Alice"
)

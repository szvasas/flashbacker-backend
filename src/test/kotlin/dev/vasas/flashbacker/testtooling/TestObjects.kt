package dev.vasas.flashbacker.testtooling

import dev.vasas.flashbacker.domain.Story
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime

const val USER_ID_OF_BOB = "abcdef-12345"
const val USER_ID_OF_ALICE = "abcdef-67891"

val greatStoryOfBob = Story(
        id = "test-id-1",
        userId = USER_ID_OF_BOB,
        location = "The Beach",
        dateHappened = LocalDate.of(2017, 12, 3),
        timestampCreated = ZonedDateTime.of(
                LocalDateTime.of(2017, 12, 3, 15, 43, 15),
                UTC
        ),
        text = "Great things"
)

val greatStoryOfBobOnTheSameDay = greatStoryOfBob.copy(
        id = "test-id-5",
        text = "Same same but different"
)

val awesomeStoryOfBob = greatStoryOfBob.copy(
        id = "test-id-2",
        location = "Home",
        dateHappened = greatStoryOfBob.dateHappened.minusDays(10),
        text = "Awesome stuff"
)

val niceStoryOfBob = awesomeStoryOfBob.copy(
        id = "test-id-3",
        location = "Airport",
        dateHappened = greatStoryOfBob.dateHappened.minusDays(20),
        text = "Nice things"
)

val niceStoryOfAlice = Story(
        id = "test-id-4",
        userId = USER_ID_OF_ALICE,
        location = "in another country",
        dateHappened = LocalDate.of(2012, 5, 3),
        timestampCreated = ZonedDateTime.of(
                LocalDateTime.of(2012, 5, 3, 12, 5, 54),
                UTC
        ),
        text = "Nice story of Alice"
)

val niceStoryOfAliceWithoutLocation = niceStoryOfAlice.copy(
        id = "test-id-6",
        location = null
)

val niceStoryOfAliceWithBlankLocation = niceStoryOfAlice.copy(
        id = "test-id-7",
        location = "  "
)

val allStories = listOf(
        greatStoryOfBob,
        greatStoryOfBobOnTheSameDay,
        awesomeStoryOfBob,
        niceStoryOfBob,
        niceStoryOfAlice,
        niceStoryOfAliceWithoutLocation,
        niceStoryOfAliceWithBlankLocation
)

val storiesOfBob = allStories.filter {
    it.userId == USER_ID_OF_BOB
}

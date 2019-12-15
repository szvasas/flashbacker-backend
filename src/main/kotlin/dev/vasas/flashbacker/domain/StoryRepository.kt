package dev.vasas.flashbacker.domain

import java.time.LocalDate

interface StoryRepository {

    fun save(story: Story)

    fun deleteByUserDateHappenedStoryId(userId: String, dateHappened: LocalDate, storyId: String)

    fun findByUserDateHappenedStoryId(userId: String, dateHappened: LocalDate, storyId: String): Story?

    fun findStoriesForUser(userId: String): List<Story>

    fun findStoriesForUserAndDate(userId: String, dateHappened: LocalDate): List<Story>

}

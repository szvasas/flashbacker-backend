package dev.vasas.flashbacker.domain

import java.time.LocalDate

interface StoryRepository {

    fun save(story: Story)

    fun deleteById(id: String)

    fun deleteByUserDateHappenedStoryId(userId: String, dateHappened: LocalDate, storyId: String)

    fun findById(id: String): Story?

    fun findByUserDateHappenedStoryId(userId: String, dateHappened: LocalDate, storyId: String): Story?

    fun findStoriesForUser(userId: String): List<Story>

    fun findStoriesForUserAndDate(userId: String, dateHappened: LocalDate): List<Story>

}

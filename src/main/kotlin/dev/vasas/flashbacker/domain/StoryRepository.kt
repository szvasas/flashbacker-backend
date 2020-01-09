package dev.vasas.flashbacker.domain

import java.time.LocalDate

interface StoryRepository {

    fun save(story: Story)

    fun deleteByUserDateHappenedStoryId(userId: String, dateHappened: LocalDate, storyId: String)

    fun findByUserDateHappenedStoryId(userId: String, dateHappened: LocalDate, storyId: String): Story?

    fun findStoriesForUser(userId: String): List<Story>

    fun findStoriesForUserPaged(userId: String, pageRequest: PageRequest<StoryKey>): Page<Story>

}

data class Page<T>(
        val content: List<T>,
        val hasNext: Boolean
)

data class PageRequest<K>(
        val size: Int,
        val lastProcessedKey: K? = null
)

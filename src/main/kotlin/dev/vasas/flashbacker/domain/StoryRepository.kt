package dev.vasas.flashbacker.domain

interface StoryRepository {

    fun save(story: Story)

    fun deleteByKey(key: StoryKey)

    fun findByKey(key: StoryKey): Story?

    fun findStoriesForUser(userId: String, pageRequest: PageRequest<StoryKey>): Page<Story>

}

data class Page<T>(
        val content: List<T>,
        val hasNext: Boolean = false
)

data class PageRequest<K>(
        val limit: Int,
        val lastProcessedKey: K? = null
)

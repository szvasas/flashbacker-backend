package dev.vasas.flashbacker.domain.repository

import dev.vasas.flashbacker.domain.Story

interface StoryRepository {

    fun save(story: Story): Story

    fun deleteById(id: String)

    fun findById(id: String): Story?

    fun findStoriesForUser(userName: String): List<Story>
}

package dev.vasas.flashbacker.persistence.dynamodb.repository

import dev.vasas.flashbacker.domain.Story
import dev.vasas.flashbacker.domain.repository.StoryRepository
import dev.vasas.flashbacker.persistence.dynamodb.adapter.toStory
import dev.vasas.flashbacker.persistence.dynamodb.adapter.toStoryEntity
import dev.vasas.flashbacker.persistence.dynamodb.dao.StoryDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class DynamoDbStoryRepository(@Autowired private val storyDao: StoryDao) : StoryRepository {

    override fun save(story: Story): Story {
        return storyDao.save(story.toStoryEntity()).toStory()
    }

    override fun deleteById(id: String) {
        storyDao.deleteById(id)
    }

    override fun findById(id: String): Story? {
        return storyDao.findById(id)
                .map { it.toStory() }
                .orElse(null)
    }

    override fun findStoriesForUser(userName: String): List<Story> {
        return storyDao.findByUserId(userName).map { it.toStory() }
    }
}

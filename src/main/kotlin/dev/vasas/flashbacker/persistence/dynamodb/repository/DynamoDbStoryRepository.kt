package dev.vasas.flashbacker.persistence.dynamodb.repository

import dev.vasas.flashbacker.domain.Story
import dev.vasas.flashbacker.domain.repository.StoryRepository
import dev.vasas.flashbacker.persistence.dynamodb.dao.DynamoDbStoryDao
import dev.vasas.flashbacker.persistence.dynamodb.entity.StoryEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

@Repository
class DynamoDbStoryRepository(@Autowired private val storyDao: DynamoDbStoryDao) : StoryRepository {

    override fun save(story: Story) {
        return storyDao.save(story.toStoryEntity())
    }

    override fun deleteById(id: String) {
        storyDao.deleteById(id)
    }

    override fun findById(id: String): Story? {
        return storyDao.findById(id)?.toStory()
    }

    override fun findStoriesForUser(userName: String): List<Story> {
        return storyDao.findByUserId(userName).map { it.toStory() }
    }

    private fun Story.toStoryEntity(): StoryEntity {
        return StoryEntity(
                id = this.id,
                userId = this.userId,
                location = this.location,
                date = this.date.toInstant(ZoneOffset.UTC).toEpochMilli(),
                text = this.text
        )
    }

    private fun StoryEntity.toStory(): Story {
        return Story(
                id = this.id,
                userId = this.userId,
                location = this.location,
                date = LocalDateTime.ofInstant(Instant.ofEpochMilli(this.date), ZoneOffset.UTC),
                text = this.text
        )
    }
}

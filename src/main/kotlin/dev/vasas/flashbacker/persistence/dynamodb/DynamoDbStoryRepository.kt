package dev.vasas.flashbacker.persistence.dynamodb

import dev.vasas.flashbacker.domain.Story
import dev.vasas.flashbacker.domain.StoryRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

@Repository
class DynamoDbStoryRepository(@Autowired private val storyDao: DynamoDbStoryDao) : StoryRepository {

    override fun save(story: Story) {
        return storyDao.save(story.toStoryEntity())
    }

    override fun deleteById(id: String) {
        storyDao.deleteById(id)
    }

    override fun deleteByUserDateHappenedStoryId(userId: String, dateHappened: LocalDate, storyId: String) {
        storyDao.deleteByUserIdAndDateHappenedAndId(userId, "${dateHappened}_$storyId")
    }

    override fun findById(id: String): Story? {
        return storyDao.findById(id)?.toStory()
    }

    override fun findByUserDateHappenedStoryId(userId: String, dateHappened: LocalDate, storyId: String): Story? {
        return storyDao.findByUserIdAndDateHappenedAndId(userId, "${dateHappened}_$storyId")?.toStory()
    }

    override fun findStoriesForUser(userId: String): List<Story> {
        return storyDao.findByUserId(userId).map { it.toStory() }
    }

    override fun findStoriesForUserAndDate(userId: String, dateHappened: LocalDate): List<Story> {
        return storyDao.findByUserIdAndDateHappened(userId, dateHappened).map { it.toStory() }
    }

    private fun Story.toStoryEntity(): StoryEntity {
        return StoryEntity(
                id = this.id,
                userId = this.userId,
                dateHappenedAndId = "${this.dateHappened}_${this.id}",
                location = this.location,
                dateHappened = this.dateHappened.toEpochDay(),
                timestampAdded = ZonedDateTime.now(ZoneId.of("UTC")).toInstant().toEpochMilli(),
                text = this.text
        )
    }

    private fun StoryEntity.toStory(): Story {
        return Story(
                id = this.id,
                userId = this.userId,
                location = this.location,
                dateHappened = LocalDate.ofEpochDay(this.dateHappened),
                text = this.text
        )
    }
}

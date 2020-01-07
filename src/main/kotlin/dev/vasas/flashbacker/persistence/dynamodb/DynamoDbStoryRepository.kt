package dev.vasas.flashbacker.persistence.dynamodb

import dev.vasas.flashbacker.domain.Story
import dev.vasas.flashbacker.domain.StoryRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

@Repository
class DynamoDbStoryRepository(@Autowired private val storyDao: DynamoDbStoryDao) : StoryRepository {

    override fun save(story: Story) {
        return storyDao.save(story.toStoryEntity())
    }

    override fun deleteByUserDateHappenedStoryId(userId: String, dateHappened: LocalDate, storyId: String) {
        storyDao.deleteByUserIdAndDateHappenedAndId(userId, createCompositeSortKey(dateHappened, storyId))
    }

    override fun findByUserDateHappenedStoryId(userId: String, dateHappened: LocalDate, storyId: String): Story? {
        return storyDao.findByUserIdAndDateHappenedAndId(userId, createCompositeSortKey(dateHappened, storyId))?.toStory()
    }

    override fun findStoriesForUser(userId: String): List<Story> {
        return storyDao.findByUserId(userId).map { it.toStory() }
    }

    override fun findStoriesForUserAndDate(userId: String, dateHappened: LocalDate): List<Story> {
        return storyDao.findByUserIdAndDateHappened(userId, dateHappened).map { it.toStory() }
    }

}

internal fun createCompositeSortKey(dateHappened: LocalDate, storyId: String): String {
    return "${dateHappened}_${storyId}"
}

private fun Story.toStoryEntity(): StoryEntity {
    return StoryEntity(
            id = this.id,
            userId = this.userId,
            dateHappenedAndId = createCompositeSortKey(dateHappened, id),
            location = this.location,
            dateHappened = this.dateHappened.toEpochDay(),
            timestampCreated = this.timestampCreated.toInstant().toEpochMilli(),
            text = this.text
    )
}

private fun StoryEntity.toStory(): Story {
    return Story(
            id = this.id,
            userId = this.userId,
            location = this.location,
            dateHappened = LocalDate.ofEpochDay(this.dateHappened),
            timestampCreated = ZonedDateTime.ofInstant(Instant.ofEpochMilli(this.timestampCreated), ZoneId.of("UTC")),
            text = this.text
    )
}

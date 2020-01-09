package dev.vasas.flashbacker.persistence.dynamodb

import dev.vasas.flashbacker.domain.Page
import dev.vasas.flashbacker.domain.PageRequest
import dev.vasas.flashbacker.domain.Story
import dev.vasas.flashbacker.domain.StoryKey
import dev.vasas.flashbacker.domain.StoryRepository
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.dateHappenedFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.idFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.textFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.timestampCreatedFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.userIdFieldName
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

    override fun deleteByKey(key: StoryKey) {
        storyDao.deleteByKey(key.toStoryEntityKey())
    }

    override fun findByKey(key: StoryKey): Story? {
        return storyDao.findByKey(key.toStoryEntityKey())?.toStory()
    }

    override fun findStoriesForUser(userId: String, pageRequest: PageRequest<StoryKey>): Page<Story> {
        val daoPageRequest = PageRequest(pageRequest.limit, pageRequest.lastProcessedKey?.toStoryEntityKey())
        val daoPage = storyDao.findByUserId(userId, daoPageRequest)
        return Page(
                daoPage.content.map { it.toStory() },
                daoPage.hasNext
        )
    }

}

private const val COMPOSITE_KEY_DELIMITER = "_"

internal fun createCompositeSortKey(dateHappened: LocalDate, storyId: String): String {
    return "${dateHappened}$COMPOSITE_KEY_DELIMITER${storyId}"
}

internal fun StoryKey.toStoryEntityKey(): StoryEntityKey {
    return StoryEntityKey(
            userId = this.userId,
            dateHappenedAndId = createCompositeSortKey(this.dateHappened, this.id)
    )
}

internal fun Story.toStoryEntity(): StoryEntity {
    return StoryEntity(
            id = this.id,
            userId = this.userId,
            dateHappenedAndId = createCompositeSortKey(dateHappened, id),
            location = this.location.takeUnless { it.isNullOrBlank() },
            dateHappened = this.dateHappened.toEpochDay(),
            timestampCreated = this.timestampCreated.toInstant().toEpochMilli(),
            text = this.text
    )
}

internal fun StoryEntity.toStory(): Story {
    return Story(
            id = this.id ?: throwInvalidDynamoDbItemException(idFieldName),
            userId = this.userId ?: throwInvalidDynamoDbItemException(userIdFieldName),
            location = this.location,
            dateHappened = this.dateHappened?.let { LocalDate.ofEpochDay(it) }
                    ?: throwInvalidDynamoDbItemException(dateHappenedFieldName),
            timestampCreated = this.timestampCreated?.let { ZonedDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.of("UTC")) }
                    ?: throwInvalidDynamoDbItemException(timestampCreatedFieldName),
            text = this.text ?: throwInvalidDynamoDbItemException(textFieldName)
    )
}

private fun throwInvalidDynamoDbItemException(fieldName: String): Nothing {
    throw InvalidDynamoDbItemException.missingMandatoryField(fieldName)
}

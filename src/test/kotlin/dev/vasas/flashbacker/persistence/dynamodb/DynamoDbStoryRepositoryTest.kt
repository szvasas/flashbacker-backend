package dev.vasas.flashbacker.persistence.dynamodb

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import dev.vasas.flashbacker.domain.PageRequest
import dev.vasas.flashbacker.domain.Story
import dev.vasas.flashbacker.domain.StoryKey
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.dateHappenedFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.idFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.textFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.timestampCreatedFieldName
import dev.vasas.flashbacker.testtooling.DynamoDbIntegrationTest
import dev.vasas.flashbacker.testtooling.allStories
import dev.vasas.flashbacker.testtooling.greatStoryOfBob
import dev.vasas.flashbacker.testtooling.niceStoryOfAlice
import dev.vasas.flashbacker.testtooling.niceStoryOfAliceWithBlankLocation
import dev.vasas.flashbacker.testtooling.niceStoryOfAliceWithoutLocation
import dev.vasas.flashbacker.testtooling.storiesOfBob
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

@DynamoDbIntegrationTest
internal class DynamoDbStoryRepositoryTest(
        @Autowired private val dynamoDbMapper: DynamoDBMapper,
        @Autowired private val dynamoDbStoryRepository: DynamoDbStoryRepository
) {

    @AfterEach
    fun clearTestData() {
        val failedBatches = dynamoDbMapper.batchDelete(allStories.map { it.toStoryEntity() })
        failedBatches.firstOrNull()?.let { throw it.exception }
    }

    @Nested
    inner class `given there are no items in the DB` {

        @Test
        fun `find method returns null`() {
            // when
            val searchResult = dynamoDbStoryRepository.findByUserDateHappenedStoryId(greatStoryOfBob.userId, greatStoryOfBob.dateHappened, greatStoryOfBob.id)

            // then
            assertThat(searchResult).isNull()
        }

        @Test
        fun `delete method does not throw`() {
            dynamoDbStoryRepository.deleteByUserDateHappenedStoryId(greatStoryOfBob.userId, greatStoryOfBob.dateHappened, greatStoryOfBob.id)
        }
    }

    @Nested
    inner class `given there are valid items in the DB` {

        @BeforeEach
        fun loadTestData() {
            val failedBatches = dynamoDbMapper.batchSave(allStories.map { it.toStoryEntity() })
            failedBatches.firstOrNull()?.let { throw it.exception }
        }

        @Test
        fun `repository finds saved Story by userId, dateHappened and storyId`() {
            // when
            val foundStory = dynamoDbStoryRepository.findByUserDateHappenedStoryId(greatStoryOfBob.userId, greatStoryOfBob.dateHappened, greatStoryOfBob.id)

            // then
            assertThat(foundStory).isEqualTo(greatStoryOfBob)
        }

        @Test
        fun `repository deletes existing Story by userId, dateHappened and storyId`() {
            // when
            dynamoDbStoryRepository.deleteByUserDateHappenedStoryId(greatStoryOfBob.userId, greatStoryOfBob.dateHappened, greatStoryOfBob.id)

            // then
            assertThat(dynamoDbStoryRepository.findByUserDateHappenedStoryId(greatStoryOfBob.userId, greatStoryOfBob.dateHappened, greatStoryOfBob.id)).isNull()
        }

        @Test
        fun `repository finds all the stories of the specified user but does not load stories of other users`() {
            // when
            val storiesOfAUser = dynamoDbStoryRepository.findStoriesForUser(greatStoryOfBob.userId)

            // then
            assertThat(storiesOfAUser.toSet()).isEqualTo(storiesOfBob.toSet())
        }

        @Test
        fun `found stories for a user are sorted by dateHappened and id descending`() {
            // when
            val storiesOfAUser = dynamoDbStoryRepository.findStoriesForUser(greatStoryOfBob.userId)

            // then
            val comparator = compareBy(Story::dateHappened, Story::id).reversed()
            assertThat(storiesOfAUser).isEqualTo(storiesOfBob.sortedWith(comparator))
        }

        @Test
        fun `found story count in a page is equal to the requested value`() {
            // when
            val requestSize = 2
            val foundPage = dynamoDbStoryRepository.findStoriesForUserPaged(greatStoryOfBob.userId, PageRequest(requestSize))

            // then
            assertThat(foundPage.content.size).isEqualTo(requestSize)
        }

        @Test
        fun `second story page contains elements starting with the last element of the first page`() {
            // given
            val firstPageSize = storiesOfBob.size - 1
            val firstPage = dynamoDbStoryRepository.findStoriesForUserPaged(greatStoryOfBob.userId, PageRequest(firstPageSize))

            // when
            val lastProcessed = firstPage.content.last()
            val secondPage = dynamoDbStoryRepository.findStoriesForUserPaged(greatStoryOfBob.userId, PageRequest(
                    Integer.MAX_VALUE,
                    StoryKey(lastProcessed.id, lastProcessed.userId, lastProcessed.dateHappened)
            ))

            // then
            assertThat(lastProcessed).isEqualTo(secondPage.content.first())

            val joinedPages = firstPage.content + secondPage.content.drop(1)
            val comparator = compareBy(Story::dateHappened, Story::id).reversed()
            assertThat(joinedPages).isEqualTo(storiesOfBob.sortedWith(comparator))
        }

    }

    @Nested
    inner class `given there is an invalid item in the DB` {

        @ParameterizedTest
        @ValueSource(strings = [idFieldName, dateHappenedFieldName, timestampCreatedFieldName, textFieldName])
        fun `repository throws when a mandatory field is not present in a DB item`(missingFieldName: String) {
            // given
            val invalidStoryEntity = clearFieldValue(greatStoryOfBob.toStoryEntity(), missingFieldName)
            dynamoDbMapper.save(invalidStoryEntity)

            // expect
            val thrown = assertThrows<InvalidDynamoDbItemException> {
                dynamoDbStoryRepository.findStoriesForUser(greatStoryOfBob.userId)
            }

            assertThat(thrown.message).isEqualTo("Cannot load item from DynamoDb! Missing mandatory field: $missingFieldName")
        }
    }

    @Test
    fun `save method saves Story with location properly`() {
        // given
        dynamoDbStoryRepository.save(niceStoryOfAlice)

        // when
        val savedStory = dynamoDbStoryRepository.findByUserDateHappenedStoryId(
                niceStoryOfAlice.userId,
                niceStoryOfAlice.dateHappened,
                niceStoryOfAlice.id
        )

        // then
        assertThat(savedStory).isEqualTo(niceStoryOfAlice)
    }

    @Test
    fun `save method saves Story without location properly`() {
        // given
        dynamoDbStoryRepository.save(niceStoryOfAliceWithoutLocation)

        // when
        val savedStory = dynamoDbStoryRepository.findByUserDateHappenedStoryId(
                niceStoryOfAliceWithoutLocation.userId,
                niceStoryOfAliceWithoutLocation.dateHappened,
                niceStoryOfAliceWithoutLocation.id
        )

        // then
        assertThat(savedStory).isEqualTo(niceStoryOfAliceWithoutLocation)
    }

    @Test
    fun `save method saves Story with blank location properly`() {
        // given
        dynamoDbStoryRepository.save(niceStoryOfAliceWithBlankLocation)

        // when
        val savedStory = dynamoDbStoryRepository.findByUserDateHappenedStoryId(
                niceStoryOfAliceWithBlankLocation.userId,
                niceStoryOfAliceWithBlankLocation.dateHappened,
                niceStoryOfAliceWithBlankLocation.id
        )

        // then
        assertThat(savedStory).isEqualTo(niceStoryOfAliceWithBlankLocation.copy(location = null))
    }

    private fun clearFieldValue(storyEntity: StoryEntity, fieldName: String): StoryEntity {
        val result = storyEntity.copy()
        StoryEntity::class
                .memberProperties
                .filterIsInstance<KMutableProperty<*>>()
                .find { it.name == fieldName }!!
                .setter
                .call(result, null)
        return result
    }
}

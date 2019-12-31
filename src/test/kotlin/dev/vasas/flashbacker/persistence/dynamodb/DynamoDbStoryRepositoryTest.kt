package dev.vasas.flashbacker.persistence.dynamodb

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import dev.vasas.flashbacker.domain.Story
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.dateHappenedAndIdFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.dateHappenedFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.idFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.locationFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.storyTableName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.textFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.timestampAddedFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.userIdFieldName
import dev.vasas.flashbacker.testtooling.DynamoDbIntegrationTest
import dev.vasas.flashbacker.testtooling.allStories
import dev.vasas.flashbacker.testtooling.greatStoryOfBob
import dev.vasas.flashbacker.testtooling.greatStoryOfBobOnTheSameDay
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

@DynamoDbIntegrationTest
internal class DynamoDbStoryRepositoryTest(
        @Autowired private val dynamoDb: AmazonDynamoDB,
        @Autowired private val dynamoDbStoryRepository: DynamoDbStoryRepository
) {

    @AfterEach
    fun clearTestData() {
        allStories.forEach {
            dynamoDb.deleteItem(storyTableName, mapOf(
                    userIdFieldName to AttributeValue(it.userId),
                    dateHappenedAndIdFieldName to AttributeValue(createCompositeSortKey(it.dateHappened, it.id))
            ))
        }
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
            allStories.forEach {
                dynamoDb.putItem(storyTableName, createAttributeMapFromStory(it))
            }
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

            assertThat(storiesOfAUser.toSet()).isEqualTo(storiesOfBob.toSet())
        }

        @Test
        fun `repository finds all the stories of the specified user and date`() {
            // when
            val storiesOfAUser = dynamoDbStoryRepository.findStoriesForUserAndDate(greatStoryOfBob.userId, greatStoryOfBob.dateHappened)

            assertThat(storiesOfAUser.toSet()).isEqualTo(setOf(greatStoryOfBob, greatStoryOfBobOnTheSameDay))
        }
    }

    @Nested
    inner class `given there is an invalid item in the DB` {

        @ParameterizedTest
        @ValueSource(strings = [idFieldName, dateHappenedFieldName, timestampAddedFieldName, textFieldName])
        fun `repository throws when a mandatory field is not present in a DB item`(missingFieldName: String) {
            // given
            val invalidDbItem = createAttributeMapFromStory(greatStoryOfBob).apply {
                set(missingFieldName, null)
            }
            dynamoDb.putItem(storyTableName, invalidDbItem)

            // expect
            val thrown = assertThrows<InvalidDynamoDbItemException> {
                dynamoDbStoryRepository.findStoriesForUser(greatStoryOfBob.userId)
            }

            assertThat(thrown.message).isEqualTo("Cannot load item from DynamoDb! Missing mandatory field: $missingFieldName")
        }
    }

    @Test
    fun `save method saves Story with location properly`() {
        dynamoDbStoryRepository.save(niceStoryOfAlice)

        val savedStory = dynamoDbStoryRepository.findByUserDateHappenedStoryId(
                niceStoryOfAlice.userId,
                niceStoryOfAlice.dateHappened,
                niceStoryOfAlice.id
        )
        assertThat(savedStory).isEqualTo(niceStoryOfAlice)
    }

    @Test
    fun `save method saves Story without location properly`() {
        dynamoDbStoryRepository.save(niceStoryOfAliceWithoutLocation)

        val savedStory = dynamoDbStoryRepository.findByUserDateHappenedStoryId(
                niceStoryOfAliceWithoutLocation.userId,
                niceStoryOfAliceWithoutLocation.dateHappened,
                niceStoryOfAliceWithoutLocation.id
        )
        assertThat(savedStory).isEqualTo(niceStoryOfAliceWithoutLocation)
    }

    @Test
    fun `save method saves Story with blank location properly`() {
        dynamoDbStoryRepository.save(niceStoryOfAliceWithBlankLocation)

        val savedStory = dynamoDbStoryRepository.findByUserDateHappenedStoryId(
                niceStoryOfAliceWithBlankLocation.userId,
                niceStoryOfAliceWithBlankLocation.dateHappened,
                niceStoryOfAliceWithBlankLocation.id
        )
        assertThat(savedStory).isEqualTo(niceStoryOfAliceWithBlankLocation.copy(location = null))
    }

    private fun createAttributeMapFromStory(story: Story): MutableMap<String, AttributeValue?> {
        val result = mutableMapOf(
                idFieldName to AttributeValue(story.id),
                userIdFieldName to AttributeValue(story.userId),
                dateHappenedAndIdFieldName to AttributeValue(createCompositeSortKey(story.dateHappened, story.id)),
                dateHappenedFieldName to AttributeValue().withN(story.dateHappened.toEpochDay().toString()),
                timestampAddedFieldName to AttributeValue().withN("1234"),
                textFieldName to AttributeValue(story.text)
        )
        if (!story.location.isNullOrBlank()) {
            result[locationFieldName] = AttributeValue(story.location)
        }
        return result
    }
}

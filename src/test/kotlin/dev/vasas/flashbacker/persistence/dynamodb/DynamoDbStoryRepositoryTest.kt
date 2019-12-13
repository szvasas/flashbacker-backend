package dev.vasas.flashbacker.persistence.dynamodb

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.dateHappenedAndIdFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.dateHappenedFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.idFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.locationFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.storyTableName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.textFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.timestampAddedFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.userIdFieldName
import dev.vasas.flashbacker.testtooling.DynamoDbIntegrationTest
import dev.vasas.flashbacker.testtooling.awesomeStoryOfBob
import dev.vasas.flashbacker.testtooling.greatStoryOfBob
import dev.vasas.flashbacker.testtooling.greatStoryOfBobOnTheSameDay
import dev.vasas.flashbacker.testtooling.niceStoryOfAlice
import dev.vasas.flashbacker.testtooling.niceStoryOfBob
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

@DynamoDbIntegrationTest
internal class DynamoDbStoryRepositoryTest(
        @Autowired private val dynamoDb: AmazonDynamoDB,
        @Autowired private val dynamoDbStoryRepository: DynamoDbStoryRepository
) {

    @AfterEach
    fun cleanUpStoryTable() {
        dynamoDb.deleteItem(storyTableName, mapOf(idFieldName to AttributeValue(greatStoryOfBob.id)))
        dynamoDb.deleteItem(storyTableName, mapOf(idFieldName to AttributeValue(greatStoryOfBobOnTheSameDay.id)))
        dynamoDb.deleteItem(storyTableName, mapOf(idFieldName to AttributeValue(awesomeStoryOfBob.id)))
        dynamoDb.deleteItem(storyTableName, mapOf(idFieldName to AttributeValue(niceStoryOfBob.id)))
        dynamoDb.deleteItem(storyTableName, mapOf(idFieldName to AttributeValue(niceStoryOfAlice.id)))
    }

    @Test
    fun `repository finds saved Story by id`() {
        // given
        dynamoDbStoryRepository.save(greatStoryOfBob)

        // when
        val foundStory = dynamoDbStoryRepository.findById(greatStoryOfBob.id)

        // then
        assertThat(foundStory).isEqualTo(greatStoryOfBob)
    }

    @Test
    fun `repository finds saved Story by userId, dateHappened and storyId`() {
        // given
        dynamoDbStoryRepository.save(greatStoryOfBob)

        // when
        val foundStory = dynamoDbStoryRepository.findByUserDateHappenedStoryId(greatStoryOfBob.userId, greatStoryOfBob.dateHappened, greatStoryOfBob.id)

        // then
        assertThat(foundStory).isEqualTo(greatStoryOfBob)
    }

    @Test
    fun `repository returns null when no Story found for a given id`() {
        // when
        val searchResult = dynamoDbStoryRepository.findById(greatStoryOfBob.id)

        // then
        assertThat(searchResult).isNull()
    }

    @Test
    fun `repository returns null when no Story found for a given userId, dateHappened and storyId`() {
        // when
        val searchResult = dynamoDbStoryRepository.findByUserDateHappenedStoryId(greatStoryOfBob.userId, greatStoryOfBob.dateHappened, greatStoryOfBob.id)

        // then
        assertThat(searchResult).isNull()
    }

    @Test
    fun `repository deletes existing Story`() {
        // given
        dynamoDbStoryRepository.save(greatStoryOfBob)

        // when
        dynamoDbStoryRepository.deleteById(greatStoryOfBob.id)

        // then
        assertThat(dynamoDbStoryRepository.findById(greatStoryOfBob.id)).isNull()
    }

    @Test
    fun `repository deletes existing Story by userId, dateHappened and storyId`() {
        // given
        dynamoDbStoryRepository.save(greatStoryOfBob)

        // when
        dynamoDbStoryRepository.deleteByUserDateHappenedStoryId(greatStoryOfBob.userId, greatStoryOfBob.dateHappened, greatStoryOfBob.id)

        // then
        assertThat(dynamoDbStoryRepository.findById(greatStoryOfBob.id)).isNull()
    }

    @Test
    fun `repository does not throw when trying to delete a non-existing Story`() {
        dynamoDbStoryRepository.deleteById(greatStoryOfBob.id)
    }

    @Test
    fun `repository finds all the stories of the specified user but does not load stories of other users`() {
        // given
        dynamoDbStoryRepository.save(greatStoryOfBob)
        dynamoDbStoryRepository.save(awesomeStoryOfBob)
        dynamoDbStoryRepository.save(niceStoryOfBob)
        dynamoDbStoryRepository.save(niceStoryOfAlice)

        // when
        val storiesOfAUser = dynamoDbStoryRepository.findStoriesForUser(greatStoryOfBob.userId)

        assertThat(storiesOfAUser.toSet()).isEqualTo(setOf(greatStoryOfBob, awesomeStoryOfBob, niceStoryOfBob))
    }

    @Test
    fun `repository finds all the stories of the specified user and date`() {
        // given
        dynamoDbStoryRepository.save(greatStoryOfBob)
        dynamoDbStoryRepository.save(greatStoryOfBobOnTheSameDay)
        dynamoDbStoryRepository.save(awesomeStoryOfBob)
        dynamoDbStoryRepository.save(niceStoryOfBob)
        dynamoDbStoryRepository.save(niceStoryOfAlice)

        // when
        val storiesOfAUser = dynamoDbStoryRepository.findStoriesForUserAndDate(greatStoryOfBob.userId, greatStoryOfBob.dateHappened)

        assertThat(storiesOfAUser.toSet()).isEqualTo(setOf(greatStoryOfBob, greatStoryOfBobOnTheSameDay))
    }

    @Test
    fun `repository throws when userId is not present in a DynamoDb item`() {
        // given
        dynamoDb.putItem(storyTableName, mapOf(
                idFieldName to AttributeValue(greatStoryOfBob.id),
                dateHappenedAndIdFieldName to AttributeValue(dateHappenedAndIdFieldName),
                locationFieldName to AttributeValue(greatStoryOfBob.location),
                dateHappenedFieldName to AttributeValue().withN("1234"),
                timestampAddedFieldName to AttributeValue().withN("1234"),
                textFieldName to AttributeValue(greatStoryOfBob.text)
        ))

        val thrown = assertThrows<InvalidDynamoDbItemException> {
            dynamoDbStoryRepository.findById(greatStoryOfBob.id)
        }

        assertThat(thrown.message).isEqualTo("Cannot load item from DynamoDb! Missing mandatory field: userId")
    }

    @Test
    fun `repository throws when location is not present in a DynamoDb item`() {
        // given
        dynamoDb.putItem(storyTableName, mapOf(
                idFieldName to AttributeValue(greatStoryOfBob.id),
                userIdFieldName to AttributeValue(greatStoryOfBob.userId),
                dateHappenedAndIdFieldName to AttributeValue(dateHappenedAndIdFieldName),
                dateHappenedFieldName to AttributeValue().withN("1234"),
                timestampAddedFieldName to AttributeValue().withN("1234"),
                textFieldName to AttributeValue(greatStoryOfBob.text)
        ))

        val thrown = assertThrows<InvalidDynamoDbItemException> {
            dynamoDbStoryRepository.findById(greatStoryOfBob.id)
        }

        assertThat(thrown.message).isEqualTo("Cannot load item from DynamoDb! Missing mandatory field: location")
    }

    @Test
    fun `repository throws when dateHappened is not present in a DynamoDb item`() {
        // given
        dynamoDb.putItem(storyTableName, mapOf(
                idFieldName to AttributeValue(greatStoryOfBob.id),
                userIdFieldName to AttributeValue(greatStoryOfBob.userId),
                dateHappenedAndIdFieldName to AttributeValue(dateHappenedAndIdFieldName),
                locationFieldName to AttributeValue(greatStoryOfBob.location),
                timestampAddedFieldName to AttributeValue().withN("1234"),
                textFieldName to AttributeValue(greatStoryOfBob.text)
        ))

        val thrown = assertThrows<InvalidDynamoDbItemException> {
            dynamoDbStoryRepository.findById(greatStoryOfBob.id)
        }

        assertThat(thrown.message).isEqualTo("Cannot load item from DynamoDb! Missing mandatory field: dateHappened")
    }

    @Test
    fun `repository throws when timestampAdded is not present in a DynamoDb item`() {
        // given
        dynamoDb.putItem(storyTableName, mapOf(
                idFieldName to AttributeValue(greatStoryOfBob.id),
                userIdFieldName to AttributeValue(greatStoryOfBob.userId),
                dateHappenedAndIdFieldName to AttributeValue(dateHappenedAndIdFieldName),
                locationFieldName to AttributeValue(greatStoryOfBob.location),
                dateHappenedFieldName to AttributeValue().withN("1234"),
                textFieldName to AttributeValue(greatStoryOfBob.text)
        ))

        val thrown = assertThrows<InvalidDynamoDbItemException> {
            dynamoDbStoryRepository.findById(greatStoryOfBob.id)
        }

        assertThat(thrown.message).isEqualTo("Cannot load item from DynamoDb! Missing mandatory field: timestampAdded")
    }

    @Test
    fun `repository throws when text is not present in a DynamoDb item`() {
        // given
        dynamoDb.putItem(storyTableName, mapOf(
                idFieldName to AttributeValue(greatStoryOfBob.id),
                userIdFieldName to AttributeValue(greatStoryOfBob.userId),
                dateHappenedAndIdFieldName to AttributeValue(dateHappenedAndIdFieldName),
                locationFieldName to AttributeValue(greatStoryOfBob.location),
                dateHappenedFieldName to AttributeValue().withN("1234"),
                timestampAddedFieldName to AttributeValue().withN("1234")
        ))

        val thrown = assertThrows<InvalidDynamoDbItemException> {
            dynamoDbStoryRepository.findById(greatStoryOfBob.id)
        }

        assertThat(thrown.message).isEqualTo("Cannot load item from DynamoDb! Missing mandatory field: text")
    }
}

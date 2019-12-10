package dev.vasas.flashbacker.persistence.dynamodb.repository

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import dev.vasas.flashbacker.persistence.dynamodb.entity.StoryEntity.Companion.storyTableName
import dev.vasas.flashbacker.testtooling.DynamoDbIntegrationTest
import dev.vasas.flashbacker.testtooling.awesomeStoryOfBob
import dev.vasas.flashbacker.testtooling.greatStoryOfBob
import dev.vasas.flashbacker.testtooling.niceStoryOfBob
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException

@DynamoDbIntegrationTest
internal class DynamoDbStoryRepositoryTest(
        @Autowired private val dynamoDb: AmazonDynamoDB,
        @Autowired private val dynamoDbStoryRepository: DynamoDbStoryRepository
) {

    @AfterEach
    fun cleanUpStoryTable() {
        dynamoDb.deleteItem(storyTableName, mapOf("id" to AttributeValue(greatStoryOfBob.id)))
        dynamoDb.deleteItem(storyTableName, mapOf("id" to AttributeValue(awesomeStoryOfBob.id)))
        dynamoDb.deleteItem(storyTableName, mapOf("id" to AttributeValue(niceStoryOfBob.id)))
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
    fun `repository returns null when no Story found for a given id`() {
        // when
        val searchResult = dynamoDbStoryRepository.findById(greatStoryOfBob.id)

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
    fun `repository throws when trying to delete a non-existing Story`() {
        assertThrows<EmptyResultDataAccessException> {
            dynamoDbStoryRepository.deleteById(greatStoryOfBob.id)
        }
    }

    @Test
    fun `repository finds all the stories of a user`() {
        // given
        dynamoDbStoryRepository.save(greatStoryOfBob)
        dynamoDbStoryRepository.save(awesomeStoryOfBob)
        dynamoDbStoryRepository.save(niceStoryOfBob)

        // when
        val storiesOfAUser = dynamoDbStoryRepository.findStoriesForUser(greatStoryOfBob.userId)

        assertThat(storiesOfAUser.toSet()).isEqualTo(setOf(greatStoryOfBob, awesomeStoryOfBob, niceStoryOfBob))
    }
}

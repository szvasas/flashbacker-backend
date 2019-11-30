package dev.vasas.flashbacker.persistence.dynamodb.repository

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import dev.vasas.flashbacker.persistence.dynamodb.entity.MemoryEntity.Companion.memoryTableName
import dev.vasas.flashbacker.testtooling.DynamoDbIntegrationTest
import dev.vasas.flashbacker.testtooling.johnsAwesomeMemory
import dev.vasas.flashbacker.testtooling.johnsGreatMemory
import dev.vasas.flashbacker.testtooling.johnsNiceMemory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException

@DynamoDbIntegrationTest
internal class DynamoDbMemoryRepositoryTest(
        @Autowired private val dynamoDb: AmazonDynamoDB,
        @Autowired private val dynamoDbMemoryRepository: DynamoDbMemoryRepository
) {

    @AfterEach
    fun cleanUpMemoryTable() {
        dynamoDb.deleteItem(memoryTableName, mapOf("id" to AttributeValue(johnsGreatMemory.id)))
        dynamoDb.deleteItem(memoryTableName, mapOf("id" to AttributeValue(johnsAwesomeMemory.id)))
        dynamoDb.deleteItem(memoryTableName, mapOf("id" to AttributeValue(johnsNiceMemory.id)))
    }

    @Test
    fun `repository finds saved Memory by id`() {
        // given
        dynamoDbMemoryRepository.save(johnsGreatMemory)

        // when
        val foundMemory = dynamoDbMemoryRepository.findById(johnsGreatMemory.id)

        // then
        assertThat(foundMemory).isEqualTo(johnsGreatMemory)
    }

    @Test
    fun `repository returns null when no Memory found for a given id`() {
        // when
        val searchResult = dynamoDbMemoryRepository.findById(johnsGreatMemory.id)

        // then
        assertThat(searchResult).isNull()
    }

    @Test
    fun `repository deletes existing Memory`() {
        // given
        dynamoDbMemoryRepository.save(johnsGreatMemory)

        // when
        dynamoDbMemoryRepository.deleteById(johnsGreatMemory.id)

        // then
        assertThat(dynamoDbMemoryRepository.findById(johnsGreatMemory.id)).isNull()
    }

    @Test
    fun `repository throws when trying to delete a non-existing Memory`() {
        assertThrows<EmptyResultDataAccessException> {
            dynamoDbMemoryRepository.deleteById(johnsGreatMemory.id)
        }
    }

    @Test
    fun `repository finds all the memories of a user`() {
        // given
        dynamoDbMemoryRepository.save(johnsGreatMemory)
        dynamoDbMemoryRepository.save(johnsAwesomeMemory)
        dynamoDbMemoryRepository.save(johnsNiceMemory)

        // when
        val memoriesOfAUser = dynamoDbMemoryRepository.findMemoriesForUser(johnsGreatMemory.userName)

        assertThat(memoriesOfAUser.toSet()).isEqualTo(setOf(johnsGreatMemory, johnsAwesomeMemory, johnsNiceMemory))
    }
}

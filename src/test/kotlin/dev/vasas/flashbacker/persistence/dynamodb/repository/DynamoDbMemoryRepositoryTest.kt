package dev.vasas.flashbacker.persistence.dynamodb.repository

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import dev.vasas.flashbacker.persistence.dynamodb.entity.MemoryEntity.Companion.memoryTableName
import dev.vasas.flashbacker.testtooling.DynamoDbIntegrationTest
import dev.vasas.flashbacker.testtooling.awesomeMemoryOfBob
import dev.vasas.flashbacker.testtooling.greatMemoryOfBob
import dev.vasas.flashbacker.testtooling.niceMemoryOfBob
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
        dynamoDb.deleteItem(memoryTableName, mapOf("id" to AttributeValue(greatMemoryOfBob.id)))
        dynamoDb.deleteItem(memoryTableName, mapOf("id" to AttributeValue(awesomeMemoryOfBob.id)))
        dynamoDb.deleteItem(memoryTableName, mapOf("id" to AttributeValue(niceMemoryOfBob.id)))
    }

    @Test
    fun `repository finds saved Memory by id`() {
        // given
        dynamoDbMemoryRepository.save(greatMemoryOfBob)

        // when
        val foundMemory = dynamoDbMemoryRepository.findById(greatMemoryOfBob.id)

        // then
        assertThat(foundMemory).isEqualTo(greatMemoryOfBob)
    }

    @Test
    fun `repository returns null when no Memory found for a given id`() {
        // when
        val searchResult = dynamoDbMemoryRepository.findById(greatMemoryOfBob.id)

        // then
        assertThat(searchResult).isNull()
    }

    @Test
    fun `repository deletes existing Memory`() {
        // given
        dynamoDbMemoryRepository.save(greatMemoryOfBob)

        // when
        dynamoDbMemoryRepository.deleteById(greatMemoryOfBob.id)

        // then
        assertThat(dynamoDbMemoryRepository.findById(greatMemoryOfBob.id)).isNull()
    }

    @Test
    fun `repository throws when trying to delete a non-existing Memory`() {
        assertThrows<EmptyResultDataAccessException> {
            dynamoDbMemoryRepository.deleteById(greatMemoryOfBob.id)
        }
    }

    @Test
    fun `repository finds all the memories of a user`() {
        // given
        dynamoDbMemoryRepository.save(greatMemoryOfBob)
        dynamoDbMemoryRepository.save(awesomeMemoryOfBob)
        dynamoDbMemoryRepository.save(niceMemoryOfBob)

        // when
        val memoriesOfAUser = dynamoDbMemoryRepository.findMemoriesForUser(greatMemoryOfBob.userId)

        assertThat(memoriesOfAUser.toSet()).isEqualTo(setOf(greatMemoryOfBob, awesomeMemoryOfBob, niceMemoryOfBob))
    }
}

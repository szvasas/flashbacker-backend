package dev.vasas.flashbacker.persistence.dynamodb.repository

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import dev.vasas.flashbacker.domain.Memory
import dev.vasas.flashbacker.persistence.dynamodb.entity.MemoryEntity.Companion.memoryTableName
import dev.vasas.flashbacker.testtooling.DynamoDbIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import java.time.LocalDateTime

@DynamoDbIntegrationTest
internal class DynamoDbMemoryRepositoryTest(
        @Autowired private val dynamoDb: AmazonDynamoDB,
        @Autowired private val dynamoDbMemoryRepository: DynamoDbMemoryRepository
) {

    val testMemory = Memory(
            id = "testId",
            userName = "testUser",
            location = "testLocation",
            date = LocalDateTime.of(2017, 12, 3, 15, 2, 3, 1000000),
            text = "hello"
    )

    val anotherMemory = testMemory.copy(id = "anotherMemory")
    val thirdMemory = testMemory.copy(id = "thirdMemory")

    @AfterEach
    fun cleanUpMemoryTable() {
        dynamoDb.deleteItem(memoryTableName, mapOf("id" to AttributeValue(testMemory.id)))
        dynamoDb.deleteItem(memoryTableName, mapOf("id" to AttributeValue(anotherMemory.id)))
        dynamoDb.deleteItem(memoryTableName, mapOf("id" to AttributeValue(thirdMemory.id)))
    }

    @Test
    fun `repository finds saved Memory by id`() {
        // given
        dynamoDbMemoryRepository.save(testMemory)

        // when
        val foundMemory = dynamoDbMemoryRepository.findById(testMemory.id)

        // then
        assertThat(foundMemory).isEqualTo(testMemory)
    }

    @Test
    fun `repository returns null when no Memory found for a given id`() {
        // when
        val searchResult = dynamoDbMemoryRepository.findById(testMemory.id)

        // then
        assertThat(searchResult).isNull()
    }

    @Test
    fun `repository deletes existing Memory`() {
        // given
        dynamoDbMemoryRepository.save(testMemory)

        // when
        dynamoDbMemoryRepository.deleteById(testMemory.id)

        // then
        assertThat(dynamoDbMemoryRepository.findById(testMemory.id)).isNull()
    }

    @Test
    fun `repository throws when trying to delete a non-existing Memory`() {
        assertThrows<EmptyResultDataAccessException> {
            dynamoDbMemoryRepository.deleteById(testMemory.id)
        }
    }

    @Test
    fun `repository finds all the memories of a user`() {
        // given
        dynamoDbMemoryRepository.save(testMemory)
        dynamoDbMemoryRepository.save(anotherMemory)
        dynamoDbMemoryRepository.save(thirdMemory)

        // when
        val memoriesOfAUser = dynamoDbMemoryRepository.findMemoriesForUser(testMemory.userName)

        assertThat(memoriesOfAUser.toSet()).isEqualTo(setOf(testMemory, anotherMemory, thirdMemory))
    }
}

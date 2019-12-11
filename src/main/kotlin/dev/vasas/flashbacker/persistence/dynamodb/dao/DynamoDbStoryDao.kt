package dev.vasas.flashbacker.persistence.dynamodb.dao

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator
import com.amazonaws.services.dynamodbv2.model.Condition
import dev.vasas.flashbacker.persistence.dynamodb.entity.StoryEntity
import dev.vasas.flashbacker.persistence.dynamodb.entity.StoryEntity.Companion.dateHappenedAndIdFieldName
import dev.vasas.flashbacker.persistence.dynamodb.entity.StoryEntity.Companion.dateHappenedFieldName
import dev.vasas.flashbacker.persistence.dynamodb.entity.StoryEntity.Companion.idFieldName
import dev.vasas.flashbacker.persistence.dynamodb.entity.StoryEntity.Companion.locationFieldName
import dev.vasas.flashbacker.persistence.dynamodb.entity.StoryEntity.Companion.storyTableName
import dev.vasas.flashbacker.persistence.dynamodb.entity.StoryEntity.Companion.textFieldName
import dev.vasas.flashbacker.persistence.dynamodb.entity.StoryEntity.Companion.timestampAddedFieldName
import dev.vasas.flashbacker.persistence.dynamodb.entity.StoryEntity.Companion.userIdFieldName
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class DynamoDbStoryDao(private val dynamoDb: AmazonDynamoDB) {

    fun save(storyEntity: StoryEntity) {
        dynamoDb.putItem(storyTableName, storyEntity.toAttributeValueMap())
    }

    fun deleteById(id: String) {
        dynamoDb.deleteItem(storyTableName, mapOf(idFieldName to AttributeValue(id)))
    }

    fun deleteByUserIdAndDateHappenedAndId(userId: String, dateHappenedAndId: String) {
        val itemToDelete = findByUserIdAndDateHappenedAndId(userId, dateHappenedAndId)
        itemToDelete?.apply {
            dynamoDb.deleteItem(storyTableName, mapOf(idFieldName to AttributeValue(id)))
        }
    }

    fun findById(id: String): StoryEntity? {
        return dynamoDb
                .getItem(storyTableName, mapOf(idFieldName to AttributeValue(id)))
                .item
                ?.toStoryEntity()
    }

    fun findByUserId(userId: String): List<StoryEntity> {
        val equalToInputCondition = Condition()
                .withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(AttributeValue(userId))

        return dynamoDb
                .scan(storyTableName, mapOf(userIdFieldName to equalToInputCondition))
                .items
                .map { it.toStoryEntity() }
    }

    fun findByUserIdAndDateHappenedAndId(userId: String, dateHappenedAndId: String): StoryEntity? {
        // Since getItem works with keys only we had to implement this with scan now.
        val userEqualToInputCondition = Condition()
                .withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(AttributeValue(userId))
        val dateHappenedAndIdEqualToInputCondition = Condition()
                .withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(AttributeValue(dateHappenedAndId))

        return dynamoDb
                .scan(storyTableName, mapOf(
                        userIdFieldName to userEqualToInputCondition,
                        dateHappenedAndIdFieldName to dateHappenedAndIdEqualToInputCondition
                ))
                .items
                .map { it.toStoryEntity() }
                .firstOrNull()
    }

    fun findByUserIdAndDateHappened(userId: String, dateHappened: LocalDate): List<StoryEntity> {
        val userEqualToInputCondition = Condition()
                .withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(AttributeValue(userId))
        val dateHappenedEqualToInputCondition = Condition()
                .withComparisonOperator(ComparisonOperator.BEGINS_WITH)
                .withAttributeValueList(AttributeValue(dateHappened.toString()))

        return dynamoDb
                .scan(storyTableName, mapOf(
                        userIdFieldName to userEqualToInputCondition,
                        dateHappenedAndIdFieldName to dateHappenedEqualToInputCondition
                ))
                .items
                .map { it.toStoryEntity() }
    }

    private fun StoryEntity.toAttributeValueMap(): Map<String, AttributeValue> {
        return mapOf(
                idFieldName to AttributeValue(id),
                userIdFieldName to AttributeValue(userId),
                dateHappenedAndIdFieldName to AttributeValue(dateHappenedAndId),
                locationFieldName to AttributeValue(location),
                dateHappenedFieldName to AttributeValue().withN(dateHappened.toString()),
                timestampAddedFieldName to AttributeValue().withN(timestampAdded.toString()),
                textFieldName to AttributeValue(text)
        )
    }

    private fun Map<String, AttributeValue>.toStoryEntity(): StoryEntity {
        return StoryEntity(
                id = getStringOrThrow(idFieldName),
                userId = getStringOrThrow(userIdFieldName),
                dateHappenedAndId = getStringOrThrow(dateHappenedAndIdFieldName),
                location = getStringOrThrow(locationFieldName),
                dateHappened = getNumberOrThrow(dateHappenedFieldName),
                timestampAdded = getNumberOrThrow(timestampAddedFieldName),
                text = getStringOrThrow(textFieldName)
        )
    }

    private fun Map<String, AttributeValue>.getStringOrThrow(fieldName: String): String {
        return this[fieldName]?.s ?: throw InvalidDynamoDbItemException.missingMandatoryField(fieldName)
    }

    private fun Map<String, AttributeValue>.getNumberOrThrow(fieldName: String): Long {
        return this[fieldName]?.n?.toLong() ?: throw InvalidDynamoDbItemException.missingMandatoryField(fieldName)
    }
}

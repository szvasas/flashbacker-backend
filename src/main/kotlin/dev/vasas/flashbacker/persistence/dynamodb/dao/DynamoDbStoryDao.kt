package dev.vasas.flashbacker.persistence.dynamodb.dao

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator
import com.amazonaws.services.dynamodbv2.model.Condition
import dev.vasas.flashbacker.persistence.dynamodb.entity.StoryEntity
import dev.vasas.flashbacker.persistence.dynamodb.entity.StoryEntity.Companion.dateFieldName
import dev.vasas.flashbacker.persistence.dynamodb.entity.StoryEntity.Companion.idFieldName
import dev.vasas.flashbacker.persistence.dynamodb.entity.StoryEntity.Companion.locationFieldName
import dev.vasas.flashbacker.persistence.dynamodb.entity.StoryEntity.Companion.storyTableName
import dev.vasas.flashbacker.persistence.dynamodb.entity.StoryEntity.Companion.textFieldName
import dev.vasas.flashbacker.persistence.dynamodb.entity.StoryEntity.Companion.userIdFieldName
import org.springframework.stereotype.Component

@Component
class DynamoDbStoryDao(private val dynamoDb: AmazonDynamoDB) {

    fun save(storyEntity: StoryEntity) {
        dynamoDb.putItem(storyTableName, storyEntity.toAttributeValueMap())
    }

    fun deleteById(id: String) {
        dynamoDb.deleteItem(storyTableName, mapOf(idFieldName to AttributeValue(id)))
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

    private fun StoryEntity.toAttributeValueMap(): Map<String, AttributeValue> {
        return mapOf(
                idFieldName to AttributeValue(id),
                userIdFieldName to AttributeValue(userId),
                locationFieldName to AttributeValue(location),
                dateFieldName to AttributeValue().withN(date.toString()),
                textFieldName to AttributeValue(text)
        )
    }

    private fun Map<String, AttributeValue>.toStoryEntity(): StoryEntity {
        return StoryEntity(
                id = this[idFieldName]?.s ?: throw InvalidDynamoDbItemException.missingMandatoryField(idFieldName),
                userId = this[userIdFieldName]?.s ?: throw InvalidDynamoDbItemException.missingMandatoryField(userIdFieldName),
                location = this[locationFieldName]?.s ?: throw InvalidDynamoDbItemException.missingMandatoryField(locationFieldName),
                date = this[dateFieldName]?.n?.toLong() ?: throw InvalidDynamoDbItemException.missingMandatoryField(dateFieldName),
                text = this[textFieldName]?.s ?: throw InvalidDynamoDbItemException.missingMandatoryField(textFieldName)
        )
    }
}
